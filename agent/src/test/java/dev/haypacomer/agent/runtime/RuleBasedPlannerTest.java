package dev.haypacomer.agent.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleBasedPlannerTest {

  private final AgentTask task =
      new AgentTask(HouseholdId.newId(), UserId.newId(), "chef", "What expires?");
  private final RuleBasedPlanner planner =
      new RuleBasedPlanner(List.of("view_expiries", "query_inventory"));

  @Test
  void readsRegisteredToolsInOrderOnce() {
    AgentContext fresh = new AgentContext(task, Set.of("query_inventory"), List.of(), 3);

    Decision decision = planner.next(fresh);

    assertEquals(
        new Decision.CallTool("query_inventory", Map.of(), "Offline rules read query_inventory"),
        decision);
  }

  @Test
  void answersWithoutEvidenceWhenNothingCouldBeRead() {
    Decision.CallTool call = new Decision.CallTool("view_expiries", Map.of(), "read");
    AgentContext failed =
        new AgentContext(
            task,
            Set.of("view_expiries"),
            List.of(new Exchange(call, Observation.failure("view_expiries", "down"))),
            3);

    assertEquals(
        new Decision.FinalAnswer("No evidence available right now; nothing was changed."),
        planner.next(failed));
    assertEquals(
        new Decision.FinalAnswer("No evidence available right now; nothing was changed."),
        planner.next(new AgentContext(task, Set.of("view_expiries"), List.of(), 0)));
  }

  @Test
  void proposesWhatThePersonAskedToAdd() {
    assertEquals(Map.of("food", "tomate", "grams", "500"), proposal("agrega tomate al mercado"));
    assertEquals(Map.of("food", "arroz", "grams", "2000"), proposal("compra 2 kg de arroz"));
    assertEquals(Map.of("food", "milk", "grams", "250"), proposal("add 250 g milk to the list"));
  }

  @Test
  void aQuestionIsNotAnOrder() {
    AgentContext context =
        new AgentContext(
            new AgentTask(HouseholdId.newId(), UserId.newId(), "market", "What is on my list?"),
            Set.of("add_to_market", "view_market_list"),
            List.of(),
            3);

    assertEquals(
        new Decision.CallTool("view_market_list", Map.of(), "Offline rules read view_market_list"),
        new RuleBasedPlanner(List.of("view_market_list")).next(context));
  }

  private Map<String, String> proposal(String goal) {
    AgentContext context =
        new AgentContext(
            new AgentTask(HouseholdId.newId(), UserId.newId(), "market", goal),
            Set.of("add_to_market"),
            List.of(),
            3);
    return ((Decision.CallTool) planner.next(context)).arguments();
  }
}
