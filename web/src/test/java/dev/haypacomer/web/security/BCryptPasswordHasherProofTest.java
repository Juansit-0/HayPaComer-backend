package dev.haypacomer.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherProofTest {

  private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

  @Test
  void derivesTheSameProofAsTheBrowserForAnyEmailCase() {
    String expected = "ca869baf32afb9205dd3bc4c84c0b8959b9739690d3f357f56b86e36050c4471";

    assertEquals(expected, hasher.proof("juan@haypacomer.dev", "fresh-milk-842"));
    assertEquals(expected, hasher.proof(" Juan@HayPaComer.dev ", "fresh-milk-842"));
  }
}
