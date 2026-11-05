package dev.haypacomer.domain.fridge;

import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.quantity.Grams;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class Fridge implements FridgeNode {

  private final FridgeId id;
  private final String name;
  private final List<Zone> zones = new ArrayList<>();

  public Fridge(FridgeId id, String name) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Names.require(name, "Fridge name");
  }

  public static Fridge named(String name) {
    return new Fridge(FridgeId.newId(), name);
  }

  public FridgeId id() {
    return id;
  }

  public void add(Zone zone) {
    Objects.requireNonNull(zone, "zone");
    if (zones.stream().anyMatch(existing -> existing.id().equals(zone.id()))) {
      throw new IllegalArgumentException("Zone already in fridge " + name + ": " + zone.id());
    }
    zones.add(zone);
  }

  public Zone removeZone(ZoneId zoneId) {
    Zone zone =
        zones.stream()
            .filter(existing -> existing.id().equals(zoneId))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Zone not in fridge " + name + ": " + zoneId));
    zone.children().stream()
        .filter(tray -> tray.itemCount() > 0)
        .findFirst()
        .ifPresent(
            tray -> {
              throw new TrayNotEmptyException(tray);
            });
    zones.remove(zone);
    return zone;
  }

  public Optional<Tray> findTray(TrayId trayId) {
    return trays().filter(tray -> tray.id().equals(trayId)).findFirst();
  }

  public Optional<Zone> zoneOf(TrayId trayId) {
    return zones.stream().filter(zone -> zone.find(trayId).isPresent()).findFirst();
  }

  public Optional<Zone> zoneHolding(FoodItemId itemId) {
    return locate(itemId).flatMap(tray -> zoneOf(tray.id()));
  }

  public Optional<Tray> locate(FoodItemId itemId) {
    return trays().filter(tray -> tray.find(itemId).isPresent()).findFirst();
  }

  public Optional<FoodItem> findItem(FoodItemId itemId) {
    return foodItems().filter(item -> item.id().equals(itemId)).findFirst();
  }

  public void place(FoodItem item, TrayId trayId) {
    Objects.requireNonNull(item, "item");
    if (locate(item.id()).isPresent()) {
      throw new IllegalArgumentException("Item already in fridge " + name + ": " + item.id());
    }
    requireTray(trayId).add(item);
  }

  public void move(FoodItemId itemId, TrayId targetTrayId) {
    Tray target = requireTray(targetTrayId);
    Tray source =
        locate(itemId)
            .orElseThrow(
                () -> new IllegalArgumentException("Item not in fridge " + name + ": " + itemId));
    if (source != target) {
      target.add(source.remove(itemId));
    }
  }

  public FoodItem take(FoodItemId itemId) {
    return locate(itemId)
        .orElseThrow(
            () -> new IllegalArgumentException("Item not in fridge " + name + ": " + itemId))
        .remove(itemId);
  }

  public FridgeMemento snapshot() {
    return new FridgeMemento(
        id,
        name,
        zones.stream()
            .map(
                zone ->
                    new FridgeMemento.ZoneState(
                        zone.id(),
                        zone.name(),
                        zone.kind(),
                        zone.children().stream()
                            .map(
                                tray ->
                                    new FridgeMemento.TrayState(
                                        tray.id(),
                                        tray.name(),
                                        tray.position(),
                                        tray.children().stream()
                                            .map(
                                                item ->
                                                    new FridgeMemento.ItemState(
                                                        item.id(),
                                                        item.food().name(),
                                                        item.quantity(),
                                                        item.tare(),
                                                        item.expiresOn().orElse(null),
                                                        item.expirySource().orElse(null),
                                                        item.openedOn().orElse(null)))
                                            .toList()))
                            .toList()))
            .toList());
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Grams totalGrams() {
    return zones.stream().map(Zone::totalGrams).reduce(Grams.ZERO, Grams::plus);
  }

  @Override
  public int itemCount() {
    return zones.stream().mapToInt(Zone::itemCount).sum();
  }

  @Override
  public List<Zone> children() {
    return List.copyOf(zones);
  }

  private Tray requireTray(TrayId trayId) {
    return findTray(trayId)
        .orElseThrow(
            () -> new IllegalArgumentException("Tray not in fridge " + name + ": " + trayId));
  }

  public static Fridge restoredFrom(FridgeMemento memento, Function<String, FoodMetadata> foods) {
    Fridge fridge = new Fridge(memento.id(), memento.name());
    for (FridgeMemento.ZoneState zoneState : memento.zones()) {
      Zone zone = new Zone(zoneState.id(), zoneState.name(), zoneState.kind());
      for (FridgeMemento.TrayState trayState : zoneState.trays()) {
        Tray tray = new Tray(trayState.id(), trayState.name(), trayState.position());
        for (FridgeMemento.ItemState itemState : trayState.items()) {
          tray.add(
              new FoodItem(
                  itemState.id(),
                  Objects.requireNonNull(
                      foods.apply(itemState.foodName()), "Unknown food " + itemState.foodName()),
                  itemState.quantity(),
                  itemState.tare(),
                  itemState.expiresOn(),
                  itemState.expirySource(),
                  itemState.openedOn()));
        }
        zone.add(tray);
      }
      fridge.add(zone);
    }
    return fridge;
  }
}
