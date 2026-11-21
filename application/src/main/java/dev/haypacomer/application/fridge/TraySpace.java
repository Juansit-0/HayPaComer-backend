package dev.haypacomer.application.fridge;

import dev.haypacomer.domain.fridge.TrayId;
import java.util.Objects;

public record TraySpace(TrayId tray, double capacityLiters, double usedLiters) {

  public TraySpace {
    Objects.requireNonNull(tray, "tray");
  }

  public double freeLiters() {
    return Math.max(0, capacityLiters - usedLiters);
  }
}
