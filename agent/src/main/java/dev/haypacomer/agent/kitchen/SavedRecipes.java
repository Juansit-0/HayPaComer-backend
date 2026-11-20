package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.domain.recipe.Recipe;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

final class SavedRecipes {

  private SavedRecipes() {}

  static List<Recipe> all(ListRecipes recipes, ToolInvocation invocation) {
    return recipes.list(invocation.user(), invocation.household());
  }

  static Optional<Recipe> find(ListRecipes recipes, ToolInvocation invocation) {
    String wanted = normalize(invocation.arguments().get("recipe"));
    List<Recipe> saved = all(recipes, invocation);
    return saved.stream()
        .filter(recipe -> normalize(recipe.name()).equals(wanted))
        .findFirst()
        .or(
            () ->
                saved.stream()
                    .filter(recipe -> normalize(recipe.name()).contains(wanted))
                    .findFirst());
  }

  static Optional<String> problem(ListRecipes recipes, ToolInvocation invocation) {
    if (find(recipes, invocation).isPresent()) {
      return Optional.empty();
    }
    List<Recipe> saved = all(recipes, invocation);
    return Optional.of(
        "No saved recipe called "
            + invocation.arguments().get("recipe").strip()
            + (saved.isEmpty()
                ? "; no recipes are saved yet"
                : "; saved recipes: "
                    + saved.stream().map(Recipe::name).collect(Collectors.joining(", "))));
  }

  static int servings(ToolInvocation invocation, Recipe recipe) {
    String servings = invocation.arguments().get("servings");
    return servings == null ? recipe.servings() : Integer.parseInt(servings.strip());
  }

  private static String normalize(String text) {
    return Normalizer.normalize(text.strip(), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }
}
