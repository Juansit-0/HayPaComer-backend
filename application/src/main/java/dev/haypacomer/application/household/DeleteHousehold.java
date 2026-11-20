package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

/** The person who made the home deletes it, with its fridge and food, so everyone in it is free. */
public final class DeleteHousehold {

  private final HouseholdRepository households;

  public DeleteHousehold(HouseholdRepository households) {
    this.households = Objects.requireNonNull(households, "households");
  }

  public void delete(UserId actor, HouseholdId id) {
    Household household = new MemberHouseholds(households).load(actor, id);
    household.requirePermission(actor, Permission.DELETE_HOUSEHOLD);
    households.delete(id);
  }
}
