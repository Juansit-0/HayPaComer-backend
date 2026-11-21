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
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.inventory.AccessRequest;
import dev.haypacomer.domain.inventory.Ownership;
import dev.haypacomer.domain.inventory.Visibility;
import dev.haypacomer.domain.member.MemberId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class AskForFood {

  private final FoodRequests shared;
  private final BroadcastLiveUpdate live;
  private final NotificationListener notify;
  private final Clock clock;

  public AskForFood(
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

  public FoodRequestView ask(UserId actor, HouseholdId householdId, FoodItemId itemId) {
    Household household = shared.inventory.household(actor, householdId);
    MemberId me = FoodRequests.member(household, actor);
    shared.inventory.locate(householdId, itemId);
    Ownership ownership =
        shared
            .inventory
            .ownerships()
            .find(itemId)
            .filter(found -> found.visibility() == Visibility.ASK_FIRST)
            .orElseThrow(() -> new IllegalStateException("This food does not need asking"));
    if (ownership.allows(me)) {
      throw new IllegalStateException("You can already use this food");
    }
    Instant now = clock.instant();
    AccessRequest request =
        shared
            .requests
            .findPending(itemId, me)
            .orElseGet(
                () -> {
                  AccessRequest opened =
                      AccessRequest.open(householdId, itemId, me, ownership.owner(), now);
                  shared.requests.save(opened);
                  live.publish(
                      LiveUpdate.of(
                          householdId, LiveUpdateKind.INVENTORY, null, FoodRequests.ASKED, now));
                  FoodRequestView asked = shared.view(household, opened, me);
                  if (asked.ownerUserId() != null) {
                    notify.onNotification(
                        Notification.to(
                            new UserId(asked.ownerUserId()),
                            householdId,
                            NotificationType.FOOD_REQUEST,
                            "Food request",
                            asked.requesterName() + " asked to use your " + asked.food(),
                            now));
                  }
                  return opened;
                });
    return shared.view(household, request, me);
  }
}
