package dev.haypacomer.domain.market;

public final class MarketItemNotFoundException extends RuntimeException {

  public MarketItemNotFoundException() {
    super("Item is not on the market list");
  }
}
