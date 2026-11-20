package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.market.AddMissingToMarketList;
import dev.haypacomer.application.market.MissingItem;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.recipe.Recipe;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MissingToMarketTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "missing_to_market",
          "Propose adding what a saved recipe is missing at home to the market list, after the"
              + " person confirms it.",
          Permission.MANAGE_MARKET_LIST,
          List.of(
              ParameterSpec.required("recipe", ParameterType.TEXT, "Saved recipe name"),
              ParameterSpec.optional(
                  "servings", ParameterType.COUNT, "Servings, default the recipe's")));

  private final AddMissingToMarketList missing;
  private final ListRecipes recipes;

  public MissingToMarketTool(AddMissingToMarketList missing, ListRecipes recipes) {
    this.missing = missing;
    this.recipes = recipes;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    return SavedRecipes.problem(recipes, invocation);
  }

  @Override
  public String describe(ToolInvocation invocation) {
    Recipe recipe = SavedRecipes.find(recipes, invocation).orElseThrow();
    return "Add what "
        + recipe.name()
        + " for "
        + SavedRecipes.servings(invocation, recipe)
        + " is missing to the market list";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    Recipe recipe = SavedRecipes.find(recipes, invocation).orElseThrow();
    List<MissingItem> added =
        missing.add(
            invocation.user(),
            invocation.household(),
            recipe,
            SavedRecipes.servings(invocation, recipe));
    if (added.isEmpty()) {
      return Observation.of(SPEC.name(), "Nothing is missing for " + recipe.name());
    }
    return Observation.of(
        SPEC.name(),
        "Missing for "
            + recipe.name()
            + ": "
            + added.stream()
                .map(
                    item ->
                        item.food().name()
                            + " "
                            + item.shortfall()
                            + " (added "
                            + item.added()
                            + ", "
                            + item.pending()
                            + " pending)")
                .collect(Collectors.joining("; ")));
  }
}
