package dev.haypacomer.notifications;

import dev.haypacomer.application.mail.EmailMessage;
import dev.haypacomer.application.port.EmailSender;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

public final class BrevoEmailSender implements EmailSender {

  public static final URI API = URI.create("https://api.brevo.com/v3/smtp/email");

  private static final Logger LOG = System.getLogger(BrevoEmailSender.class.getName());
  private static final JsonMapper JSON = JsonMapper.builder().build();

  private final URI api;
  private final String apiKey;
  private final Map<String, String> sender;
  private final HttpClient http;
  private final Duration timeout;

  public BrevoEmailSender(URI api, String apiKey, String from, String fromName, Duration timeout) {
    this.api = Objects.requireNonNull(api, "api");
    this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
    this.timeout = Objects.requireNonNull(timeout, "timeout");
    if (apiKey.isBlank() || from == null || from.isBlank()) {
      throw new IllegalArgumentException("Brevo needs an API key and a sender address");
    }
    this.sender = Map.of("email", from, "name", fromName == null ? "HayPaComer" : fromName);
    this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
  }

  @Override
  public void send(EmailMessage message) {
    String body =
        JSON.writeValueAsString(
            Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", message.to().value())),
                "subject", message.subject(),
                "textContent", message.body()));
    HttpRequest request =
        HttpRequest.newBuilder(api)
            .timeout(timeout)
            .header("api-key", apiKey)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    try {
      int status = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
      if (status >= 300) {
        LOG.log(Level.WARNING, "Brevo rejected the email \"{0}\": {1}", message.subject(), status);
      }
    } catch (IOException failure) {
      LOG.log(Level.WARNING, "Brevo is unreachable for the email \"{0}\"", message.subject());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
