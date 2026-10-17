package dev.haypacomer.application.ai;

import dev.haypacomer.domain.food.FoodCategory;
import java.util.Objects;

public record FoodVerdict(
    boolean food,
    String name,
    FoodCategory category,
    boolean perishable,
    int shelfDays,
    String reason,
    AdvisorSource source) {

  public FoodVerdict {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(source, "source");
    if (food) {
      Objects.requireNonNull(category, "category");
      if (shelfDays < 1 || shelfDays > 730) {
        throw new IllegalArgumentException("Shelf days must be between 1 and 730");
      }
    }
  }
}
