package dev.haypacomer.notifications;

import dev.haypacomer.application.i18n.MessageTranslator;
import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.notification.ChannelKind;
import dev.haypacomer.application.notification.Notification;
import dev.haypacomer.application.notification.NotificationPreferences;
import dev.haypacomer.application.port.NotificationChannel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class LocalizedNotificationChannel implements NotificationChannel {

  private final NotificationChannel delivery;
  private final ResolveLocale locales;
  private final TranslateMessages translations;

  public LocalizedNotificationChannel(
      NotificationChannel delivery, ResolveLocale locales, TranslateMessages translations) {
    this.delivery = Objects.requireNonNull(delivery, "delivery");
    this.locales = Objects.requireNonNull(locales, "locales");
    this.translations = Objects.requireNonNull(translations, "translations");
  }

  @Override
  public ChannelKind kind() {
    return delivery.kind();
  }

  @Override
  public void deliver(Notification notification, NotificationPreferences recipient) {
    MessageTranslator translator =
        translations.into(locales.resolve(Optional.of(recipient.user()), List.of()));
    delivery.deliver(
        new Notification(
            notification.id(),
            notification.household(),
            notification.type(),
            translator.translate(notification.title()),
            translator.translate(notification.body()),
            notification.at()),
        recipient);
  }
}
