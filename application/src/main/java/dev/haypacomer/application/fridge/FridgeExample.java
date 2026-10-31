package dev.haypacomer.application.fridge;

import java.util.Objects;

public record FridgeExample(String key, FridgeShape shape) {

  public FridgeExample {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(shape, "shape");
  }
}
