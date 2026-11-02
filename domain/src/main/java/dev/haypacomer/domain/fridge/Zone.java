package dev.haypacomer.domain.fridge;

import dev.haypacomer.domain.quantity.Grams;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Zone implements FridgeNode {

  private final ZoneId id;
  private final String name;
  private final ZoneKind kind;
  private final List<Tray> trays = new ArrayList<>();

  public Zone(ZoneId id, String name, ZoneKind kind) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Names.require(name, "Zone name");
    this.kind = Objects.requireNonNull(kind, "kind");
  }

  public static Zone named(String name, ZoneKind kind) {
    return new Zone(ZoneId.newId(), name, kind);
  }

  public ZoneId id() {
    return id;
  }

  public ZoneKind kind() {
    return kind;
  }

  public void add(Tray tray) {
    Objects.requireNonNull(tray, "tray");
    if (find(tray.id()).isPresent()) {
      throw new IllegalArgumentException("Tray already in zone " + name + ": " + tray.id());
    }
    trays.add(tray);
  }

  public Tray remove(TrayId trayId) {
    Tray tray =
        find(trayId)
            .orElseThrow(
                () -> new IllegalArgumentException("Tray not in zone " + name + ": " + trayId));
    if (tray.itemCount() > 0) {
      throw new TrayNotEmptyException(tray);
    }
    trays.remove(tray);
    return tray;
  }

  // A tray keeps its id, position, and food under its new name.
  public void renameTray(TrayId trayId, String name) {
    Tray tray =
        find(trayId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Tray not in zone " + this.name + ": " + trayId));
    Tray renamed = new Tray(tray.id(), name, tray.position());
    tray.children().forEach(renamed::add);
    trays.set(trays.indexOf(tray), renamed);
  }

  public Optional<Tray> find(TrayId trayId) {
    return trays.stream().filter(tray -> tray.id().equals(trayId)).findFirst();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Grams totalGrams() {
    return trays.stream().map(Tray::totalGrams).reduce(Grams.ZERO, Grams::plus);
  }

  @Override
  public int itemCount() {
    return trays.stream().mapToInt(Tray::itemCount).sum();
  }

  @Override
  public List<Tray> children() {
    return List.copyOf(trays);
  }
}
