package dev.haypacomer.application.auth;

import java.util.regex.Pattern;

final class PasswordProof {

  private static final Pattern FORMAT = Pattern.compile("[0-9a-f]{64}");

  private PasswordProof() {}

  static boolean valid(String proof) {
    return proof != null && FORMAT.matcher(proof).matches();
  }

  static String require(String proof) {
    if (!valid(proof)) {
      throw new InvalidPasswordException(
          "Password proof must be 64 lowercase hexadecimal characters");
    }
    return proof;
  }
}
