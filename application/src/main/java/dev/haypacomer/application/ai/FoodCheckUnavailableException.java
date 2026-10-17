package dev.haypacomer.application.ai;

public class FoodCheckUnavailableException extends RuntimeException {

  private final boolean providerDown;

  public FoodCheckUnavailableException(String message) {
    this(message, false);
  }

  private FoodCheckUnavailableException(String message, boolean providerDown) {
    super(message);
    this.providerDown = providerDown;
  }

  public static FoodCheckUnavailableException providerDown(String message) {
    return new FoodCheckUnavailableException(message, true);
  }

  public boolean isProviderDown() {
    return providerDown;
  }
}
