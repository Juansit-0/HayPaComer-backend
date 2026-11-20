package dev.haypacomer.application.moving;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.CarriedFood;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Membership;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

/**
 * Before someone leaves a home (or is taken out of it), their own food leaves the fridge with them;
 * it goes into the fridge of the next home they join. The same rules as leaving apply first.
 */
public final class PackUpMemberFood {

  private final GetHousehold households;
  private final CarriedFood carried;

  public PackUpMemberFood(HouseholdRepository households, CarriedFood carried) {
    this.households = new GetHousehold(households);
    this.carried = Objects.requireNonNull(carried, "carried");
  }

  public int packUp(UserId actor, HouseholdId id, UserId target) {
    Household household = households.get(actor, id);
    Membership leaving =
        household
            .membershipOf(target)
            .orElseThrow(
                () -> new IllegalArgumentException("User is not a member of this household"));
    if (!actor.equals(target)) {
      household.requirePermission(actor, Permission.MANAGE_MEMBERS);
    }
    if (leaving.role() == Role.OWNER) {
      throw new IllegalStateException("The owner cannot leave; transfer ownership first");
    }
    return carried.packUp(id, target);
  }
}
