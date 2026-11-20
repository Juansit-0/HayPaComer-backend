package dev.haypacomer.application.inventory;

import java.math.BigDecimal;
import java.util.Objects;

public record DensityEstimate(String food, BigDecimal gramsPerMilliliter, DensitySource source) {

  public DensityEstimate {
    Objects.requireNonNull(food, "food");
    Objects.requireNonNull(gramsPerMilliliter, "gramsPerMilliliter");
    Objects.requireNonNull(source, "source");
  }
}
