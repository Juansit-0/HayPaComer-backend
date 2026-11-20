package dev.haypacomer.domain.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.member.MemberId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccessRequestTest {

  private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

  private static AccessRequest open() {
    return AccessRequest.open(
        HouseholdId.newId(), FoodItemId.newId(), MemberId.newId(), MemberId.newId(), NOW);
  }

  @Test
  void aRequestIsAnsweredOnce() {
    AccessRequest approved = open().decide(true, NOW.plusSeconds(5));

    assertEquals(AccessRequest.Status.APPROVED, approved.status());
    assertFalse(approved.isPending());
    assertThrows(IllegalStateException.class, () -> approved.decide(false, NOW.plusSeconds(9)));
  }

  @Test
  void aNoIsKeptAsDenied() {
    AccessRequest request = open();
    assertTrue(request.isPending());
    assertEquals(AccessRequest.Status.DENIED, request.decide(false, NOW).status());
  }

  @Test
  void nobodyAsksForTheirOwnFood() {
    MemberId me = MemberId.newId();
    assertThrows(
        IllegalArgumentException.class,
        () -> AccessRequest.open(HouseholdId.newId(), FoodItemId.newId(), me, me, NOW));
  }
}
