package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.Objects;

/** Turns the household code off, so nobody else joins with it. */
public final class RevokeJoinCode {

  private final HouseholdRepository households;
  private final JoinCodeRepository codes;

  public RevokeJoinCode(HouseholdRepository households, JoinCodeRepository codes) {
    this.households = Objects.requireNonNull(households, "households");
    this.codes = Objects.requireNonNull(codes, "codes");
  }

  public void revoke(UserId actor, HouseholdId id) {
    JoinCodes.requireManager(households, actor, id);
    codes.delete(id);
  }
}
