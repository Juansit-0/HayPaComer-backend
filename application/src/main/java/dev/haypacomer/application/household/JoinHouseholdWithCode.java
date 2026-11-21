package dev.haypacomer.application.household;

import dev.haypacomer.application.market.DirectWork;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.JoinCode;
import dev.haypacomer.domain.identity.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class JoinHouseholdWithCode {

  private final JoinCodeRepository codes;
  private final HouseholdRepository households;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  public JoinHouseholdWithCode(
      JoinCodeRepository codes, HouseholdRepository households, Clock clock) {
    this(codes, households, new DirectWork(), clock);
  }

  public JoinHouseholdWithCode(
      JoinCodeRepository codes,
      HouseholdRepository households,
      UnitOfWork unitOfWork,
      Clock clock) {
    this.codes = Objects.requireNonNull(codes, "codes");
    this.households = Objects.requireNonNull(households, "households");
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public Household join(UserId actor, String typed) {
    Instant now = clock.instant();
    JoinCode code = active(typed, now);
    return unitOfWork.runFor(code.household(), () -> joinInTurn(actor, typed, now));
  }

  private JoinCode active(String typed, Instant now) {
    String normalized = JoinCode.normalize(typed);
    if (normalized.length() != JoinCode.LENGTH) {
      throw new JoinCodeInvalidException();
    }
    return codes
        .findByCode(normalized)
        .filter(code -> code.isActive(now))
        .orElseThrow(JoinCodeInvalidException::new);
  }

  private Household joinInTurn(UserId actor, String typed, Instant now) {
    JoinCode code = active(typed, now);
    Household household =
        households.findById(code.household()).orElseThrow(JoinCodeInvalidException::new);
    if (household.membershipOf(actor).isPresent()) {
      throw new IllegalStateException("You already belong to this household");
    }
    OneHousehold.requireNone(households, actor);
    household.join(actor, code.role(), now);
    households.save(household);
    return household;
  }
}
