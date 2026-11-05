package dev.haypacomer.application.household;

import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

public final class TransferOwnership {

  private final MemberHouseholds households;

  private final UnitOfWork unitOfWork;

  public TransferOwnership(HouseholdRepository households) {
    this(households, new DirectWork());
  }

  public TransferOwnership(HouseholdRepository households, UnitOfWork unitOfWork) {
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.households = new MemberHouseholds(households);
  }

  public Household transfer(UserId actor, HouseholdId id, UserId newOwner) {
    return unitOfWork.runFor(id, () -> transferInTurn(actor, id, newOwner));
  }

  private Household transferInTurn(UserId actor, HouseholdId id, UserId newOwner) {
    Household household = households.load(actor, id);
    household.transferOwnership(actor, newOwner);
    households.save(household);
    return household;
  }
}
