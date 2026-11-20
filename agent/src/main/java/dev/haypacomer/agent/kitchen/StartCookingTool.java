package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.application.session.ResumeCookingSession;
import dev.haypacomer.application.session.StartCookingSession;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.recipe.Recipe;
import dev.haypacomer.domain.session.CookingSession;
import java.util.List;
import java.util.Optional;

public final class StartCookingTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "start_cooking",
          "Propose starting a guided cooking session of a saved recipe, after the person"
              + " confirms it.",
          Permission.COOK,
          List.of(
              ParameterSpec.required("recipe", ParameterType.TEXT, "Saved recipe name"),
              ParameterSpec.optional(
                  "servings", ParameterType.COUNT, "Servings, default the recipe's")));

  private final StartCookingSession start;
  private final ResumeCookingSession sessions;
  private final ListRecipes recipes;

  public StartCookingTool(
      StartCookingSession start, ResumeCookingSession sessions, ListRecipes recipes) {
    this.start = start;
    this.sessions = sessions;
    this.recipes = recipes;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    Optional<CookingSession> active = sessions.active(invocation.user(), invocation.household());
    if (active.isPresent()) {
      return Optional.of(
          "A cooking session is already active: "
              + active.get().recipe().name()
              + "; finish or cancel it first");
    }
    return SavedRecipes.problem(recipes, invocation);
  }

  @Override
  public String describe(ToolInvocation invocation) {
    Recipe recipe = SavedRecipes.find(recipes, invocation).orElseThrow();
    return "Start cooking " + recipe.name() + " for " + SavedRecipes.servings(invocation, recipe);
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    Recipe recipe = SavedRecipes.find(recipes, invocation).orElseThrow();
    CookingSession session =
        start.start(
            invocation.user(),
            invocation.household(),
            recipe,
            SavedRecipes.servings(invocation, recipe),
            Optional.empty());
    return Observation.of(SPEC.name(), "Started " + CookingLines.describe(session));
  }
}
