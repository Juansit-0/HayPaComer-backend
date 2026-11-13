package dev.haypacomer.application.agent;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdMemory;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EditHouseholdMemory {

  public static final int MAX_NOTES = 200;

  private final GetHousehold households;
  private final HouseholdMemory memory;
  private final UnitOfWork unitOfWork;

  public EditHouseholdMemory(HouseholdRepository households, HouseholdMemory memory) {
    this(households, memory, new DirectWork());
  }

  public EditHouseholdMemory(
      HouseholdRepository households, HouseholdMemory memory, UnitOfWork unitOfWork) {
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.households = new GetHousehold(households);
    this.memory = Objects.requireNonNull(memory, "memory");
  }

  public void edit(
      UserId actor, HouseholdId household, List<String> forgetKeys, List<MemoryNote> notes) {
    unitOfWork.runFor(
        household,
        () -> {
          editInTurn(actor, household, forgetKeys, notes);
          return null;
        });
  }

  // Everything is counted before anything is written, so an edit that does not fit changes
  // nothing.
  private void editInTurn(
      UserId actor, HouseholdId household, List<String> forgetKeys, List<MemoryNote> notes) {
    households.get(actor, household).requirePermission(actor, Permission.COOK);
    Set<String> kept = new HashSet<>(memory.read(household).keySet());
    kept.removeAll(forgetKeys);
    notes.forEach(note -> kept.add(note.key()));
    if (kept.size() > MAX_NOTES) {
      throw new IllegalStateException("Household memory is full; forget something first");
    }
    forgetKeys.forEach(key -> memory.forget(household, key));
    notes.forEach(note -> memory.remember(household, note.key(), note.value()));
  }
}
