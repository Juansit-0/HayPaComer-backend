package dev.haypacomer.application.auth;

import dev.haypacomer.application.port.LoginAttemptLog;
import dev.haypacomer.application.port.PasswordHasher;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.identity.EmailAddress;
import dev.haypacomer.domain.identity.User;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class LogIn {

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final LoginAttemptLog attempts;
  private final SessionIssuer sessions;
  private final Clock clock;
  private final AuthSettings settings;

  public LogIn(
      UserRepository users,
      PasswordHasher hasher,
      LoginAttemptLog attempts,
      SessionIssuer sessions,
      Clock clock,
      AuthSettings settings) {
    this.users = Objects.requireNonNull(users, "users");
    this.hasher = Objects.requireNonNull(hasher, "hasher");
    this.attempts = Objects.requireNonNull(attempts, "attempts");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  public AuthTokens logIn(LoginCommand command) {
    Instant now = clock.instant();
    EmailAddress email = parse(command.email());
    if (attempts.failuresSince(email, now.minus(settings.lockoutWindow()))
        >= settings.maxFailedLogins()) {
      throw new TooManyLoginAttemptsException();
    }
    Optional<User> user = users.findByEmail(email);
    boolean valid = verify(user, email, command);
    attempts.record(email, valid, now);
    if (!valid) {
      throw new InvalidCredentialsException();
    }
    return sessions.issue(user.orElseThrow().id(), UUID.randomUUID(), now).tokens();
  }

  private boolean verify(Optional<User> user, EmailAddress email, LoginCommand command) {
    String password = command.password();
    String proof = proofOf(email, command);
    if (user.isEmpty() || proof == null) {
      hasher.hash(proof == null ? "" : proof);
      return false;
    }
    User found = user.get();
    if (!Credentials.legacy(found.passwordHash())) {
      return found.canSignIn() && Credentials.matches(hasher, proof, found.passwordHash());
    }
    if (password == null) {
      hasher.hash(proof);
      return false;
    }
    boolean valid = found.canSignIn() && hasher.matches(password, found.passwordHash());
    if (valid) {
      users.save(found.changePassword(Credentials.store(hasher, proof)));
    }
    return valid;
  }

  private String proofOf(EmailAddress email, LoginCommand command) {
    if (command.passwordProof() != null) {
      return PasswordProof.valid(command.passwordProof()) ? command.passwordProof() : null;
    }
    return command.password() == null || command.password().isEmpty()
        ? null
        : hasher.proof(email.value(), command.password());
  }

  private static EmailAddress parse(String email) {
    try {
      return new EmailAddress(email);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new InvalidCredentialsException();
    }
  }
}
