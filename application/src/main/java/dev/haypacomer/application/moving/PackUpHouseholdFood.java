package dev.haypacomer.application.moving;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.CarriedFood;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Membership;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

/** Before a home is deleted, everyone takes their own food along to their next home. */
public final class PackUpHouseholdFood {

  private final GetHousehold households;
  private final CarriedFood carried;

  public PackUpHouseholdFood(HouseholdRepository households, CarriedFood carried) {
    this.households = new GetHousehold(households);
    this.carried = Objects.requireNonNull(carried, "carried");
  }

  public int packUp(UserId actor, HouseholdId id) {
    Household household = households.get(actor, id);
    household.requirePermission(actor, Permission.DELETE_HOUSEHOLD);
    int packed = 0;
    for (Membership membership : household.memberships()) {
      packed += carried.packUp(id, membership.user());
    }
    return packed;
  }
}
