package dev.haypacomer.ai.offline;

import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.ai.FoodVerdict;
import dev.haypacomer.application.port.FoodClassifier;

public final class OfflineFoodClassifier implements FoodClassifier {

  @Override
  public String provider() {
    return "offline";
  }

  @Override
  public FoodVerdict classify(String name) {
    throw new FoodCheckUnavailableException(
        "Without the AI provider only catalog foods can be added");
  }
}
