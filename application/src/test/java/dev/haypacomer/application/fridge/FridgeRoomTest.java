package dev.haypacomer.application.fridge;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.domain.food.FoodCategory;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.ZoneKind;
import dev.haypacomer.domain.quantity.ConversionFactors;
import dev.haypacomer.domain.quantity.Grams;
import dev.haypacomer.domain.quantity.Unit;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FridgeRoomTest {

  private static final FoodMetadata MILK =
      new FoodMetadata(
          "Milk",
          FoodCategory.DAIRY,
          Unit.MILLILITER,
          ConversionFactors.withDensity("1.03"),
          true,
          7,
          Set.of());
  private static final FoodMetadata CARROT =
      new FoodMetadata(
          "Carrot",
          FoodCategory.VEGETABLE,
          Unit.GRAM,
          ConversionFactors.MASS_ONLY,
          true,
          14,
          Set.of());

  @Test
  void foodTakesItsWeightOverHowDenseItSits() {
    assertEquals(1.0, FridgeRoom.liters(MILK, Grams.of(1030)), 1e-9);
    assertEquals(2.0, FridgeRoom.liters(CARROT, Grams.of(1000)), 1e-9);
  }

  @Test
  void aChallengerShelfFillsUpAndSaysHowMuchRoomIsLeft() {
    FridgeShape shape = FridgeShape.examples().getFirst().shape();
    Fridge fridge = FridgeLayout.EMPTY.build("Minibar");
    shape.fit(fridge);
    FridgeRoom room = new FridgeRoom(shape);
    Tray shelf = tray(fridge, ZoneKind.SHELF);
    double capacity = room.capacity(ZoneKind.SHELF);
    assertTrue(capacity > 5 && capacity < 30, "litres per shelf: " + capacity);
    assertTrue(room.capacity(ZoneKind.FREEZER) > 0);

    assertDoesNotThrow(() -> room.requireRoom(fridge, shelf.id(), CARROT, Grams.of(1000)));
    int grams = (int) (capacity * 1000 * 0.5) - 1000;
    shelf.add(new FoodItem(FoodItemId.newId(), CARROT, Grams.of(grams), Grams.ZERO, null));

    TrayFullException full =
        assertThrows(
            TrayFullException.class,
            () -> room.requireRoom(fridge, shelf.id(), CARROT, Grams.of(2001)));
    assertEquals(shelf.id(), full.tray());
    assertEquals(capacity - grams / 500.0, full.freeLiters(), 1e-6);
    assertEquals(
        grams / 500.0,
        room.spaces(fridge).stream()
            .filter(space -> space.tray().equals(shelf.id()))
            .findFirst()
            .orElseThrow()
            .usedLiters(),
        1e-6);
  }

  private static Tray tray(Fridge fridge, ZoneKind kind) {
    return fridge.children().stream()
        .filter(zone -> zone.kind() == kind)
        .flatMap(zone -> zone.children().stream())
        .findFirst()
        .orElseThrow();
  }
}
