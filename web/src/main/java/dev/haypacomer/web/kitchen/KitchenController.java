package dev.haypacomer.web.kitchen;

import dev.haypacomer.application.HayPaComerFacade;
import dev.haypacomer.application.fridge.FridgeLayout;
import dev.haypacomer.application.moving.UnpackFood;
import dev.haypacomer.application.port.FoodItemPieces;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.web.kitchen.KitchenDtos.InventoryItemResponse;
import dev.haypacomer.web.kitchen.KitchenDtos.NodeResponse;
import dev.haypacomer.web.kitchen.KitchenDtos.SetUpFridgeRequest;
import dev.haypacomer.web.kitchen.KitchenDtos.SnapshotResponse;
import dev.haypacomer.web.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Kitchen", description = "Fridges and the live inventory")
@RestController
@RequestMapping("/api/v1/households/{householdId}")
public class KitchenController {

  private final HayPaComerFacade facade;
  private final FoodItemPieces foodPieces;
  private final UnpackFood unpackFood;

  public KitchenController(
      HayPaComerFacade facade, FoodItemPieces foodPieces, UnpackFood unpackFood) {
    this.facade = facade;
    this.foodPieces = foodPieces;
    this.unpackFood = unpackFood;
  }

  @Operation(summary = "Set up a new fridge")
  @PostMapping("/fridges")
  @ResponseStatus(HttpStatus.CREATED)
  NodeResponse setUpFridge(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @Valid @RequestBody SetUpFridgeRequest request) {
    FridgeLayout layout = request.layout() == null ? FridgeLayout.STANDARD : request.layout();
    var fridge =
        facade.setUpFridge(
            CurrentUser.of(jwt), new HouseholdId(householdId), request.name(), layout);
    // A home made before its fridge gets the food its members brought along now.
    unpackFood.unpack(CurrentUser.of(jwt), new HouseholdId(householdId));
    return NodeResponse.from(
        facade.fridges(CurrentUser.of(jwt), new HouseholdId(householdId)).stream()
            .filter(found -> found.id().equals(fridge.id()))
            .findFirst()
            .orElse(fridge));
  }

  @Operation(summary = "List the household fridges")
  @GetMapping("/fridges")
  List<NodeResponse> fridges(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId) {
    HouseholdId household = new HouseholdId(householdId);
    var hidden = facade.hiddenFoods(CurrentUser.of(jwt), household);
    return facade.fridges(CurrentUser.of(jwt), household).stream()
        .map(fridge -> NodeResponse.from(fridge, hidden))
        .toList();
  }

  @Operation(summary = "List the food in the fridges, optionally food to use first on top")
  @GetMapping("/inventory")
  List<InventoryItemResponse> inventory(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @RequestParam(defaultValue = "false") boolean rescueFirst) {
    HouseholdId household = new HouseholdId(householdId);
    var entries =
        rescueFirst
            ? facade.rescueFirst(CurrentUser.of(jwt), household)
            : facade.inventory(CurrentUser.of(jwt), household);
    var pieces = foodPieces.find(entries.stream().map(entry -> entry.food().item().id()).toList());
    return entries.stream()
        .map(entry -> InventoryItemResponse.from(entry, pieces.get(entry.food().item().id())))
        .toList();
  }

  @Operation(summary = "Show the kitchen at a glance")
  @GetMapping("/kitchen")
  SnapshotResponse snapshot(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId) {
    return SnapshotResponse.from(
        facade.snapshot(CurrentUser.of(jwt), new HouseholdId(householdId)));
  }
}
