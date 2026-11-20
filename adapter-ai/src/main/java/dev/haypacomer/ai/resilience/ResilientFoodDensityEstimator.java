package dev.haypacomer.ai.resilience;

import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.port.FoodDensityEstimator;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class ResilientFoodDensityEstimator implements FoodDensityEstimator {

  private final FoodDensityEstimator provider;
  private final ProviderCircuit circuit;

  public ResilientFoodDensityEstimator(FoodDensityEstimator provider, ProviderCircuit circuit) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.circuit = Objects.requireNonNull(circuit, "circuit");
  }

  @Override
  public String provider() {
    return provider.provider();
  }

  @Override
  public Optional<BigDecimal> gramsPerMilliliter(String foodName) {
    return circuit.call(
        () -> provider.gramsPerMilliliter(foodName),
        Optional::empty,
        failure ->
            failure instanceof FoodCheckUnavailableException unavailable
                && unavailable.isProviderDown());
  }
}
