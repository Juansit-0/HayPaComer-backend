package dev.haypacomer.application.household;

public final class JoinCodeInvalidException extends RuntimeException {

  public JoinCodeInvalidException() {
    super("That household code is not valid or has expired");
  }
}
