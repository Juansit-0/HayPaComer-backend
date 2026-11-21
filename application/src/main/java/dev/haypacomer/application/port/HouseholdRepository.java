package dev.haypacomer.application.port;

import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.Optional;

public interface HouseholdRepository {

  void save(Household household);

  Optional<Household> findById(HouseholdId id);

  List<Household> findByUser(UserId user);

  default void delete(HouseholdId id) {
    throw new UnsupportedOperationException("This store cannot delete households");
  }
}
