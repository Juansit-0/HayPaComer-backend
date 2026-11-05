package dev.haypacomer.application.household;

import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Membership;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

public final class ChangeMemberRole {

  private final MemberHouseholds households;

  private final UnitOfWork unitOfWork;

  public ChangeMemberRole(HouseholdRepository households) {
    this(households, new DirectWork());
  }

  public ChangeMemberRole(HouseholdRepository households, UnitOfWork unitOfWork) {
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.households = new MemberHouseholds(households);
  }

  public Membership change(UserId actor, HouseholdId id, UserId target, Role role) {
    return unitOfWork.runFor(id, () -> changeInTurn(actor, id, target, role));
  }

  private Membership changeInTurn(UserId actor, HouseholdId id, UserId target, Role role) {
    Household household = households.load(actor, id);
    Membership membership = household.changeRole(actor, target, role);
    households.save(household);
    return membership;
  }
}
