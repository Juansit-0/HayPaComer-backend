package dev.haypacomer.domain.fridge;

import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.quantity.Grams;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record FridgeMemento(FridgeId id, String name, List<ZoneState> zones) {

  public FridgeMemento {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(name, "name");
    zones = List.copyOf(zones);
  }

  public record ZoneState(ZoneId id, String name, ZoneKind kind, List<TrayState> trays) {

    public ZoneState {
      trays = List.copyOf(trays);
    }
  }

  public record TrayState(TrayId id, String name, int position, List<ItemState> items) {

    public TrayState {
      items = List.copyOf(items);
    }
  }

  public record ItemState(
      FoodItemId id,
      String foodName,
      Grams quantity,
      Grams tare,
      LocalDate expiresOn,
      ExpirySource expirySource,
      LocalDate openedOn) {

    public ItemState(
        FoodItemId id, String foodName, Grams quantity, Grams tare, LocalDate expiresOn) {
      this(id, foodName, quantity, tare, expiresOn, null, null);
    }
  }
}
