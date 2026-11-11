package dev.haypacomer.application.mail;

import dev.haypacomer.application.i18n.MessageTranslator;
import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.port.EmailSender;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.identity.User;
import java.util.List;
import java.util.Objects;

public final class LocalizedEmailSender implements EmailSender {

  private final EmailSender delivery;
  private final UserRepository users;
  private final ResolveLocale locales;
  private final TranslateMessages translations;

  public LocalizedEmailSender(
      EmailSender delivery,
      UserRepository users,
      ResolveLocale locales,
      TranslateMessages translations) {
    this.delivery = Objects.requireNonNull(delivery, "delivery");
    this.users = Objects.requireNonNull(users, "users");
    this.locales = Objects.requireNonNull(locales, "locales");
    this.translations = Objects.requireNonNull(translations, "translations");
  }

  @Override
  public void send(EmailMessage message) {
    String locale = locales.resolve(users.findByEmail(message.to()).map(User::id), List.of());
    MessageTranslator translator = translations.into(locale);
    delivery.send(
        new EmailMessage(
            message.to(),
            translator.translate(message.subject()),
            translator.translate(message.body()),
            message.link()));
  }
}
