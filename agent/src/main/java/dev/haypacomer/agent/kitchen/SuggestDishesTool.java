package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.ai.SuggestDishes;
import dev.haypacomer.application.ai.Suggestion;
import dev.haypacomer.application.ai.SuggestionQuery;
import dev.haypacomer.application.ai.Suggestions;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.domain.cooking.RecipeEvaluation;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.recipe.Recipe;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SuggestDishesTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.read(
          "suggest_dishes",
          "Rank the saved recipes by what can be cooked now with measured stock, rescuing food"
              + " that expires first.",
          List.of(ParameterSpec.optional("servings", ParameterType.COUNT, "Servings, default 2")));

  private final SuggestDishes suggest;
  private final ListRecipes recipes;

  public SuggestDishesTool(SuggestDishes suggest, ListRecipes recipes) {
    this.suggest = suggest;
    this.recipes = recipes;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Suggest dishes from the saved recipes";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    List<Recipe> saved = SavedRecipes.all(recipes, invocation);
    if (saved.isEmpty()) {
      return Observation.of(SPEC.name(), "No recipes are saved yet; save one to get suggestions");
    }
    String servings = invocation.arguments().get("servings");
    Suggestions found =
        suggest.suggest(
            invocation.user(),
            invocation.household(),
            SuggestionQuery.builder()
                .candidates(saved.stream().limit(SuggestionQuery.MAX_CANDIDATES).toList())
                .servings(servings == null ? 2 : Integer.parseInt(servings.strip()))
                .limit(3)
                .build());
    if (found.items().isEmpty()) {
      return Observation.of(SPEC.name(), "No saved recipe fits the stock right now");
    }
    List<Suggestion> items = found.items();
    return Observation.of(
        SPEC.name(),
        IntStream.range(0, items.size())
            .mapToObj(index -> (index + 1) + ". " + line(items.get(index)))
            .collect(Collectors.joining("\n")));
  }

  private static String line(Suggestion suggestion) {
    RecipeEvaluation evaluation = suggestion.evaluation();
    StringBuilder line =
        new StringBuilder(evaluation.recipe().name())
            .append(", ")
            .append(evaluation.recipe().minutes())
            .append(" min: ")
            .append(suggestion.reason());
    if (!suggestion.rescuedFoods().isEmpty()) {
      line.append("; rescues ")
          .append(
              suggestion.rescuedFoods().stream()
                  .map(FoodMetadata::name)
                  .collect(Collectors.joining(", ")));
    }
    if (!evaluation.missing().isEmpty()) {
      line.append("; missing ")
          .append(
              evaluation.missing().stream()
                  .map(missing -> missing.requirement().food().name() + " " + missing.shortfall())
                  .collect(Collectors.joining(", ")));
    }
    return line.toString();
  }
}
