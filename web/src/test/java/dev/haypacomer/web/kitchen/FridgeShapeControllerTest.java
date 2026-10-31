package dev.haypacomer.web.kitchen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.haypacomer.application.device.AuthenticateDevice;
import dev.haypacomer.application.fridge.ChangeFridgeShape;
import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.application.fridge.ViewFridgeShape;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.TrayNotEmptyException;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.web.error.ApiExceptionHandler;
import dev.haypacomer.web.security.JwtAccessTokenIssuer;
import dev.haypacomer.web.security.JwtProperties;
import dev.haypacomer.web.security.SecurityConfiguration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FridgeShapeController.class)
@Import({SecurityConfiguration.class, ApiExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(
    properties = {
      "haypacomer.security.jwt.secret=test-secret-with-at-least-32-bytes!!",
      "haypacomer.security.jwt.issuer=https://api.haypacomer.dev",
      "haypacomer.security.jwt.access-token-time-to-live=15m"
    })
class FridgeShapeControllerTest {

  private static final FridgeShape CR050 = FridgeShape.examples().getFirst().shape();

  @Autowired private MockMvc mvc;
  @Autowired private JwtEncoder encoder;
  @Autowired private JwtProperties properties;
  @MockitoBean private ViewFridgeShape viewFridgeShape;
  @MockitoBean private ChangeFridgeShape changeFridgeShape;
  @MockitoBean private AuthenticateDevice authenticateDevice;

  private final UserId juan = UserId.newId();
  private final HouseholdId household = HouseholdId.newId();
  private final FridgeId fridge = FridgeId.newId();

  private String bearer() {
    return "Bearer "
        + new JwtAccessTokenIssuer(encoder, properties).issue(juan, Instant.now()).value();
  }

  private String path() {
    return "/api/v1/households/" + household.value() + "/fridges/" + fridge.value() + "/shape";
  }

  @Test
  void listsTheExamplesWithTheChallengerFirst() throws Exception {
    mvc.perform(get("/api/v1/fridge-examples").header("Authorization", bearer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].key").value("challenger-cr050"))
        .andExpect(jsonPath("$[0].shape.type").value("MINIBAR"))
        .andExpect(jsonPath("$[0].shape.doorRacks").value(3));
  }

  @Test
  void readsAndChangesTheShape() throws Exception {
    when(viewFridgeShape.view(juan, household, fridge)).thenReturn(CR050);
    when(changeFridgeShape.change(eq(juan), eq(household), eq(fridge), eq(CR050)))
        .thenReturn(CR050);

    mvc.perform(get(path()).header("Authorization", bearer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.widthCm").value(43));
    mvc.perform(
            put(path())
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"MINIBAR","shelves":2,"doorRacks":3,"drawers":0,"freezerShelves":1,
                     "hinge":"LEFT","widthCm":43,"heightCm":58,"depthCm":49}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shelves").value(2));
  }

  @Test
  void aShapeOutOfRangeIsRejectedAndATrayWithFoodIsAConflict() throws Exception {
    Tray bottom = Tray.named("Bottom", 2);
    when(changeFridgeShape.change(any(), any(), any(), any()))
        .thenThrow(new TrayNotEmptyException(bottom));

    mvc.perform(
            put(path())
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"MINIBAR","shelves":9,"doorRacks":3,"drawers":0,"freezerShelves":1,
                     "hinge":"LEFT","widthCm":43,"heightCm":58,"depthCm":49}
                    """))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put(path())
                .header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"type":"MINIBAR","shelves":2,"doorRacks":3,"drawers":0,"freezerShelves":1,
                     "hinge":"LEFT","widthCm":43,"heightCm":58,"depthCm":49}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.trayId").value(bottom.id().value().toString()));
    mvc.perform(get("/api/v1/fridge-examples")).andExpect(status().isUnauthorized());
  }
}
