package dev.haypacomer.application.agent;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.HouseholdMemory;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

public final class ForgetForHousehold {

  private final GetHousehold households;
  private final HouseholdMemory memory;

  public ForgetForHousehold(HouseholdRepository households, HouseholdMemory memory) {
    this.households = new GetHousehold(households);
    this.memory = Objects.requireNonNull(memory, "memory");
  }

  public void forget(UserId actor, HouseholdId household, MemoryTopic topic, String subject) {
    households.get(actor, household).requirePermission(actor, Permission.COOK);
    memory.forget(household, MemoryNote.keyOf(topic, subject));
  }
}
