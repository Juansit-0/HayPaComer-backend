package dev.haypacomer.application.fridge;

import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.TrayId;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.fridge.ZoneKind;
import dev.haypacomer.domain.quantity.Grams;
import java.util.List;
import java.util.Objects;

public record FridgeRoom(FridgeShape shape) {

  static final double USABLE = 0.75;
  private static final double WALL = 3.5;
  private static final double BASE = 4;

  public FridgeRoom {
    Objects.requireNonNull(shape, "shape");
  }

  public List<TraySpace> spaces(Fridge fridge) {
    return fridge.children().stream()
        .flatMap(
            zone ->
                zone.children().stream()
                    .map(tray -> new TraySpace(tray.id(), capacity(zone.kind()), used(tray))))
        .toList();
  }

  public void requireRoom(Fridge fridge, TrayId trayId, FoodMetadata food, Grams adding) {
    Zone zone =
        fridge
            .zoneOf(trayId)
            .orElseThrow(() -> new IllegalArgumentException("Tray not in fridge: " + trayId));
    Tray tray = fridge.findTray(trayId).orElseThrow();
    double needed = liters(food, adding);
    double free = capacity(zone.kind()) - used(tray);
    if (needed > free + 1e-9) {
      throw new TrayFullException(trayId, Math.max(0, free), needed);
    }
  }

  static double liters(FoodMetadata food, Grams grams) {
    double density =
        food.conversion()
            .density()
            .map(value -> value.doubleValue())
            .orElse(
                switch (food.category()) {
                  case VEGETABLE, EGGS -> 0.5;
                  case FRUIT -> 0.55;
                  case GRAIN -> 0.75;
                  case LEGUME -> 0.8;
                  case MEAT, POULTRY, FISH, PREPARED -> 0.9;
                  case DAIRY, BEVERAGE, CONDIMENT, OTHER -> 1.0;
                });
    return grams.value().doubleValue() / density / 1000;
  }

  private static double used(Tray tray) {
    return tray.children().stream().mapToDouble(item -> liters(item.food(), item.quantity())).sum();
  }

  double capacity(ZoneKind kind) {
    double width = shape.widthCm();
    double depth = shape.depthCm();
    double body = shape.heightCm() - BASE;
    double coldWidth = width;
    double coldHeight = body;
    double freezerWidth = 0;
    double freezerHeight = 0;
    double icebox = 0;
    switch (shape.type()) {
      case TOP_FREEZER, BOTTOM_FREEZER -> {
        coldHeight = body * 0.66;
        freezerWidth = width;
        freezerHeight = body * 0.34 - WALL;
      }
      case SIDE_BY_SIDE -> {
        coldWidth = width * 0.58;
        freezerWidth = width * 0.42;
        freezerHeight = body;
      }
      default -> icebox = shape.freezerShelves() > 0 ? Math.min(16, body * 0.2) : 0;
    }
    double innerWidth = coldWidth - 2 * WALL;
    double innerDepth = depth - 12;
    double drawerHeight = shape.drawers() > 0 ? Math.min(20, coldHeight * 0.2) : 0;
    double top = coldHeight - (icebox > 0 ? icebox + 2 : 0);
    double bottom = shape.drawers() > 0 ? 2 + drawerHeight + 9 : 8;
    double litres =
        switch (kind) {
          case SHELF -> innerWidth * innerDepth * (top - bottom) / Math.max(1, shape.shelves());
          case DRAWER -> innerWidth / Math.max(1, shape.drawers()) * drawerHeight * (depth - 16);
          case DOOR -> (coldWidth - 12) * 9 * 18;
          case FREEZER ->
              icebox > 0
                  ? innerWidth * 0.72 * icebox * (depth - 22)
                  : (freezerWidth - 2 * WALL)
                      * freezerHeight
                      * innerDepth
                      / Math.max(1, shape.freezerShelves());
        };
    return Math.max(0, litres) / 1000 * USABLE;
  }
}
