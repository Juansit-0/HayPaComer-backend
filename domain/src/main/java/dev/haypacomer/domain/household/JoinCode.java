package dev.haypacomer.domain.household;

import dev.haypacomer.domain.identity.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * A short code that lets anyone who has it join a household and share its fridge, without an email
 * invitation. One code per household, kept for as long as the home exists; making a new one
 * replaces the old.
 */
public record JoinCode(
    HouseholdId household,
    String code,
    Role role,
    UserId createdBy,
    Instant createdAt,
    Instant expiresAt) {

  public static final Duration TIME_TO_LIVE = Duration.ofDays(3650);
  public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  public static final int LENGTH = 8;

  public JoinCode {
    Objects.requireNonNull(household, "household");
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(createdBy, "createdBy");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(expiresAt, "expiresAt");
    if (role == Role.OWNER) {
      throw new IllegalArgumentException("Join codes cannot grant ownership");
    }
    if (normalize(code).length() != LENGTH) {
      throw new IllegalArgumentException("A join code has " + LENGTH + " characters");
    }
  }

  public static JoinCode create(
      HouseholdId household, Role role, UserId createdBy, Instant now, RandomGenerator random) {
    StringBuilder code = new StringBuilder(LENGTH);
    for (int index = 0; index < LENGTH; index++) {
      code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return new JoinCode(household, code.toString(), role, createdBy, now, now.plus(TIME_TO_LIVE));
  }

  /**
   * What people type: any case, with or without dashes or spaces. The alphabet leaves out O, 0, I
   * and 1, so a code read aloud is never ambiguous.
   */
  public static String normalize(String typed) {
    if (typed == null) {
      return "";
    }
    return typed.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
  }

  /** The code as shown: two groups of four, easy to read aloud. */
  public String display() {
    return code.substring(0, LENGTH / 2) + "-" + code.substring(LENGTH / 2);
  }

  public boolean isActive(Instant now) {
    return now.isBefore(expiresAt);
  }
}
