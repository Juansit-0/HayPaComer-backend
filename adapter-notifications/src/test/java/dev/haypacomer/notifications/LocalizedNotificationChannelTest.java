package dev.haypacomer.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haypacomer.application.i18n.LocaleOption;
import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.notification.ChannelKind;
import dev.haypacomer.application.notification.Notification;
import dev.haypacomer.application.notification.NotificationPreferences;
import dev.haypacomer.application.notification.NotificationType;
import dev.haypacomer.application.port.NotificationChannel;
import dev.haypacomer.application.port.TranslationRepository;
import dev.haypacomer.application.port.UserLocaleRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LocalizedNotificationChannelTest {

  private final UserId ana = UserId.newId();
  private final Map<UserId, String> saved = new HashMap<>();
  private final List<Notification> sent = new ArrayList<>();

  private final TranslationRepository translations =
      new TranslationRepository() {
        @Override
        public List<LocaleOption> locales() {
          return List.of(
              new LocaleOption("es-CO", "Español", true), new LocaleOption("en", "English", false));
        }

        @Override
        public Map<String, String> texts(String locale) {
          return Map.of();
        }

        @Override
        public Map<String, String> templates(String locale) {
          return locale.equals("es-CO")
              ? Map.of("The door is open", "La puerta está abierta", "Close {}", "Cierra {}")
              : Map.of();
        }
      };

  private final NotificationChannel telegram =
      new NotificationChannel() {
        @Override
        public ChannelKind kind() {
          return ChannelKind.TELEGRAM;
        }

        @Override
        public void deliver(Notification notification, NotificationPreferences recipient) {
          sent.add(notification);
        }
      };

  private final LocalizedNotificationChannel channel =
      new LocalizedNotificationChannel(
          telegram,
          new ResolveLocale(
              translations,
              new UserLocaleRepository() {
                @Override
                public Optional<String> locale(UserId user) {
                  return Optional.ofNullable(saved.get(user));
                }

                @Override
                public void save(UserId user, String locale) {
                  saved.put(user, locale);
                }
              }),
          new TranslateMessages(translations));

  private void deliver() {
    channel.deliver(
        Notification.of(
            HouseholdId.newId(),
            NotificationType.values()[0],
            "The door is open",
            "Close Kitchen",
            Instant.EPOCH),
        new NotificationPreferences(ana, Set.of(ChannelKind.TELEGRAM), "42"));
  }

  @Test
  void theMessageArrivesInTheRecipientsLanguage() {
    deliver();

    assertEquals(ChannelKind.TELEGRAM, channel.kind());
    assertEquals("La puerta está abierta", sent.get(0).title());
    assertEquals("Cierra Kitchen", sent.get(0).body());
  }

  @Test
  void someoneWhoChoseEnglishKeepsEnglish() {
    saved.put(ana, "en");

    deliver();

    assertEquals("The door is open", sent.get(0).title());
  }
}
