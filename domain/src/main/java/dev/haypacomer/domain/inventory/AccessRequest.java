package dev.haypacomer.domain.inventory;

import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.member.MemberId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Someone asking the person who stocked a food marked "ask first" whether they may use it. The
 * owner answers once; a yes adds the asker to the food's grants.
 */
public record AccessRequest(
    UUID id,
    HouseholdId household,
    FoodItemId item,
    MemberId requester,
    MemberId owner,
    Status status,
    Instant createdAt,
    Instant decidedAt) {

  public enum Status {
    PENDING,
    APPROVED,
    DENIED
  }

  public AccessRequest {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(household, "household");
    Objects.requireNonNull(item, "item");
    Objects.requireNonNull(requester, "requester");
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(createdAt, "createdAt");
    if (requester.equals(owner)) {
      throw new IllegalArgumentException("Nobody needs to ask for their own food");
    }
  }

  public static AccessRequest open(
      HouseholdId household, FoodItemId item, MemberId requester, MemberId owner, Instant now) {
    return new AccessRequest(
        UUID.randomUUID(), household, item, requester, owner, Status.PENDING, now, null);
  }

  public boolean isPending() {
    return status == Status.PENDING;
  }

  public AccessRequest decide(boolean approve, Instant at) {
    if (!isPending()) {
      throw new IllegalStateException("This request was already answered");
    }
    return new AccessRequest(
        id,
        household,
        item,
        requester,
        owner,
        approve ? Status.APPROVED : Status.DENIED,
        createdAt,
        at);
  }
}
