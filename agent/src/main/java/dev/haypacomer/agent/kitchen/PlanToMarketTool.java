package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.planning.AddPlanDeltaToMarketList;
import dev.haypacomer.application.planning.PlanDeltaItem;
import dev.haypacomer.application.planning.ViewCurrentPlan;
import dev.haypacomer.application.planning.WeeklyPlanNotFoundException;
import dev.haypacomer.domain.household.Permission;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class PlanToMarketTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "plan_to_market",
          "Propose adding what the rest of this week's plan needs beyond the stock at home to"
              + " the market list, after the person confirms it.",
          Permission.MANAGE_MARKET_LIST,
          List.of());

  private final AddPlanDeltaToMarketList delta;
  private final ViewCurrentPlan plans;

  public PlanToMarketTool(AddPlanDeltaToMarketList delta, ViewCurrentPlan plans) {
    this.delta = delta;
    this.plans = plans;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    try {
      plans.view(invocation.user(), invocation.household());
      return Optional.empty();
    } catch (WeeklyPlanNotFoundException none) {
      return Optional.of("No plan for this week; make the weekly plan first");
    }
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Add what this week's plan needs to the market list";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    List<PlanDeltaItem> added = delta.add(invocation.user(), invocation.household());
    if (added.isEmpty()) {
      return Observation.of(SPEC.name(), "The stock at home covers the plan; nothing added");
    }
    return Observation.of(
        SPEC.name(),
        "Added for the plan: "
            + added.stream()
                .map(
                    item ->
                        item.added()
                            + " of "
                            + item.food().name()
                            + " ("
                            + item.pending()
                            + " pending)")
                .collect(Collectors.joining("; ")));
  }
}
