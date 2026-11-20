package dev.haypacomer.application.inventory;

import dev.haypacomer.application.ai.AiRateLimitExceededException;
import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.AiRateLimiter;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.application.port.FoodDensityEstimator;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.ConversionFactors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public final class EstimateFoodDensity {

  static final int CALLS_PER_MINUTE = 10;
  static final BigDecimal LIGHTEST = new BigDecimal("0.2");
  static final BigDecimal HEAVIEST = new BigDecimal("2.5");
  static final BigDecimal WATER = BigDecimal.ONE;

  private final GetHousehold households;
  private final FoodCatalogRepository catalog;
  private final FoodDensityEstimator estimator;
  private final AiRateLimiter limiter;

  public EstimateFoodDensity(
      HouseholdRepository households,
      FoodCatalogRepository catalog,
      FoodDensityEstimator estimator,
      AiRateLimiter limiter) {
    this.households = new GetHousehold(Objects.requireNonNull(households, "households"));
    this.catalog = Objects.requireNonNull(catalog, "catalog");
    this.estimator = Objects.requireNonNull(estimator, "estimator");
    this.limiter = Objects.requireNonNull(limiter, "limiter");
  }

  public DensityEstimate estimate(UserId actor, HouseholdId household, String food) {
    households.get(actor, household);
    String wanted = food == null ? "" : food.strip();
    if (wanted.isEmpty() || wanted.length() > LearnFood.MAX_NAME) {
      throw new IllegalArgumentException(
          "Food names have 1 to " + LearnFood.MAX_NAME + " characters");
    }
    FoodMetadata known =
        catalog.findByName(wanted).orElseThrow(() -> new FoodNotInCatalogException(wanted));
    Optional<BigDecimal> density = known.conversion().density();
    if (density.isPresent()) {
      return new DensityEstimate(known.name(), density.get(), DensitySource.CATALOG);
    }
    if (!limiter.tryAcquire("density:" + actor.value(), CALLS_PER_MINUTE, Duration.ofMinutes(1))) {
      throw new AiRateLimitExceededException();
    }
    Optional<BigDecimal> estimated = plausible(known.name());
    if (estimated.isEmpty()) {
      return new DensityEstimate(known.name(), WATER, DensitySource.DEFAULT);
    }
    catalog.save(
        new FoodMetadata(
            known.name(),
            known.category(),
            known.defaultUnit(),
            new ConversionFactors(estimated.get(), known.conversion().gramsPerPiece()),
            known.perishable(),
            known.shelfDays(),
            known.allergens()));
    return new DensityEstimate(known.name(), estimated.get(), DensitySource.AI);
  }

  private Optional<BigDecimal> plausible(String food) {
    Optional<BigDecimal> answer;
    try {
      answer = estimator.gramsPerMilliliter(food);
    } catch (FoodCheckUnavailableException unavailable) {
      return Optional.empty();
    }
    return answer
        .map(value -> value.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros())
        .filter(value -> value.compareTo(LIGHTEST) >= 0 && value.compareTo(HEAVIEST) <= 0);
  }
}
