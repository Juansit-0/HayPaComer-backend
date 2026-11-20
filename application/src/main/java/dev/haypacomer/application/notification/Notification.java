package dev.haypacomer.application.notification;

import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A message for a home; with a recipient it reaches only that person (an answer to their request).
 */
public record Notification(
    UUID id,
    HouseholdId household,
    NotificationType type,
    String title,
    String body,
    Instant at,
    UserId recipient) {

  public Notification(
      UUID id,
      HouseholdId household,
      NotificationType type,
      String title,
      String body,
      Instant at) {
    this(id, household, type, title, body, at, null);
  }

  public Notification {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(household, "household");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(body, "body");
    Objects.requireNonNull(at, "at");
  }

  public static Notification of(
      HouseholdId household, NotificationType type, String title, String body, Instant at) {
    return new Notification(UUID.randomUUID(), household, type, title, body, at);
  }

  public static Notification to(
      UserId recipient,
      HouseholdId household,
      NotificationType type,
      String title,
      String body,
      Instant at) {
    Objects.requireNonNull(recipient, "recipient");
    return new Notification(UUID.randomUUID(), household, type, title, body, at, recipient);
  }
}
