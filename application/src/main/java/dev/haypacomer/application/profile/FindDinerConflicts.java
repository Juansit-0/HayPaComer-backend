package dev.haypacomer.application.profile;

import dev.haypacomer.application.port.FoodProfileRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.member.MemberId;
import dev.haypacomer.domain.member.ProfileConflict;
import dev.haypacomer.domain.recipe.Recipe;
import java.util.List;
import java.util.Set;

public final class FindDinerConflicts {

  private final SelectDiners diners;

  public FindDinerConflicts(HouseholdRepository households, FoodProfileRepository profiles) {
    this.diners = new SelectDiners(households, profiles);
  }

  public List<ProfileConflict> find(
      UserId actor, HouseholdId household, Recipe recipe, Set<MemberId> chosenDiners) {
    return diners.select(actor, household, chosenDiners).conflictsWith(recipe);
  }
}
