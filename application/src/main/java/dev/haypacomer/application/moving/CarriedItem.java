package dev.haypacomer.application.moving;

import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.inventory.Visibility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** One food someone took along from the home they left, waiting for the next fridge. */
public record CarriedItem(
    UUID id,
    String food,
    BigDecimal grams,
    LocalDate expiresOn,
    ExpirySource expirySource,
    boolean opened,
    Visibility visibility,
    BigDecimal pieceGrams) {}
