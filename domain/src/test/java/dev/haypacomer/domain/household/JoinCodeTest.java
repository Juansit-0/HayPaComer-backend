package dev.haypacomer.domain.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class JoinCodeTest {

  private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

  @Test
  void aNewCodeHasEightUnambiguousCharactersAndLastsAWeek() {
    JoinCode code =
        JoinCode.create(
            HouseholdId.newId(), Role.MEMBER, UserId.newId(), NOW, new SplittableRandom(7));

    assertEquals(JoinCode.LENGTH, code.code().length());
    assertTrue(code.code().chars().allMatch(c -> JoinCode.ALPHABET.indexOf(c) >= 0));
    assertTrue(code.code().chars().noneMatch(c -> "O0I1".indexOf(c) >= 0));
    assertEquals(NOW.plus(JoinCode.TIME_TO_LIVE), code.expiresAt());
    assertTrue(code.isActive(NOW));
    assertFalse(code.isActive(code.expiresAt()));
  }

  @Test
  void theCodeIsShownInTwoGroupsAndReadBackHoweverItIsTyped() {
    JoinCode code =
        new JoinCode(
            HouseholdId.newId(), "ABCDEFGH", Role.MEMBER, UserId.newId(), NOW, NOW.plusSeconds(60));

    assertEquals("ABCD-EFGH", code.display());
    assertEquals("ABCDEFGH", JoinCode.normalize(" abcd-efgh "));
    assertEquals("", JoinCode.normalize(null));
  }

  @Test
  void aCodeNeverMakesSomeoneTheOwner() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new JoinCode(
                HouseholdId.newId(),
                "ABCDEFGH",
                Role.OWNER,
                UserId.newId(),
                NOW,
                NOW.plusSeconds(60)));
  }
}
