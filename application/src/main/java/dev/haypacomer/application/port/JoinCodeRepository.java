package dev.haypacomer.application.port;

import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.JoinCode;
import java.util.Optional;

public interface JoinCodeRepository {

  /** Stores the household's code, replacing the one it had. */
  void save(JoinCode code);

  Optional<JoinCode> findByCode(String code);

  Optional<JoinCode> findByHousehold(HouseholdId household);

  void delete(HouseholdId household);
}
