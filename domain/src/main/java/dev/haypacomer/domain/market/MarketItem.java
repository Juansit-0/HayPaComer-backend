package dev.haypacomer.domain.market;

import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.Grams;
import java.time.Instant;
import java.util.Objects;

public record MarketItem(
    MarketItemId id,
    FoodMetadata food,
    Grams grams,
    MarketSource source,
    UserId addedBy,
    Instant addedAt,
    Instant checkedAt,
    Instant clearedAt) {

  public MarketItem {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(food, "food");
    Objects.requireNonNull(grams, "grams");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(addedBy, "addedBy");
    Objects.requireNonNull(addedAt, "addedAt");
    if (grams.isZero()) {
      throw new IllegalArgumentException("Market item needs a positive amount");
    }
  }

  public boolean isPending() {
    return checkedAt == null;
  }

  public boolean isInCart() {
    return checkedAt != null && clearedAt == null;
  }

  MarketItem withGrams(Grams newGrams) {
    return new MarketItem(id, food, newGrams, source, addedBy, addedAt, checkedAt, clearedAt);
  }

  MarketItem checked(Instant at) {
    return new MarketItem(id, food, grams, source, addedBy, addedAt, at, null);
  }

  MarketItem unchecked() {
    return new MarketItem(id, food, grams, source, addedBy, addedAt, null, null);
  }

  MarketItem cleared(Instant at) {
    return new MarketItem(id, food, grams, source, addedBy, addedAt, checkedAt, at);
  }
}
