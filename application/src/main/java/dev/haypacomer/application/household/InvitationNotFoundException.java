package dev.haypacomer.application.household;

public final class InvitationNotFoundException extends RuntimeException {

  public InvitationNotFoundException() {
    super("Invitation not found");
  }
}
