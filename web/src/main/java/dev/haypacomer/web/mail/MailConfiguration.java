package dev.haypacomer.web.mail;

import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.mail.LocalizedEmailSender;
import dev.haypacomer.application.mail.MailLinks;
import dev.haypacomer.application.port.EmailSender;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.notifications.BrevoEmailSender;
import dev.haypacomer.notifications.LogEmailSender;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {

  @Bean
  MailLinks mailLinks(@Value("${haypacomer.mail.base-url}") String baseUrl) {
    return new MailLinks(baseUrl);
  }

  @Bean
  EmailSender emailSender(
      UserRepository users,
      ResolveLocale locales,
      TranslateMessages translations,
      @Value("${haypacomer.mail.provider:log}") String provider,
      @Value("${haypacomer.mail.log-links:false}") boolean logLinks,
      @Value("${haypacomer.mail.brevo.api-key:}") String apiKey,
      @Value("${haypacomer.mail.from:}") String from,
      @Value("${haypacomer.mail.from-name:HayPaComer}") String fromName) {
    EmailSender delivery =
        switch (provider) {
          case "log" -> new LogEmailSender(logLinks);
          case "brevo" ->
              new BrevoEmailSender(
                  BrevoEmailSender.API, apiKey, from, fromName, Duration.ofSeconds(10));
          default -> throw new IllegalStateException("Unknown MAIL_PROVIDER: " + provider);
        };
    return new LocalizedEmailSender(delivery, users, locales, translations);
  }
}
