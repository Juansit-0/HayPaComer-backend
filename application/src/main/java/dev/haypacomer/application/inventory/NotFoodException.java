package dev.haypacomer.application.inventory;

public class NotFoodException extends RuntimeException {

  private final String name;

  public NotFoodException(String name, String reason) {
    super(reason);
    this.name = name;
  }

  public String name() {
    return name;
  }
}
