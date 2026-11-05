package dev.haypacomer.application.agent;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdMemory;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.util.Arrays;
import java.util.Objects;

public final class RememberForHousehold {

  public static final int MAX_NOTES = 200;

  private final GetHousehold households;
  private final HouseholdMemory memory;
  private final UnitOfWork unitOfWork;

  public RememberForHousehold(HouseholdRepository households, HouseholdMemory memory) {
    this(households, memory, new DirectWork());
  }

  public RememberForHousehold(
      HouseholdRepository households, HouseholdMemory memory, UnitOfWork unitOfWork) {
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.households = new GetHousehold(households);
    this.memory = Objects.requireNonNull(memory, "memory");
  }

  public void remember(UserId actor, HouseholdId household, MemoryNote... notes) {
    unitOfWork.runFor(
        household,
        () -> {
          rememberInTurn(actor, household, notes);
          return null;
        });
  }

  // The notes are counted before any is written, so a batch that does not fit leaves nothing.
  private void rememberInTurn(UserId actor, HouseholdId household, MemoryNote[] notes) {
    households.get(actor, household).requirePermission(actor, Permission.COOK);
    var current = memory.read(household);
    long added =
        Arrays.stream(notes)
            .map(MemoryNote::key)
            .distinct()
            .filter(key -> !current.containsKey(key))
            .count();
    if (current.size() + added > MAX_NOTES) {
      throw new IllegalStateException("Household memory is full; forget something first");
    }
    for (MemoryNote note : notes) {
      memory.remember(household, note.key(), note.value());
    }
  }
}
