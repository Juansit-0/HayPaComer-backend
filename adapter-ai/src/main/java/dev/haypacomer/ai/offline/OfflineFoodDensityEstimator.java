package dev.haypacomer.ai.offline;

import dev.haypacomer.application.port.FoodDensityEstimator;
import java.math.BigDecimal;
import java.util.Optional;

public final class OfflineFoodDensityEstimator implements FoodDensityEstimator {

  @Override
  public String provider() {
    return "offline";
  }

  @Override
  public Optional<BigDecimal> gramsPerMilliliter(String foodName) {
    return Optional.empty();
  }
}
