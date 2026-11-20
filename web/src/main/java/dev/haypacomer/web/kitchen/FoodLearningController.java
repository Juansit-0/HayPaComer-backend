package dev.haypacomer.web.kitchen;

import dev.haypacomer.application.inventory.DensityEstimate;
import dev.haypacomer.application.inventory.EstimateFoodDensity;
import dev.haypacomer.application.inventory.LearnFood;
import dev.haypacomer.domain.food.FoodCategory;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.web.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FoodLearningController {

  private final LearnFood learnFood;
  private final EstimateFoodDensity estimateFoodDensity;

  public FoodLearningController(LearnFood learnFood, EstimateFoodDensity estimateFoodDensity) {
    this.learnFood = learnFood;
    this.estimateFoodDensity = estimateFoodDensity;
  }

  @GetMapping("/api/v1/households/{householdId}/foods/density")
  DensityEstimate density(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @RequestParam(defaultValue = "") String food) {
    return estimateFoodDensity.estimate(CurrentUser.of(jwt), new HouseholdId(householdId), food);
  }

  @PostMapping("/api/v1/households/{householdId}/foods")
  LearnedFood learn(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @Valid @RequestBody LearnRequest request) {
    return LearnedFood.from(
        learnFood.learn(CurrentUser.of(jwt), new HouseholdId(householdId), request.name()));
  }

  record LearnRequest(@NotBlank @Size(max = 60) String name) {}

  record LearnedFood(String name, FoodCategory category, boolean perishable, int shelfDays) {

    static LearnedFood from(FoodMetadata food) {
      return new LearnedFood(food.name(), food.category(), food.perishable(), food.shelfDays());
    }
  }
}
