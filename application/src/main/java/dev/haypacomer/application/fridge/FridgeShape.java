package dev.haypacomer.application.fridge;

import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.fridge.ZoneKind;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record FridgeShape(
    FridgeType type,
    int shelves,
    int doorRacks,
    int drawers,
    int freezerShelves,
    Hinge hinge,
    int widthCm,
    int heightCm,
    int depthCm) {

  public static final int MAX_SHELVES = 6;
  public static final int MAX_DOOR_RACKS = 5;
  public static final int MAX_DRAWERS = 3;

  public FridgeShape {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(hinge, "hinge");
    within("shelves", shelves, 0, MAX_SHELVES);
    within("doorRacks", doorRacks, 0, MAX_DOOR_RACKS);
    within("drawers", drawers, 0, MAX_DRAWERS);
    within("freezerShelves", freezerShelves, type.needsFreezer() ? 1 : 0, type.maxFreezerShelves());
    within("widthCm", widthCm, 20, 250);
    within("heightCm", heightCm, 20, 250);
    within("depthCm", depthCm, 20, 250);
  }

  // The fridges people start from: the Challenger CR 050 minibar of the demo first, then the common
  // shapes of a Colombian kitchen.
  public static List<FridgeExample> examples() {
    return List.of(
        new FridgeExample(
            "challenger-cr050",
            new FridgeShape(FridgeType.MINIBAR, 2, 3, 0, 1, Hinge.LEFT, 43, 58, 49)),
        new FridgeExample("top-freezer", standard(FridgeType.TOP_FREEZER, 3, 3, 1, 1)),
        new FridgeExample("bottom-freezer", standard(FridgeType.BOTTOM_FREEZER, 3, 3, 1, 2)),
        new FridgeExample("side-by-side", standard(FridgeType.SIDE_BY_SIDE, 4, 4, 2, 4)),
        new FridgeExample("single-door", standard(FridgeType.SINGLE_DOOR, 3, 3, 1, 1)));
  }

  // A fridge whose shape was never described: its counts from the tree, drawn as the top-freezer
  // fridge the
  // app always showed.
  public static FridgeShape inferredFrom(Fridge fridge) {
    return standard(FridgeType.TOP_FREEZER, 0, 0, 0, 1).withCountsOf(fridge);
  }

  // The counts always come from the fridge itself, so a stored shape never disagrees with its
  // trays.
  public FridgeShape withCountsOf(Fridge fridge) {
    return new FridgeShape(
        type,
        Math.min(count(fridge, ZoneKind.SHELF), MAX_SHELVES),
        Math.min(count(fridge, ZoneKind.DOOR), MAX_DOOR_RACKS),
        Math.min(count(fridge, ZoneKind.DRAWER), MAX_DRAWERS),
        Math.min(
            Math.max(count(fridge, ZoneKind.FREEZER), type.needsFreezer() ? 1 : 0),
            type.maxFreezerShelves()),
        hinge,
        widthCm,
        heightCm,
        depthCm);
  }

  // Adds the trays the shape asks for and removes the ones it no longer has, the lowest first; a
  // tray
  // that still holds food is never removed (TrayNotEmptyException). Kept trays keep their names.
  public void fit(Fridge fridge) {
    fitZone(fridge, ZoneKind.SHELF, shelves);
    fitZone(fridge, ZoneKind.DOOR, doorRacks);
    fitZone(fridge, ZoneKind.DRAWER, drawers);
    fitZone(fridge, ZoneKind.FREEZER, freezerShelves);
  }

  private static void fitZone(Fridge fridge, ZoneKind kind, int wanted) {
    List<Zone> zones = fridge.children().stream().filter(zone -> zone.kind() == kind).toList();
    if (zones.isEmpty() && wanted > 0) {
      Zone zone = Zone.named(zoneName(kind), kind);
      fridge.add(zone);
      zones = List.of(zone);
    }
    List<Zone> lastFirst = zones.reversed();
    int total = zones.stream().mapToInt(zone -> zone.children().size()).sum();
    for (int index = total; wanted > index; index++) {
      Zone zone = zones.getLast();
      zone.add(Tray.named(trayName(kind, index), zone.children().size()));
    }
    for (Zone zone : lastFirst) {
      List<Tray> trays =
          zone.children().stream().sorted(Comparator.comparingInt(Tray::position)).toList();
      for (Tray tray : trays.reversed()) {
        if (total > wanted) {
          zone.remove(tray.id());
          total--;
        }
      }
      if (zone.children().isEmpty() && wanted == 0) {
        fridge.removeZone(zone.id());
      }
    }
  }

  private static FridgeShape standard(
      FridgeType type, int shelves, int doorRacks, int drawers, int freezerShelves) {
    return new FridgeShape(
        type,
        shelves,
        doorRacks,
        drawers,
        freezerShelves,
        Hinge.LEFT,
        type.widthCm(),
        type.heightCm(),
        type.depthCm());
  }

  private static int count(Fridge fridge, ZoneKind kind) {
    return fridge.children().stream()
        .filter(zone -> zone.kind() == kind)
        .mapToInt(zone -> zone.children().size())
        .sum();
  }

  private static String zoneName(ZoneKind kind) {
    return switch (kind) {
      case SHELF -> "Shelves";
      case DOOR -> "Door";
      case DRAWER -> "Drawer";
      case FREEZER -> "Freezer";
    };
  }

  // Names the translations know: Top, Middle and Bottom, then Shelf 4; Rack, then Rack 2;
  // Vegetables,
  // then Drawer 2.
  private static String trayName(ZoneKind kind, int index) {
    return switch (kind) {
      case SHELF, FREEZER ->
          index < 3 ? List.of("Top", "Middle", "Bottom").get(index) : "Shelf " + (index + 1);
      case DOOR -> index == 0 ? "Rack" : "Rack " + (index + 1);
      case DRAWER -> index == 0 ? "Vegetables" : "Drawer " + (index + 1);
    };
  }

  private static void within(String name, int value, int min, int max) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          name + " must be between " + min + " and " + max + ": " + value);
    }
  }
}
