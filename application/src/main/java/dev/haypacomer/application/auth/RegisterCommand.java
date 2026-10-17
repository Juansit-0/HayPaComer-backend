package dev.haypacomer.application.auth;

public record RegisterCommand(
    String email, String password, String passwordProof, String displayName) {

  public RegisterCommand(String email, String password, String displayName) {
    this(email, password, null, displayName);
  }

  @Override
  public String toString() {
    return "RegisterCommand[email=" + email + ", displayName=" + displayName + "]";
  }
}
