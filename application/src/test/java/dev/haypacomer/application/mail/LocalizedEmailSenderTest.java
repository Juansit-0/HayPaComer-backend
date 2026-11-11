package dev.haypacomer.application.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haypacomer.application.i18n.LocaleOption;
import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.port.TranslationRepository;
import dev.haypacomer.application.port.UserLocaleRepository;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.identity.EmailAddress;
import dev.haypacomer.domain.identity.PasswordHash;
import dev.haypacomer.domain.identity.User;
import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LocalizedEmailSenderTest {

  private static final String LINK = "https://haypacomer.dev/#/join?token=abc";

  private final User ana =
      User.register(
          new EmailAddress("ana@example.com"), new PasswordHash("x"), "Ana", Instant.EPOCH);
  private final Map<UserId, String> saved = new HashMap<>();
  private final List<EmailMessage> sent = new ArrayList<>();

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
              ? Map.of(
                  "You are invited to {} on HayPaComer", "Te invitaron a {} en HayPaComer",
                  "Join {} within 7 days: {}", "Únete a {} en los próximos 7 días: {}")
              : Map.of();
        }
      };

  private final UserRepository users =
      new UserRepository() {
        @Override
        public void save(User user) {}

        @Override
        public Optional<User> findById(UserId id) {
          return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(EmailAddress email) {
          return Optional.of(ana).filter(user -> user.email().equals(email));
        }
      };

  private final LocalizedEmailSender sender =
      new LocalizedEmailSender(
          sent::add,
          users,
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

  private static EmailMessage invitation(String to) {
    return new EmailMessage(
        new EmailAddress(to),
        "You are invited to Casa López on HayPaComer",
        "Join Casa López within 7 days: " + LINK,
        LINK);
  }

  @Test
  void someoneWithoutASavedLanguageGetsTheDefaultOne() {
    sender.send(invitation("nuevo@example.com"));

    assertEquals("Te invitaron a Casa López en HayPaComer", sent.get(0).subject());
    assertEquals("Únete a Casa López en los próximos 7 días: " + LINK, sent.get(0).body());
    assertEquals(LINK, sent.get(0).link());
  }

  @Test
  void anAccountGetsTheLanguageItSaved() {
    saved.put(ana.id(), "en");

    sender.send(invitation("ana@example.com"));

    assertEquals("You are invited to Casa López on HayPaComer", sent.get(0).subject());
  }
}
