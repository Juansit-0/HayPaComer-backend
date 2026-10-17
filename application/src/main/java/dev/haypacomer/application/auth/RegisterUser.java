package dev.haypacomer.application.auth;

import dev.haypacomer.application.port.PasswordHasher;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.identity.EmailAddress;
import dev.haypacomer.domain.identity.User;
import java.time.Clock;
import java.util.Objects;

public final class RegisterUser {

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final Clock clock;
  private final AuthSettings settings;

  public RegisterUser(
      UserRepository users, PasswordHasher hasher, Clock clock, AuthSettings settings) {
    this.users = Objects.requireNonNull(users, "users");
    this.hasher = Objects.requireNonNull(hasher, "hasher");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  public User register(RegisterCommand command) {
    EmailAddress email = new EmailAddress(command.email());
    String proof =
        Credentials.proof(
            hasher, email.value(), command.password(), command.passwordProof(), settings);
    if (users.findByEmail(email).isPresent()) {
      throw new EmailAlreadyRegisteredException();
    }
    User user =
        User.register(
            email, Credentials.store(hasher, proof), command.displayName(), clock.instant());
    users.save(user);
    return user;
  }
}
