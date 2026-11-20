package dev.haypacomer.web.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DemoAccountControllerTest {

  private static DemoAccountController controller(String email, String password) {
    return new DemoAccountController(email, password, "", "", "");
  }

  @Test
  void theDemoAccountIsOfferedOnlyWhenBothValuesAreSet() {
    assertEquals(HttpStatus.NOT_FOUND, controller("", "").demo().getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND, controller("reviewer@example.test", " ").demo().getStatusCode());
    var offered = controller("reviewer@example.test", "secret").demo();
    assertEquals(HttpStatus.OK, offered.getStatusCode());
    assertEquals("reviewer@example.test", offered.getBody().email());
    assertEquals(1, offered.getBody().accounts().size());
  }

  @Test
  void aSecondLabelledAccountIsListedWhenConfigured() {
    var offered =
        new DemoAccountController(
                "reviewer@example.test", "secret", "home@example.test", "other", "Prueba de casa")
            .demo()
            .getBody();
    assertEquals("reviewer@example.test", offered.email());
    assertEquals(2, offered.accounts().size());
    assertEquals("Prueba de casa", offered.accounts().get(1).label());
    assertEquals("home@example.test", offered.accounts().get(1).email());
  }
}
