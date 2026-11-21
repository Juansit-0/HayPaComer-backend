package dev.haypacomer.application.inventory;

import dev.haypacomer.application.port.AccessRequestRepository;
import dev.haypacomer.application.port.FoodOwnershipRepository;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InventoryMovementLog;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.household.AccessDeniedException;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.Membership;
import dev.haypacomer.domain.identity.User;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.inventory.AccessRequest;
import dev.haypacomer.domain.member.MemberId;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class FoodRequests {

  static final String ASKED = "Someone asked to use a food";
  static final String ANSWERED = "A food request was answered";

  final HouseholdInventory inventory;
  final AccessRequestRepository requests;
  private final UserRepository users;

  FoodRequests(
      HouseholdRepository households,
      FridgeRepository fridges,
      FoodOwnershipRepository ownerships,
      InventoryMovementLog movements,
      AccessRequestRepository requests,
      UserRepository users) {
    this.inventory = new HouseholdInventory(households, fridges, ownerships, movements);
    this.requests = Objects.requireNonNull(requests, "requests");
    this.users = Objects.requireNonNull(users, "users");
  }

  static MemberId member(Household household, UserId actor) {
    return household
        .membershipOf(actor)
        .map(Membership::member)
        .orElseThrow(() -> new AccessDeniedException("You are not a member of this household"));
  }

  FoodRequestView view(Household household, AccessRequest request, MemberId viewer) {
    Optional<FoodItem> item = inventory.find(request.household(), request.item());
    return new FoodRequestView(
        request.id(),
        request.owner().equals(viewer) && request.isPending(),
        request.item().value(),
        item.map(found -> found.food().name()).orElse(null),
        userOf(household, request.requester()),
        nameOf(household, request.requester()),
        userOf(household, request.owner()),
        nameOf(household, request.owner()),
        request.status(),
        request.createdAt(),
        request.decidedAt());
  }

  private static Optional<Membership> membership(Household household, MemberId member) {
    return household.memberships().stream()
        .filter(candidate -> candidate.member().equals(member))
        .findFirst();
  }

  private static UUID userOf(Household household, MemberId member) {
    return membership(household, member).map(found -> found.user().value()).orElse(null);
  }

  private String nameOf(Household household, MemberId member) {
    return membership(household, member)
        .flatMap(found -> users.findById(found.user()))
        .map(User::displayName)
        .orElse(null);
  }
}
