package dev.haypacomer.application.inventory;

import dev.haypacomer.application.fridge.FridgeRoom;
import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.application.port.FridgeShapeRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.TrayId;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.Grams;
import java.time.Instant;

record InventoryWorkspace(
    UserId actor,
    Household household,
    HouseholdInventory inventory,
    FoodAccessGuard guard,
    FoodCatalogRepository catalog,
    ExpiryDesk expiry,
    FridgeShapeRepository shapes,
    Instant now) {

  // Refuses food that would not fit where it goes; without shapes (older wiring and tests) any
  // amount fits.
  void requireRoom(Fridge fridge, TrayId tray, FoodMetadata food, Grams grams) {
    if (shapes == null) {
      return;
    }
    FridgeShape shape =
        shapes
            .find(fridge.id())
            .map(stored -> stored.withCountsOf(fridge))
            .orElseGet(() -> FridgeShape.inferredFrom(fridge));
    new FridgeRoom(shape).requireRoom(fridge, tray, food, grams);
  }
}
