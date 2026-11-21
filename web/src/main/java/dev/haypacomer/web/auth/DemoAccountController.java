package dev.haypacomer.web.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Demo", description = "Shared test accounts for the demo")
@RestController
public class DemoAccountController {

  private final String email;
  private final String password;
  private final Account second;

  public DemoAccountController(
      @Value("${haypacomer.demo.email:}") String email,
      @Value("${haypacomer.demo.password:}") String password,
      @Value("${haypacomer.demo.second-email:}") String secondEmail,
      @Value("${haypacomer.demo.second-password:}") String secondPassword,
      @Value("${haypacomer.demo.second-label:}") String secondLabel) {
    this.email = email;
    this.password = password;
    this.second =
        secondEmail.isBlank() || secondPassword.isBlank()
            ? null
            : new Account(secondLabel.isBlank() ? null : secondLabel, secondEmail, secondPassword);
  }

  /**
   * The shared test accounts. The first one is also given as email and password, as before; a
   * second one, with its own label, appears in the list when it is configured.
   */
  @Operation(summary = "Get the shared demo accounts when they are configured")
  @GetMapping("/api/v1/auth/demo")
  ResponseEntity<DemoAccount> demo() {
    if (email.isBlank() || password.isBlank()) {
      return ResponseEntity.notFound().build();
    }
    List<Account> accounts =
        second == null
            ? List.of(new Account(null, email, password))
            : List.of(new Account(null, email, password), second);
    return ResponseEntity.ok(new DemoAccount(email, password, accounts));
  }

  record Account(String label, String email, String password) {}

  record DemoAccount(String email, String password, List<Account> accounts) {}
}
