package dev.haypacomer.web.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DemoAccountControllerTest {

  @Test
  void theDemoAccountIsOfferedOnlyWhenBothValuesAreSet() {
    assertEquals(HttpStatus.NOT_FOUND, new DemoAccountController("", "").demo().getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        new DemoAccountController("reviewer@example.test", " ").demo().getStatusCode());
    var offered = new DemoAccountController("reviewer@example.test", "secret").demo();
    assertEquals(HttpStatus.OK, offered.getStatusCode());
    assertEquals("reviewer@example.test", offered.getBody().email());
  }
}
