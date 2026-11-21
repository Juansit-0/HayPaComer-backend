package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.JoinCode;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class ViewJoinCode {

  private final HouseholdRepository households;
  private final JoinCodeRepository codes;
  private final Clock clock;
  private final RandomGenerator random;

  public ViewJoinCode(HouseholdRepository households, JoinCodeRepository codes, Clock clock) {
    this(households, codes, clock, new SecureRandom());
  }

  public ViewJoinCode(
      HouseholdRepository households,
      JoinCodeRepository codes,
      Clock clock,
      RandomGenerator random) {
    this.households = Objects.requireNonNull(households, "households");
    this.codes = Objects.requireNonNull(codes, "codes");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.random = Objects.requireNonNull(random, "random");
  }

  public JoinCode current(UserId actor, HouseholdId id) {
    new MemberHouseholds(households).load(actor, id);
    Instant now = clock.instant();
    return codes
        .findByHousehold(id)
        .filter(code -> code.isActive(now))
        .orElseGet(
            () -> {
              JoinCode code = JoinCode.create(id, Role.MEMBER, actor, now, random);
              codes.save(code);
              return code;
            });
  }
}
