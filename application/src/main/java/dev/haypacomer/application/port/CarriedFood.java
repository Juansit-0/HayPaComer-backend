package dev.haypacomer.application.port;

import dev.haypacomer.application.moving.CarriedItem;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.UUID;

/** The food people take along when they leave a home, until it goes into their next fridge. */
public interface CarriedFood {

  /**
   * Takes the user's own food out of the home's fridges and keeps it for them; returns how many.
   */
  int packUp(HouseholdId household, UserId user);

  List<CarriedItem> waiting(UserId user);

  void unpacked(UUID carriedId);
}
