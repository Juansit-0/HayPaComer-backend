package dev.haypacomer.application.port;

import java.math.BigDecimal;
import java.util.Optional;

public interface FoodDensityEstimator {

  String provider();

  Optional<BigDecimal> gramsPerMilliliter(String foodName);
}
