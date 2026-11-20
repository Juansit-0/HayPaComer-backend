package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.domain.recipe.Recipe;
import java.util.List;
import java.util.stream.Collectors;

public final class ListRecipesTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.read(
          "list_recipes", "The household's saved recipes with servings and minutes.", List.of());

  private final ListRecipes recipes;

  public ListRecipesTool(ListRecipes recipes) {
    this.recipes = recipes;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Read the saved recipes";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    List<Recipe> saved = SavedRecipes.all(recipes, invocation);
    if (saved.isEmpty()) {
      return Observation.of(SPEC.name(), "No recipes are saved yet");
    }
    return Observation.of(
        SPEC.name(),
        saved.stream()
            .map(
                recipe ->
                    recipe.name()
                        + ": "
                        + recipe.servings()
                        + " servings, "
                        + recipe.minutes()
                        + " min")
            .collect(Collectors.joining("\n")));
  }
}
