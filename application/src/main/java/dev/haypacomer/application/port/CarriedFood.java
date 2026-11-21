package dev.haypacomer.application.port;

import dev.haypacomer.application.moving.CarriedItem;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.UUID;

public interface CarriedFood {

  int packUp(HouseholdId household, UserId user);

  List<CarriedItem> waiting(UserId user);

  void unpacked(UUID carriedId);
}
