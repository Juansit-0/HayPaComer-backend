package dev.haypacomer.agent.runtime;

import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

public record AgentTask(HouseholdId household, UserId user, String specialist, String goal) {

  public static final String EARLIER = "\n\nEarlier in this conversation:\n";

  public AgentTask {
    Objects.requireNonNull(household, "household");
    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(specialist, "specialist");
    Objects.requireNonNull(goal, "goal");
    if (goal.isBlank()) {
      throw new IllegalArgumentException("A task needs a goal");
    }
  }

  public String asked() {
    return asked(goal);
  }

  public static String asked(String goal) {
    int earlier = goal.indexOf(EARLIER);
    return (earlier < 0 ? goal : goal.substring(0, earlier)).strip();
  }
}
