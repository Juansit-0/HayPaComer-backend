package dev.haypacomer.web.agent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(
    properties = "haypacomer.security.jwt.secret=integration-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ChefChatIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

  @Container
  @ServiceConnection(name = "redis")
  static final GenericContainer<?> REDIS =
      new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

  @Autowired private MockMvc mvc;

  private ResultActions send(String path, String bearer, String body) throws Exception {
    var request = post(path).contentType(MediaType.APPLICATION_JSON).content(body);
    if (bearer != null) {
      request.header("Authorization", bearer);
    }
    return mvc.perform(request);
  }

  private static String body(ResultActions result) throws Exception {
    return result.andReturn().getResponse().getContentAsString();
  }

  private String signUp(String email) throws Exception {
    send(
            "/api/v1/auth/register",
            null,
            "{\"email\":\"" + email + "\",\"password\":\"fresh-milk-842\",\"displayName\":\"J\"}")
        .andExpect(status().isCreated());
    return "Bearer "
        + JsonPath.read(
            body(
                send(
                    "/api/v1/auth/login",
                    null,
                    "{\"email\":\"" + email + "\",\"password\":\"fresh-milk-842\"}")),
            "$.accessToken");
  }

  @Test
  void theChefAnswersFromTheRealFridgeAndKeepsTheConversation() throws Exception {
    String juan = signUp("chat@haypacomer.dev");
    String stranger = signUp("chat-stranger@haypacomer.dev");
    String base =
        "/api/v1/households/"
            + JsonPath.read(
                body(
                    send(
                        "/api/v1/households",
                        juan,
                        "{\"name\":\"Apartment\",\"currency\":\"COP\",\"timezone\":\"UTC\"}")),
                "$.id");
    String fridge = body(send(base + "/fridges", juan, "{\"name\":\"Kitchen\"}"));
    String tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();
    send(
            base + "/items",
            juan,
            "{\"fridgeId\":\""
                + JsonPath.read(fridge, "$.id")
                + "\",\"trayId\":\""
                + JsonPath.read(fridge, "$.children[0].children[0].id")
                + "\",\"food\":\"Chicken breast\",\"grams\":650,\"expiresOn\":\""
                + tomorrow
                + "\"}")
        .andExpect(status().isCreated());

    String first =
        body(
            send(base + "/agent/chat", juan, "{\"message\":\"Que cocino esta noche?\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runs[0].specialist").value("chef"))
                .andExpect(jsonPath("$.evidence[0].tool").value("suggest_dishes"))
                .andExpect(
                    jsonPath("$.evidence[0].content")
                        .value(org.hamcrest.Matchers.containsString("No recipes are saved yet"))));
    String conversation = JsonPath.read(first, "$.conversationId");

    send(
            base + "/agent/chat",
            juan,
            "{\"message\":\"Y que compro?\",\"conversationId\":\"" + conversation + "\"}")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conversationId").value(conversation))
        .andExpect(jsonPath("$.runs[0].specialist").value("market"));

    mvc.perform(get(base + "/agent/conversations").header("Authorization", juan))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(conversation));
    mvc.perform(get(base + "/agent/conversations/" + conversation).header("Authorization", juan))
        .andExpect(jsonPath("$.length()").value(4))
        .andExpect(jsonPath("$[0].role").value("USER"))
        .andExpect(jsonPath("$[1].role").value("ASSISTANT"));
    mvc.perform(
            get(base + "/agent/conversations/" + conversation).header("Authorization", stranger))
        .andExpect(status().isNotFound());
    send(base + "/agent/chat", stranger, "{\"message\":\"Peek\"}").andExpect(status().isNotFound());
    send(base + "/agent/chat", juan, "{\"message\":\"\"}").andExpect(status().isBadRequest());
  }

  @Test
  void theChefAnswersInSpanishWhenTheCallerAsksForIt() throws Exception {
    String juan = signUp("chat-es@haypacomer.dev");
    String base =
        "/api/v1/households/"
            + JsonPath.read(
                body(
                    send(
                        "/api/v1/households",
                        juan,
                        "{\"name\":\"Casa\",\"currency\":\"COP\",\"timezone\":\"UTC\"}")),
                "$.id");
    String fridge = body(send(base + "/fridges", juan, "{\"name\":\"Kitchen\"}"));
    send(
            base + "/items",
            juan,
            "{\"fridgeId\":\""
                + JsonPath.read(fridge, "$.id")
                + "\",\"trayId\":\""
                + JsonPath.read(fridge, "$.children[0].children[0].id")
                + "\",\"food\":\"Chicken breast\",\"grams\":650}")
        .andExpect(status().isCreated());
    String body = "{\"message\":\"I used 200 g of chicken breast\",\"specialist\":\"chef\"}";

    send(base + "/agent/chat", juan, body)
        .andExpect(
            jsonPath("$.answer")
                .value(
                    org.hamcrest.Matchers.startsWith(
                        "Waiting for your confirmation: Use 200 g of Chicken breast")));
    String spanish =
        body(
            mvc.perform(
                    post(base + "/agent/chat")
                        .header("Authorization", juan)
                        .header("Accept-Language", "es-CO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.answer")
                        .value(
                            org.hamcrest.Matchers.startsWith(
                                "Esperando tu confirmación: Usar 200 g de pechuga de pollo")))
                .andExpect(
                    jsonPath("$.confirmation.summary")
                        .value(
                            org.hamcrest.Matchers.startsWith("Usar 200 g de pechuga de pollo"))));

    mvc.perform(
            post("/api/v1/agent/confirmations/"
                    + JsonPath.read(spanish, "$.confirmation.id")
                    + "/approve")
                .header("Authorization", juan)
                .header("Accept-Language", "es-CO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("Usaste 200 g de pechuga de pollo; quedan 450 g"));

    send(
            base + "/items",
            juan,
            "{\"fridgeId\":\""
                + JsonPath.read(fridge, "$.id")
                + "\",\"trayId\":\""
                + JsonPath.read(fridge, "$.children[0].children[0].id")
                + "\",\"food\":\"Rice\",\"grams\":100,\"visibility\":\"NOPE\"}")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.visibility").value("has an invalid value"));
    mvc.perform(get(base + "/expiry-estimate").header("Authorization", juan))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.food").value("is required"));
  }
}
