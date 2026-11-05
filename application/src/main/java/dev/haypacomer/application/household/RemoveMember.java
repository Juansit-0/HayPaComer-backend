package dev.haypacomer.application.household;

import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

public final class RemoveMember {

  private final MemberHouseholds households;

  private final UnitOfWork unitOfWork;

  public RemoveMember(HouseholdRepository households) {
    this(households, new DirectWork());
  }

  public RemoveMember(HouseholdRepository households, UnitOfWork unitOfWork) {
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.households = new MemberHouseholds(households);
  }

  public void remove(UserId actor, HouseholdId id, UserId target) {
    unitOfWork.runFor(
        id,
        () -> {
          removeInTurn(actor, id, target);
          return null;
        });
  }

  private void removeInTurn(UserId actor, HouseholdId id, UserId target) {
    Household household = households.load(actor, id);
    household.remove(actor, target);
    households.save(household);
  }
}
