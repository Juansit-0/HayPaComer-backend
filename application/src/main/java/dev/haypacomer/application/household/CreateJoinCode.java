package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.JoinCode;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Makes the code people type to join the household and share its fridge, replacing the old one. */
public final class CreateJoinCode {

  private final HouseholdRepository households;
  private final JoinCodeRepository codes;
  private final Clock clock;
  private final RandomGenerator random;

  public CreateJoinCode(HouseholdRepository households, JoinCodeRepository codes, Clock clock) {
    this(households, codes, clock, new SecureRandom());
  }

  public CreateJoinCode(
      HouseholdRepository households,
      JoinCodeRepository codes,
      Clock clock,
      RandomGenerator random) {
    this.households = Objects.requireNonNull(households, "households");
    this.codes = Objects.requireNonNull(codes, "codes");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.random = Objects.requireNonNull(random, "random");
  }

  public JoinCode create(UserId actor, HouseholdId id, Role role) {
    JoinCodes.requireManager(households, actor, id);
    JoinCode code =
        JoinCode.create(id, role == null ? Role.MEMBER : role, actor, clock.instant(), random);
    codes.save(code);
    return code;
  }
}
