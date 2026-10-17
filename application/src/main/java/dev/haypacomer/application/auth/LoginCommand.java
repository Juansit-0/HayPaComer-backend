package dev.haypacomer.application.auth;

public record LoginCommand(String email, String password, String passwordProof) {

  public LoginCommand(String email, String password) {
    this(email, password, null);
  }

  @Override
  public String toString() {
    return "LoginCommand[email=" + email + "]";
  }
}
