package dev.haypacomer.application.agent;

import dev.haypacomer.application.port.HouseholdMemory;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;

public final class RememberForHousehold {

  public static final int MAX_NOTES = EditHouseholdMemory.MAX_NOTES;

  private final EditHouseholdMemory edit;

  public RememberForHousehold(HouseholdRepository households, HouseholdMemory memory) {
    this.edit = new EditHouseholdMemory(households, memory);
  }

  public RememberForHousehold(
      HouseholdRepository households, HouseholdMemory memory, UnitOfWork unitOfWork) {
    this.edit = new EditHouseholdMemory(households, memory, unitOfWork);
  }

  public void remember(UserId actor, HouseholdId household, MemoryNote... notes) {
    edit.edit(actor, household, List.of(), List.of(notes));
  }
}
