package dev.haypacomer.application.inventory;

import dev.haypacomer.application.ai.AiRateLimitExceededException;
import dev.haypacomer.application.ai.FoodVerdict;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.AiRateLimiter;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.application.port.FoodClassifier;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.ConversionFactors;
import dev.haypacomer.domain.quantity.Unit;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class LearnFood {

  static final int CALLS_PER_MINUTE = 10;
  static final int MAX_NAME = 60;

  private final GetHousehold households;
  private final FoodCatalogRepository catalog;
  private final FoodClassifier classifier;
  private final AiRateLimiter limiter;

  public LearnFood(
      HouseholdRepository households,
      FoodCatalogRepository catalog,
      FoodClassifier classifier,
      AiRateLimiter limiter) {
    this.households = new GetHousehold(Objects.requireNonNull(households, "households"));
    this.catalog = Objects.requireNonNull(catalog, "catalog");
    this.classifier = Objects.requireNonNull(classifier, "classifier");
    this.limiter = Objects.requireNonNull(limiter, "limiter");
  }

  public FoodMetadata learn(UserId actor, HouseholdId household, String name) {
    households.get(actor, household).requirePermission(actor, Permission.EDIT_INVENTORY);
    String wanted = name == null ? "" : name.strip();
    if (wanted.isEmpty() || wanted.length() > MAX_NAME) {
      throw new IllegalArgumentException("Food names have 1 to " + MAX_NAME + " characters");
    }
    Optional<FoodMetadata> known = catalog.findByName(wanted);
    if (known.isPresent()) {
      return known.get();
    }
    if (!limiter.tryAcquire("food:" + actor.value(), CALLS_PER_MINUTE, Duration.ofMinutes(1))) {
      throw new AiRateLimitExceededException();
    }
    FoodVerdict verdict = classifier.classify(wanted);
    if (!verdict.food()) {
      throw new NotFoodException(wanted, verdict.reason());
    }
    Optional<FoodMetadata> canonical = catalog.findByName(verdict.name());
    if (canonical.isPresent()) {
      return canonical.get();
    }
    FoodMetadata learned =
        new FoodMetadata(
            verdict.name(),
            verdict.category(),
            Unit.GRAM,
            ConversionFactors.MASS_ONLY,
            verdict.perishable(),
            verdict.shelfDays(),
            Set.of());
    catalog.save(learned);
    return learned;
  }
}
