package dev.haypacomer.application.fridge;

import dev.haypacomer.domain.fridge.TrayId;
import java.util.Locale;

public final class TrayFullException extends IllegalStateException {

  private final TrayId tray;
  private final double freeLiters;

  public TrayFullException(TrayId tray, double freeLiters, double neededLiters) {
    super(
        String.format(
            Locale.ROOT,
            "This part of the fridge is full: %.1f L free, %.1f L needed",
            freeLiters,
            neededLiters));
    this.tray = tray;
    this.freeLiters = freeLiters;
  }

  public TrayId tray() {
    return tray;
  }

  public double freeLiters() {
    return freeLiters;
  }
}
