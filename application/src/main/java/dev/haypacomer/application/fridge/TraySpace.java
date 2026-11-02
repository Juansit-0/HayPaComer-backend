package dev.haypacomer.application.fridge;

import dev.haypacomer.domain.fridge.TrayId;
import java.util.Objects;

// How much a tray holds and how much of it the food already takes, in litres.
public record TraySpace(TrayId tray, double capacityLiters, double usedLiters) {

  public TraySpace {
    Objects.requireNonNull(tray, "tray");
  }

  public double freeLiters() {
    return Math.max(0, capacityLiters - usedLiters);
  }
}
