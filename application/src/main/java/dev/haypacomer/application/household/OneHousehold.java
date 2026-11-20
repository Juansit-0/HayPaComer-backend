package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.identity.UserId;

/** The rule that a person is in one home at a time. */
final class OneHousehold {

  private OneHousehold() {}

  static void requireNone(HouseholdRepository households, UserId user) {
    if (!households.findByUser(user).isEmpty()) {
      throw new AlreadyInHouseholdException();
    }
  }
}
