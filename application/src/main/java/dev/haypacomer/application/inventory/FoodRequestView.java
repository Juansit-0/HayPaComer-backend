package dev.haypacomer.application.inventory;

import dev.haypacomer.domain.inventory.AccessRequest;
import java.time.Instant;
import java.util.UUID;

public record FoodRequestView(
    UUID id,
    boolean toAnswer,
    UUID itemId,
    String food,
    UUID requesterUserId,
    String requesterName,
    UUID ownerUserId,
    String ownerName,
    AccessRequest.Status status,
    Instant createdAt,
    Instant decidedAt) {}
