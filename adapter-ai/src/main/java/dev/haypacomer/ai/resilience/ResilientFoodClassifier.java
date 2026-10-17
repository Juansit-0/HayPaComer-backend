package dev.haypacomer.ai.resilience;

import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.ai.FoodVerdict;
import dev.haypacomer.application.port.FoodClassifier;
import java.util.Objects;

public final class ResilientFoodClassifier implements FoodClassifier {

  private final FoodClassifier provider;
  private final ProviderCircuit circuit;

  public ResilientFoodClassifier(FoodClassifier provider, ProviderCircuit circuit) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.circuit = Objects.requireNonNull(circuit, "circuit");
  }

  @Override
  public String provider() {
    return provider.provider();
  }

  @Override
  public FoodVerdict classify(String name) {
    return circuit.call(
        () -> provider.classify(name),
        () -> {
          throw new FoodCheckUnavailableException(ResilientRecipePhotoReader.RESTING);
        },
        failure ->
            failure instanceof FoodCheckUnavailableException unavailable
                && unavailable.isProviderDown());
  }
}
