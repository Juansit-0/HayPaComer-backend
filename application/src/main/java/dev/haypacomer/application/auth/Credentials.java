package dev.haypacomer.application.auth;

import dev.haypacomer.application.port.PasswordHasher;
import dev.haypacomer.domain.identity.PasswordHash;

final class Credentials {

  private static final String PROOF = "{proof}";

  private Credentials() {}

  static String proof(
      PasswordHasher hasher, String email, String password, String proof, AuthSettings settings) {
    if (proof != null) {
      return PasswordProof.require(proof);
    }
    PasswordPolicy.requireAcceptable(password, settings);
    return hasher.proof(email, password);
  }

  static PasswordHash store(PasswordHasher hasher, String proof) {
    return new PasswordHash(PROOF + hasher.hash(proof).value());
  }

  static boolean legacy(PasswordHash hash) {
    return !hash.value().startsWith(PROOF);
  }

  static boolean matches(PasswordHasher hasher, String proof, PasswordHash hash) {
    return !legacy(hash)
        && hasher.matches(proof, new PasswordHash(hash.value().substring(PROOF.length())));
  }
}
