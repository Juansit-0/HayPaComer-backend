package dev.haypacomer.notifications;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import dev.haypacomer.application.mail.EmailMessage;
import dev.haypacomer.domain.identity.EmailAddress;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class BrevoEmailSenderTest {

  private static final EmailMessage RESET =
      new EmailMessage(
          new EmailAddress("ana@example.com"),
          "Reset your \"HayPaComer\" password",
          "Use this link: https://haypacomer.dev/#/reset-password?token=abc",
          "https://haypacomer.dev/#/reset-password?token=abc");

  private HttpServer server;
  private final AtomicReference<String> key = new AtomicReference<>();
  private final AtomicReference<String> body = new AtomicReference<>();
  private volatile int status = 201;

  @BeforeEach
  void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/v3/smtp/email",
        exchange -> {
          key.set(exchange.getRequestHeaders().getFirst("api-key"));
          body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          exchange.sendResponseHeaders(status, -1);
          exchange.close();
        });
    server.start();
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  private BrevoEmailSender brevo() {
    return new BrevoEmailSender(
        URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email"),
        "xkeysib-test",
        "hola@example.com",
        "HayPaComer",
        Duration.ofSeconds(2));
  }

  @Test
  void sendsTheMessageAsJsonWithTheKeyInAHeader() {
    brevo().send(RESET);

    JsonNode sent = JsonMapper.builder().build().readTree(body.get());
    assertEquals("xkeysib-test", key.get());
    assertEquals("hola@example.com", sent.get("sender").get("email").asString());
    assertEquals("ana@example.com", sent.get("to").get(0).get("email").asString());
    assertEquals(RESET.subject(), sent.get("subject").asString());
    assertEquals(RESET.body(), sent.get("textContent").asString());
  }

  @Test
  void aRejectedOrUnreachableSendDoesNotThrow() {
    status = 401;
    assertDoesNotThrow(() -> brevo().send(RESET));
    server.stop(0);
    assertDoesNotThrow(() -> brevo().send(RESET));
  }

  @Test
  void needsAKeyAndASender() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new BrevoEmailSender(BrevoEmailSender.API, " ", "a@b.co", null, Duration.ZERO));
    assertThrows(
        IllegalArgumentException.class,
        () -> new BrevoEmailSender(BrevoEmailSender.API, "key", "", null, Duration.ZERO));
  }
}
