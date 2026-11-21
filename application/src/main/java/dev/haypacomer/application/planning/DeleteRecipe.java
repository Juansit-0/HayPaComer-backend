package dev.haypacomer.application.planning;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.RecipeRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.recipe.RecipeId;
import java.util.Objects;

public final class DeleteRecipe {

  private final GetHousehold households;
  private final RecipeRepository recipes;

  public DeleteRecipe(HouseholdRepository households, RecipeRepository recipes) {
    this.households = new GetHousehold(households);
    this.recipes = Objects.requireNonNull(recipes, "recipes");
  }

  public void delete(UserId actor, HouseholdId householdId, RecipeId recipeId) {
    households.get(actor, householdId).requirePermission(actor, Permission.COOK);
    recipes.find(householdId, recipeId).orElseThrow(RecipeNotFoundException::new);
    if (recipes.planned(householdId, recipeId)) {
      throw new RecipeInPlanException();
    }
    recipes.delete(householdId, recipeId);
  }
}
