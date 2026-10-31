package dev.haypacomer.domain.fridge;

public final class TrayNotEmptyException extends IllegalStateException {

  private final TrayId tray;

  public TrayNotEmptyException(Tray tray) {
    super("Tray " + tray.name() + " still holds food");
    this.tray = tray.id();
  }

  public TrayId tray() {
    return tray;
  }
}
