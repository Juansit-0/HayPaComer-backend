package dev.haypacomer.web.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoAccountController {

  private final String email;
  private final String password;

  public DemoAccountController(
      @Value("${haypacomer.demo.email:}") String email,
      @Value("${haypacomer.demo.password:}") String password) {
    this.email = email;
    this.password = password;
  }

  @GetMapping("/api/v1/auth/demo")
  ResponseEntity<DemoAccount> demo() {
    if (email.isBlank() || password.isBlank()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new DemoAccount(email, password));
  }

  record DemoAccount(String email, String password) {}
}
