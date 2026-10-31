package dev.haypacomer.application.fridge;

public enum FridgeType {
  MINIBAR(43, 58, 49, 1),
  SINGLE_DOOR(55, 145, 60, 1),
  TOP_FREEZER(60, 165, 65, 4),
  BOTTOM_FREEZER(60, 185, 65, 4),
  SIDE_BY_SIDE(90, 178, 70, 6);

  private final int widthCm;
  private final int heightCm;
  private final int depthCm;
  private final int maxFreezerShelves;

  FridgeType(int widthCm, int heightCm, int depthCm, int maxFreezerShelves) {
    this.widthCm = widthCm;
    this.heightCm = heightCm;
    this.depthCm = depthCm;
    this.maxFreezerShelves = maxFreezerShelves;
  }

  public int widthCm() {
    return widthCm;
  }

  public int heightCm() {
    return heightCm;
  }

  public int depthCm() {
    return depthCm;
  }

  public int maxFreezerShelves() {
    return maxFreezerShelves;
  }

  public boolean needsFreezer() {
    return this == TOP_FREEZER || this == BOTTOM_FREEZER || this == SIDE_BY_SIDE;
  }
}
