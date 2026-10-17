package dev.haypacomer.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.domain.identity.EmailAddress;
import dev.haypacomer.domain.identity.User;
import org.junit.jupiter.api.Test;

class PasswordProofTest {

  private static final String EMAIL = "juan@haypacomer.dev";
  private static final String PASSWORD = "fresh-milk-842";

  private final AuthFakes fakes = new AuthFakes();
  private final String proof = fakes.hasher.proof(EMAIL, PASSWORD);

  private User stored() {
    return fakes.users.findByEmail(new EmailAddress(EMAIL)).orElseThrow();
  }

  @Test
  void registersWithAProofAndSignsInWithTheProofOrThePassword() {
    fakes.registerUser().register(new RegisterCommand(EMAIL, null, proof, "Juan"));

    assertTrue(stored().passwordHash().value().startsWith("{proof}"));
    assertEquals(stored().id(), fakes.logIn().logIn(new LoginCommand(EMAIL, null, proof)).user());
    assertEquals(stored().id(), fakes.logIn().logIn(new LoginCommand(EMAIL, PASSWORD)).user());
  }

  @Test
  void rejectsAMalformedProofOnRegistration() {
    assertThrows(
        InvalidPasswordException.class,
        () -> fakes.registerUser().register(new RegisterCommand(EMAIL, null, "not-hex", "Juan")));
  }

  @Test
  void upgradesALegacyAccountOnTheFirstSignInWithItsPassword() {
    fakes.users.save(
        User.register(
            new EmailAddress(EMAIL), fakes.hasher.hash(PASSWORD), "Juan", fakes.clock.instant()));

    assertThrows(
        InvalidCredentialsException.class,
        () -> fakes.logIn().logIn(new LoginCommand(EMAIL, null, proof)));
    fakes.logIn().logIn(new LoginCommand(EMAIL, PASSWORD));

    assertTrue(stored().passwordHash().value().startsWith("{proof}"));
    assertEquals(stored().id(), fakes.logIn().logIn(new LoginCommand(EMAIL, null, proof)).user());
  }

  @Test
  void aWrongProofOrPasswordNeverSignsIn() {
    fakes.registerUser().register(new RegisterCommand(EMAIL, PASSWORD, "Juan"));
    String other = fakes.hasher.proof(EMAIL, "other-password-1");

    assertThrows(
        InvalidCredentialsException.class,
        () -> fakes.logIn().logIn(new LoginCommand(EMAIL, null, other)));
    assertThrows(
        InvalidCredentialsException.class,
        () -> fakes.logIn().logIn(new LoginCommand(EMAIL, "other-password-1")));
    assertThrows(
        InvalidCredentialsException.class,
        () -> fakes.logIn().logIn(new LoginCommand(EMAIL, null, "ZZ")));
    assertFalse(stored().passwordHash().value().contains(PASSWORD));
  }
}
