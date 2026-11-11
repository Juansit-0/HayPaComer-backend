package dev.haypacomer.web.mail;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.haypacomer.notifications.BrevoEmailSender;
import dev.haypacomer.notifications.LogEmailSender;
import org.junit.jupiter.api.Test;

class MailConfigurationTest {

  @Test
  void anIncompleteSetupKeepsTheAppUpAndLogsEmail() {
    assertInstanceOf(LogEmailSender.class, MailConfiguration.delivery("", false, "", "", ""));
    assertInstanceOf(
        LogEmailSender.class, MailConfiguration.delivery("smtp", false, "k", "a@b.co", ""));
    assertInstanceOf(
        LogEmailSender.class, MailConfiguration.delivery("brevo", false, "", "a@b.co", ""));
    assertInstanceOf(
        LogEmailSender.class, MailConfiguration.delivery("brevo", false, "k", " ", ""));
  }

  @Test
  void brevoIsChosenWhateverTheCaseOrSpaces() {
    assertInstanceOf(
        BrevoEmailSender.class, MailConfiguration.delivery(" Brevo ", false, " k ", "a@b.co", ""));
  }
}
