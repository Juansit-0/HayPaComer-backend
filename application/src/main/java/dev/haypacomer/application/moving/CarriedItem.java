package dev.haypacomer.application.moving;

import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.inventory.Visibility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CarriedItem(
    UUID id,
    String food,
    BigDecimal grams,
    LocalDate expiresOn,
    ExpirySource expirySource,
    boolean opened,
    Visibility visibility,
    BigDecimal pieceGrams) {}
