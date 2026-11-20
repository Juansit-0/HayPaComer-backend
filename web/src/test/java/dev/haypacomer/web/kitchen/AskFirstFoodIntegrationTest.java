package dev.haypacomer.web.kitchen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Food marked "ask first": a member asks, the owner allows, the member uses it. */
@SpringBootTest(
    properties = "haypacomer.security.jwt.secret=integration-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AskFirstFoodIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

  @Autowired private MockMvc mvc;

  private static final String MILK_EXPIRES = java.time.LocalDate.now().plusDays(4).toString();

  private String signUp(String email) throws Exception {
    mvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\""
                        + email
                        + "\",\"password\":\"fresh-milk-842\",\"displayName\":\"x\"}"))
        .andExpect(status().isCreated());
    String login =
        mvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"fresh-milk-842\"}"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return "Bearer " + JsonPath.read(login, "$.accessToken");
  }

  private String send(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
      String bearer,
      String body,
      int expected)
      throws Exception {
    if (body != null) {
      request = request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
    return mvc.perform(request.header("Authorization", bearer))
        .andExpect(status().is(expected))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private String home(String bearer, String name) throws Exception {
    return JsonPath.read(
        send(
            post("/api/v1/households"),
            bearer,
            "{\"name\":\"" + name + "\",\"currency\":\"COP\",\"timezone\":\"America/Bogota\"}",
            201),
        "$.id");
  }

  private String[] fridge(String bearer, String household) throws Exception {
    String created =
        send(
            post("/api/v1/households/" + household + "/fridges"),
            bearer,
            "{\"name\":\"Cocina\",\"layout\":\"STANDARD\"}",
            201);
    String tray = JsonPath.read(created, "$.children[0].children[0].id");
    return new String[] {JsonPath.read(created, "$.id"), tray};
  }

  private void stock(String bearer, String household, String[] fridge, String body)
      throws Exception {
    send(
        post("/api/v1/households/" + household + "/items"),
        bearer,
        "{\"fridgeId\":\"" + fridge[0] + "\",\"trayId\":\"" + fridge[1] + "\"," + body + "}",
        201);
  }

  private List<Map<String, Object>> inventory(String bearer, String household) throws Exception {
    return JsonPath.read(
        send(get("/api/v1/households/" + household + "/inventory"), bearer, null, 200), "$");
  }

  private String code(String bearer, String household) throws Exception {
    return JsonPath.read(
        send(get("/api/v1/households/" + household + "/join-code"), bearer, null, 200), "$.code");
  }

  @Test
  void anAllowedMemberUsesAskFirstFood() throws Exception {
    String ana = signUp("ana@askfirst.test");
    String beto = signUp("beto@askfirst.test");
    String household = home(ana, "Casa");
    String[] cocina = fridge(ana, household);
    stock(ana, household, cocina, "\"food\":\"Cheese\",\"grams\":250,\"visibility\":\"ASK_FIRST\"");
    send(post("/api/v1/households/join"), beto, "{\"code\":\"" + code(ana, household) + "\"}", 200);
    String base = "/api/v1/households/" + household;

    Map<String, Object> cheese = inventory(beto, household).getFirst();
    assertEquals(false, cheese.get("usable"));
    String item = (String) cheese.get("id");
    String request =
        JsonPath.read(
            send(post(base + "/items/" + item + "/access-requests"), beto, null, 201), "$.id");

    List<Map<String, Object>> toAnswer =
        JsonPath.read(send(get(base + "/access-requests"), ana, null, 200), "$");
    assertEquals(true, toAnswer.getFirst().get("toAnswer"));
    String answered = send(post(base + "/access-requests/" + request + "/approve"), ana, null, 200);
    assertEquals("APPROVED", JsonPath.read(answered, "$.status"));
    send(get(base + "/access-requests"), ana, null, 200);
    inventory(ana, household);

    assertEquals(true, inventory(beto, household).getFirst().get("usable"));
    assertEquals(
        "APPROVED",
        JsonPath.read(send(get(base + "/access-requests"), beto, null, 200), "$[0].status"));
    send(post(base + "/items/" + item + "/consume"), beto, "{\"grams\":50}", 200);
    assertEquals(200.0, ((Number) inventory(ana, household).getFirst().get("grams")).doubleValue());
  }
}
