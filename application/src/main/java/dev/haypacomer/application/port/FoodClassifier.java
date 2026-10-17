package dev.haypacomer.application.port;

import dev.haypacomer.application.ai.FoodVerdict;

public interface FoodClassifier {

  String provider();

  FoodVerdict classify(String name);
}
