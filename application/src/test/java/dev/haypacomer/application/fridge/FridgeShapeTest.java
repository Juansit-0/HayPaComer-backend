package dev.haypacomer.application.fridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.ZoneKind;
import java.util.List;
import org.junit.jupiter.api.Test;

class FridgeShapeTest {

  @Test
  void theChallengerMinibarComesFirstAndEveryExampleBuildsItsOwnCounts() {
    List<FridgeExample> examples = FridgeShape.examples();
    assertEquals("challenger-cr050", examples.getFirst().key());
    for (FridgeExample example : examples) {
      Fridge fridge = FridgeLayout.EMPTY.build("Kitchen");
      example.shape().fit(fridge);
      assertEquals(example.shape(), example.shape().withCountsOf(fridge), example.key());
    }
  }

  @Test
  void rangesAndAFreezerWhereTheTypeHasOne() {
    assertThrows(IllegalArgumentException.class, () -> shape(FridgeType.MINIBAR, 7, 0));
    assertThrows(IllegalArgumentException.class, () -> shape(FridgeType.TOP_FREEZER, 3, 0));
    assertThrows(IllegalArgumentException.class, () -> shape(FridgeType.MINIBAR, 2, 2));
    assertThrows(
        IllegalArgumentException.class,
        () -> new FridgeShape(FridgeType.MINIBAR, 2, 3, 0, 1, Hinge.LEFT, 10, 58, 49));
  }

  @Test
  void theStandardFridgeIsReadFromItsTraysAndNamesNewTraysInOrder() {
    Fridge fridge = FridgeLayout.STANDARD.build("Kitchen");
    FridgeShape inferred = FridgeShape.inferredFrom(fridge);
    assertEquals(FridgeType.TOP_FREEZER, inferred.type());
    assertEquals(
        List.of(3, 1, 1, 1),
        List.of(
            inferred.shelves(),
            inferred.doorRacks(),
            inferred.drawers(),
            inferred.freezerShelves()));

    new FridgeShape(FridgeType.TOP_FREEZER, 5, 3, 2, 2, Hinge.LEFT, 60, 165, 65).fit(fridge);

    assertEquals(
        List.of("Top", "Middle", "Bottom", "Shelf 4", "Shelf 5"), names(fridge, ZoneKind.SHELF));
    assertEquals(List.of("Rack", "Rack 2", "Rack 3"), names(fridge, ZoneKind.DOOR));
    assertEquals(List.of("Vegetables", "Drawer 2"), names(fridge, ZoneKind.DRAWER));
    assertEquals(List.of("Top", "Middle"), names(fridge, ZoneKind.FREEZER));
    assertEquals(2, FridgeShape.inferredFrom(fridge).freezerShelves());
  }

  @Test
  void shrinkingRemovesTheLowestTraysAndZonesLeftEmpty() {
    Fridge fridge = FridgeLayout.STANDARD.build("Kitchen");

    shape(FridgeType.MINIBAR, 2, 0).fit(fridge);

    assertEquals(List.of("Top", "Middle"), names(fridge, ZoneKind.SHELF));
    assertEquals(List.of(), names(fridge, ZoneKind.DRAWER));
    assertEquals(
        List.of(ZoneKind.SHELF, ZoneKind.DOOR),
        fridge.children().stream().map(zone -> zone.kind()).toList());
  }

  private static FridgeShape shape(FridgeType type, int shelves, int freezer) {
    return new FridgeShape(type, shelves, 1, 0, freezer, Hinge.RIGHT, 43, 58, 49);
  }

  private static List<String> names(Fridge fridge, ZoneKind kind) {
    return fridge.children().stream()
        .filter(zone -> zone.kind() == kind)
        .flatMap(zone -> zone.children().stream())
        .map(Tray::name)
        .toList();
  }
}
