package dev.haypacomer.application.inventory;

import dev.haypacomer.application.port.AccessRequestRepository;
import dev.haypacomer.application.port.FoodOwnershipRepository;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InventoryMovementLog;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.member.MemberId;
import java.util.List;

/** The requests waiting for your answer, and how your own asks went. */
public final class ListFoodRequests {

  private final FoodRequests shared;

  public ListFoodRequests(
      HouseholdRepository households,
      FridgeRepository fridges,
      FoodOwnershipRepository ownerships,
      InventoryMovementLog movements,
      AccessRequestRepository requests,
      UserRepository users) {
    this.shared = new FoodRequests(households, fridges, ownerships, movements, requests, users);
  }

  public List<FoodRequestView> list(UserId actor, HouseholdId householdId) {
    Household household = shared.inventory.household(actor, householdId);
    MemberId me = FoodRequests.member(household, actor);
    return shared.requests.involving(householdId, me).stream()
        .map(request -> shared.view(household, request, me))
        .toList();
  }
}
