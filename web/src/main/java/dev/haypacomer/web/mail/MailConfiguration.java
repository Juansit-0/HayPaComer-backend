package dev.haypacomer.web.mail;

import dev.haypacomer.application.i18n.ResolveLocale;
import dev.haypacomer.application.i18n.TranslateMessages;
import dev.haypacomer.application.mail.LocalizedEmailSender;
import dev.haypacomer.application.mail.MailLinks;
import dev.haypacomer.application.port.EmailSender;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.notifications.BrevoEmailSender;
import dev.haypacomer.notifications.LogEmailSender;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {

  private static final Logger LOG = System.getLogger(MailConfiguration.class.getName());

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
    return new LocalizedEmailSender(
        delivery(provider, logLinks, apiKey, from, fromName), users, locales, translations);
  }

  static EmailSender delivery(
      String provider, boolean logLinks, String apiKey, String from, String fromName) {
    String chosen = provider == null ? "" : provider.strip().toLowerCase(Locale.ROOT);
    if (chosen.isEmpty() || chosen.equals("log")) {
      return new LogEmailSender(logLinks);
    }
    if (!chosen.equals("brevo")) {
      LOG.log(Level.WARNING, "Unknown MAIL_PROVIDER \"{0}\"; email only goes to the log", chosen);
      return new LogEmailSender(logLinks);
    }
    if (apiKey.isBlank() || from.isBlank()) {
      LOG.log(Level.WARNING, "Brevo needs BREVO_API_KEY and MAIL_FROM; email only goes to the log");
      return new LogEmailSender(logLinks);
    }
    return new BrevoEmailSender(
        BrevoEmailSender.API,
        apiKey.strip(),
        from.strip(),
        fromName.isBlank() ? "HayPaComer" : fromName.strip(),
        Duration.ofSeconds(10));
  }
}
