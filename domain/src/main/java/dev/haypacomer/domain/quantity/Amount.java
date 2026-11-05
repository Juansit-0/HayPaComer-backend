package dev.haypacomer.domain.quantity;

import java.math.BigDecimal;
import java.util.Objects;

public record Amount(Quantity quantity) implements QuantityExpression {

  private static final BigDecimal MAX_GRAMS = BigDecimal.valueOf(100_000);

  public Amount {
    Objects.requireNonNull(quantity, "quantity");
  }

  @Override
  public Grams interpret(ConversionFactors factors) {
    Grams grams = quantity.toGrams(factors);
    if (grams.value().compareTo(MAX_GRAMS) > 0) {
      throw new InvalidQuantityException("A quantity holds up to 100 kg");
    }
    return grams;
  }

  @Override
  public String toString() {
    return quantity.toString();
  }
}
