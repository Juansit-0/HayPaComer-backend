package dev.haypacomer.web.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

/** Whoever moves from one home to another takes their own food to the new fridge. */
@SpringBootTest(
    properties = "haypacomer.security.jwt.secret=integration-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class MovingFoodIntegrationTest {

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
  void ownFoodFollowsItsOwnerToTheNextHome() throws Exception {
    String ana = signUp("ana@moving.test");
    String beto = signUp("beto@moving.test");
    String carla = signUp("carla@moving.test");

    // Ana's home, with two of her foods and one of Carla's.
    String first = home(ana, "Primera");
    String[] firstFridge = fridge(ana, first);
    stock(
        ana,
        first,
        firstFridge,
        "\"food\":\"Beer can\",\"grams\":720,\"pieceGrams\":360,\"visibility\":\"PRIVATE\"");
    stock(
        ana,
        first,
        firstFridge,
        "\"food\":\"Milk\",\"grams\":1000,\"expiresOn\":\""
            + MILK_EXPIRES
            + "\",\"expirySource\":\"USER\"");
    send(post("/api/v1/households/join"), carla, "{\"code\":\"" + code(ana, first) + "\"}", 200);
    stock(carla, first, firstFridge, "\"food\":\"Egg\",\"grams\":360");

    // Beto's home.
    String second = home(beto, "Segunda");
    String[] secondFridge = fridge(beto, second);
    stock(beto, second, secondFridge, "\"food\":\"Tomato\",\"grams\":500");
    String secondCode = code(beto, second);

    // Carla leaves Ana's home: her egg leaves with her, Ana's food stays.
    UUID carlaId =
        UUID.fromString(JsonPath.read(send(get("/api/v1/me"), carla, null, 200), "$.id"));
    send(delete("/api/v1/households/" + first + "/members/" + carlaId), carla, null, 204);
    List<Map<String, Object>> firstLeft = inventory(ana, first);
    assertEquals(2, firstLeft.size());
    assertTrue(firstLeft.stream().noneMatch(item -> "Egg".equals(item.get("name"))));

    // Carla joins Beto's home: her egg goes into his fridge.
    send(post("/api/v1/households/join"), carla, "{\"code\":\"" + secondCode + "\"}", 200);
    assertTrue(inventory(beto, second).stream().anyMatch(item -> "Egg".equals(item.get("name"))));

    // Ana deletes her home and joins Beto's: both her foods come along, still hers.
    send(delete("/api/v1/households/" + first), ana, null, 204);
    send(post("/api/v1/households/join"), ana, "{\"code\":\"" + secondCode + "\"}", 200);
    List<Map<String, Object>> anaSees = inventory(ana, second);
    Map<String, Object> cans =
        anaSees.stream()
            .filter(item -> "Beer can".equals(item.get("name")))
            .findFirst()
            .orElseThrow();
    assertEquals(720.0, ((Number) cans.get("grams")).doubleValue());
    assertEquals(360.0, ((Number) cans.get("pieceGrams")).doubleValue());
    assertTrue(((List<?>) cans.get("statuses")).contains("PRIVATE"));
    Map<String, Object> milk =
        anaSees.stream().filter(item -> "Milk".equals(item.get("name"))).findFirst().orElseThrow();
    assertEquals(MILK_EXPIRES, milk.get("expiresOn"));
    assertEquals(4, anaSees.size());

    // Beto sees Ana's private cans only as someone else's private food.
    assertTrue(
        inventory(beto, second).stream().anyMatch(item -> "Private food".equals(item.get("name"))));
    assertTrue(
        inventory(beto, second).stream().noneMatch(item -> "Beer can".equals(item.get("name"))));
  }

  @Test
  void foodBroughtToAHomeWithoutAFridgeWaitsForTheFridge() throws Exception {
    String dani = signUp("dani@moving.test");
    String first = home(dani, "Vieja");
    stock(dani, first, fridge(dani, first), "\"food\":\"Cheese\",\"grams\":250");
    send(delete("/api/v1/households/" + first), dani, null, 204);

    String second = home(dani, "Nueva");
    assertEquals(0, inventory(dani, second).size());
    fridge(dani, second);
    List<Map<String, Object>> unpacked = inventory(dani, second);
    assertEquals(1, unpacked.size());
    assertEquals("Cheese", unpacked.getFirst().get("name"));
  }
}
