package dev.haypacomer.application.inventory;

public final class AccessRequestNotFoundException extends RuntimeException {

  public AccessRequestNotFoundException() {
    super("Food request not found");
  }
}
