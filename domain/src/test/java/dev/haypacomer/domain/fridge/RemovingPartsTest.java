package dev.haypacomer.domain.fridge;

import static dev.haypacomer.domain.fridge.FridgeFixtures.MILK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RemovingPartsTest {

  @Test
  void anEmptyTrayLeavesItsZoneAndAFullOneStays() {
    Zone shelves = Zone.named("Shelves", ZoneKind.SHELF);
    Tray top = Tray.named("Top", 0);
    Tray bottom = Tray.named("Bottom", 1);
    shelves.add(top);
    shelves.add(bottom);
    top.add(FridgeFixtures.item(MILK, 842));

    assertEquals(bottom, shelves.remove(bottom.id()));
    TrayNotEmptyException full =
        assertThrows(TrayNotEmptyException.class, () -> shelves.remove(top.id()));
    assertEquals(top.id(), full.tray());
    assertEquals(1, shelves.children().size());
    assertThrows(IllegalArgumentException.class, () -> shelves.remove(TrayId.newId()));
  }

  @Test
  void anEmptyZoneLeavesTheFridgeAndOneWithFoodStays() {
    Fridge fridge = Fridge.named("Kitchen");
    Zone door = Zone.named("Door", ZoneKind.DOOR);
    door.add(Tray.named("Rack", 0));
    Zone drawer = Zone.named("Drawer", ZoneKind.DRAWER);
    Tray vegetables = Tray.named("Vegetables", 0);
    drawer.add(vegetables);
    fridge.add(door);
    fridge.add(drawer);
    vegetables.add(FridgeFixtures.item(MILK, 200));

    assertEquals(door, fridge.removeZone(door.id()));
    assertThrows(TrayNotEmptyException.class, () -> fridge.removeZone(drawer.id()));
    assertEquals(1, fridge.children().size());
    assertThrows(IllegalArgumentException.class, () -> fridge.removeZone(ZoneId.newId()));
    assertTrue(fridge.findTray(vegetables.id()).isPresent());
  }
}
