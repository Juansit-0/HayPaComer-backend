package dev.haypacomer.application.port;

import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.inventory.AccessRequest;
import dev.haypacomer.domain.member.MemberId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessRequestRepository {

  void save(AccessRequest request);

  Optional<AccessRequest> find(HouseholdId household, UUID id);

  Optional<AccessRequest> findPending(FoodItemId item, MemberId requester);

  /** Pending requests the member has to answer, and the member's own recent requests. */
  List<AccessRequest> involving(HouseholdId household, MemberId member);
}
