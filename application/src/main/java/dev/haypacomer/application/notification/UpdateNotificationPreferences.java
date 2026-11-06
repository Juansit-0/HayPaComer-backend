package dev.haypacomer.application.notification;

import dev.haypacomer.application.port.NotificationPreferenceRepository;
import dev.haypacomer.domain.identity.UserId;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class UpdateNotificationPreferences {

  private final NotificationPreferenceRepository preferences;

  public UpdateNotificationPreferences(NotificationPreferenceRepository preferences) {
    this.preferences = Objects.requireNonNull(preferences, "preferences");
  }

  public NotificationPreferences update(
      UserId actor, Set<ChannelKind> channels, String telegramChatId) {
    // The inbox stays on: a cold chain alert must reach someone even with every other channel off.
    Set<ChannelKind> kept = EnumSet.of(ChannelKind.WEB);
    kept.addAll(channels);
    NotificationPreferences updated = new NotificationPreferences(actor, kept, telegramChatId);
    preferences.save(updated);
    return updated;
  }
}
