package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.planning.GenerateWeeklyPlan;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.planning.WeeklyPlan;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GenerateWeekPlanTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "generate_week_plan",
          "Propose making this week's meal plan from the saved recipes, rescuing food that"
              + " expires first, after the person confirms it.",
          Permission.COOK,
          List.of(ParameterSpec.optional("servings", ParameterType.COUNT, "Servings, default 2")));

  private final GenerateWeeklyPlan generate;
  private final ListRecipes recipes;

  public GenerateWeekPlanTool(GenerateWeeklyPlan generate, ListRecipes recipes) {
    this.generate = generate;
    this.recipes = recipes;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    return SavedRecipes.all(recipes, invocation).isEmpty()
        ? Optional.of("No recipes are saved yet; save one before planning the week")
        : Optional.empty();
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Make this week's meal plan for " + servings(invocation);
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    WeeklyPlan plan =
        generate.generate(
            invocation.user(), invocation.household(), servings(invocation), Set.of());
    if (plan.entries().isEmpty()) {
      return Observation.of(SPEC.name(), "Made an empty plan for the week of " + plan.weekStart());
    }
    return Observation.of(
        SPEC.name(),
        "Planned "
            + plan.entries().size()
            + " meals for the week of "
            + plan.weekStart()
            + "\n"
            + plan.entries().stream()
                .map(
                    entry ->
                        plan.dateOf(entry)
                            + " "
                            + entry.meal()
                            + ": "
                            + entry.recipe().name()
                            + (entry.needsShopping() ? ", needs shopping" : ""))
                .collect(Collectors.joining("\n")));
  }

  private static int servings(ToolInvocation invocation) {
    String servings = invocation.arguments().get("servings");
    return servings == null ? 2 : Integer.parseInt(servings.strip());
  }
}
