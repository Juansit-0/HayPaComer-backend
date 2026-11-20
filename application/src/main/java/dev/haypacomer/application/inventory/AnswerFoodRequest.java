package dev.haypacomer.application.inventory;

import dev.haypacomer.application.live.BroadcastLiveUpdate;
import dev.haypacomer.application.live.LiveUpdate;
import dev.haypacomer.application.live.LiveUpdateKind;
import dev.haypacomer.application.notification.Notification;
import dev.haypacomer.application.notification.NotificationType;
import dev.haypacomer.application.port.AccessRequestRepository;
import dev.haypacomer.application.port.FoodOwnershipRepository;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InventoryMovementLog;
import dev.haypacomer.application.port.NotificationListener;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.household.AccessDeniedException;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.inventory.AccessRequest;
import dev.haypacomer.domain.inventory.Ownership;
import dev.haypacomer.domain.member.MemberId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** The owner answers a request once; a yes grants the food, as granting it by hand would. */
public final class AnswerFoodRequest {

  private final FoodRequests shared;
  private final BroadcastLiveUpdate live;
  private final NotificationListener notify;
  private final Clock clock;

  public AnswerFoodRequest(
      HouseholdRepository households,
      FridgeRepository fridges,
      FoodOwnershipRepository ownerships,
      InventoryMovementLog movements,
      AccessRequestRepository requests,
      UserRepository users,
      BroadcastLiveUpdate live,
      NotificationListener notify,
      Clock clock) {
    this.shared = new FoodRequests(households, fridges, ownerships, movements, requests, users);
    this.live = Objects.requireNonNull(live, "live");
    this.notify = Objects.requireNonNull(notify, "notify");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public FoodRequestView answer(
      UserId actor, HouseholdId householdId, UUID requestId, boolean approve) {
    Household household = shared.inventory.household(actor, householdId);
    MemberId me = FoodRequests.member(household, actor);
    AccessRequest request =
        shared
            .requests
            .find(householdId, requestId)
            .orElseThrow(AccessRequestNotFoundException::new);
    Ownership ownership = shared.inventory.ownerships().find(request.item()).orElse(null);
    if (ownership == null || !ownership.owner().equals(me)) {
      throw new AccessDeniedException("Only the person who stocked this food can answer");
    }
    Instant now = clock.instant();
    AccessRequest decided = request.decide(approve, now);
    if (approve) {
      shared.inventory.ownerships().save(request.item(), ownership.grant(request.requester()));
    }
    shared.requests.save(decided);
    live.publish(
        LiveUpdate.of(householdId, LiveUpdateKind.INVENTORY, null, FoodRequests.ANSWERED, now));
    FoodRequestView answered = shared.view(household, decided, me);
    // Whoever asked hears the answer in their notifications.
    if (answered.requesterUserId() != null) {
      notify.onNotification(
          Notification.to(
              new UserId(answered.requesterUserId()),
              householdId,
              NotificationType.FOOD_REQUEST_ANSWERED,
              "Answer to your request",
              approve
                  ? answered.ownerName() + " let you use " + answered.food()
                  : answered.ownerName() + " would rather keep " + answered.food(),
              now));
    }
    return answered;
  }
}
