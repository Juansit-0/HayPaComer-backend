package dev.haypacomer.web.kitchen;

import dev.haypacomer.application.fridge.ChangeFridgeShape;
import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.application.fridge.FridgeType;
import dev.haypacomer.application.fridge.Hinge;
import dev.haypacomer.application.fridge.TraySpace;
import dev.haypacomer.application.fridge.ViewFridgeShape;
import dev.haypacomer.application.fridge.ViewFridgeSpace;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.web.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FridgeShapeController {

  private final ViewFridgeShape viewFridgeShape;
  private final ChangeFridgeShape changeFridgeShape;
  private final ViewFridgeSpace viewFridgeSpace;

  public FridgeShapeController(
      ViewFridgeShape viewFridgeShape,
      ChangeFridgeShape changeFridgeShape,
      ViewFridgeSpace viewFridgeSpace) {
    this.viewFridgeShape = viewFridgeShape;
    this.changeFridgeShape = changeFridgeShape;
    this.viewFridgeSpace = viewFridgeSpace;
  }

  @GetMapping("/api/v1/households/{householdId}/fridges/{fridgeId}/space")
  List<SpaceResponse> space(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @PathVariable UUID fridgeId) {
    return viewFridgeSpace
        .view(CurrentUser.of(jwt), new HouseholdId(householdId), new FridgeId(fridgeId))
        .stream()
        .map(SpaceResponse::from)
        .toList();
  }

  record SpaceResponse(UUID trayId, double capacityLiters, double usedLiters) {

    static SpaceResponse from(TraySpace space) {
      return new SpaceResponse(
          space.tray().value(),
          Math.round(space.capacityLiters() * 10) / 10.0,
          Math.round(space.usedLiters() * 10) / 10.0);
    }
  }

  @GetMapping("/api/v1/fridge-examples")
  List<ExampleResponse> examples() {
    return FridgeShape.examples().stream()
        .map(example -> new ExampleResponse(example.key(), ShapeBody.from(example.shape())))
        .toList();
  }

  @GetMapping("/api/v1/households/{householdId}/fridges/{fridgeId}/shape")
  ShapeBody shape(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @PathVariable UUID fridgeId) {
    return ShapeBody.from(
        viewFridgeShape.view(
            CurrentUser.of(jwt), new HouseholdId(householdId), new FridgeId(fridgeId)));
  }

  @PutMapping("/api/v1/households/{householdId}/fridges/{fridgeId}/shape")
  ShapeBody change(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @PathVariable UUID fridgeId,
      @Valid @RequestBody ShapeBody body) {
    return ShapeBody.from(
        changeFridgeShape.change(
            CurrentUser.of(jwt),
            new HouseholdId(householdId),
            new FridgeId(fridgeId),
            body.toShape()));
  }

  record ExampleResponse(String key, ShapeBody shape) {}

  record ShapeBody(
      @NotNull FridgeType type,
      int shelves,
      int doorRacks,
      int drawers,
      int freezerShelves,
      @NotNull Hinge hinge,
      int widthCm,
      int heightCm,
      int depthCm) {

    static ShapeBody from(FridgeShape shape) {
      return new ShapeBody(
          shape.type(),
          shape.shelves(),
          shape.doorRacks(),
          shape.drawers(),
          shape.freezerShelves(),
          shape.hinge(),
          shape.widthCm(),
          shape.heightCm(),
          shape.depthCm());
    }

    FridgeShape toShape() {
      return new FridgeShape(
          type, shelves, doorRacks, drawers, freezerShelves, hinge, widthCm, heightCm, depthCm);
    }
  }
}
