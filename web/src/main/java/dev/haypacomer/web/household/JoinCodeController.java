package dev.haypacomer.web.household;

import dev.haypacomer.application.household.CreateJoinCode;
import dev.haypacomer.application.household.JoinHouseholdWithCode;
import dev.haypacomer.application.household.RevokeJoinCode;
import dev.haypacomer.application.household.ViewJoinCode;
import dev.haypacomer.application.moving.UnpackFood;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.JoinCode;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.web.household.HouseholdDtos.HouseholdResponse;
import dev.haypacomer.web.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Join codes", description = "Short codes to join a household")
@RestController
public class JoinCodeController {

  private final CreateJoinCode createCode;
  private final ViewJoinCode viewCode;
  private final RevokeJoinCode revokeCode;
  private final JoinHouseholdWithCode joinWithCode;
  private final UnpackFood unpackFood;

  public JoinCodeController(
      CreateJoinCode createCode,
      ViewJoinCode viewCode,
      RevokeJoinCode revokeCode,
      JoinHouseholdWithCode joinWithCode,
      UnpackFood unpackFood) {
    this.createCode = createCode;
    this.viewCode = viewCode;
    this.revokeCode = revokeCode;
    this.joinWithCode = joinWithCode;
    this.unpackFood = unpackFood;
  }

  @Operation(summary = "Show the household's current join code")
  @GetMapping("/api/v1/households/{householdId}/join-code")
  JoinCodeResponse current(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId) {
    return JoinCodeResponse.from(
        viewCode.current(CurrentUser.of(jwt), new HouseholdId(householdId)));
  }

  @Operation(summary = "Create a new join code for the household")
  @PostMapping("/api/v1/households/{householdId}/join-code")
  @ResponseStatus(HttpStatus.CREATED)
  JoinCodeResponse create(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @RequestBody(required = false) CreateCodeRequest request) {
    Role role = request == null ? null : request.role();
    return JoinCodeResponse.from(
        createCode.create(CurrentUser.of(jwt), new HouseholdId(householdId), role));
  }

  @Operation(summary = "Revoke the household join code")
  @DeleteMapping("/api/v1/households/{householdId}/join-code")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId) {
    revokeCode.revoke(CurrentUser.of(jwt), new HouseholdId(householdId));
  }

  @Operation(summary = "Join a household with a join code")
  @PostMapping("/api/v1/households/join")
  HouseholdResponse join(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody JoinRequest request) {
    UserId actor = CurrentUser.of(jwt);
    var joined = joinWithCode.join(actor, request.code());
    // The food brought from the previous home goes into this fridge.
    unpackFood.unpack(actor, joined.id());
    return HouseholdResponse.from(joined, actor);
  }

  record CreateCodeRequest(Role role) {}

  record JoinRequest(@NotBlank @Size(max = 20) String code) {

    @Override
    public String toString() {
      return "JoinRequest[protected]";
    }
  }

  record JoinCodeResponse(String code, Role role, Instant expiresAt) {

    static JoinCodeResponse from(JoinCode code) {
      return new JoinCodeResponse(code.display(), code.role(), code.expiresAt());
    }
  }
}
