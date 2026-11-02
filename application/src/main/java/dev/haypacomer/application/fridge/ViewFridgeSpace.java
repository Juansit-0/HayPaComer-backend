package dev.haypacomer.application.fridge;

import dev.haypacomer.application.coldchain.FridgeNotFoundException;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.FridgeShapeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.Objects;

public final class ViewFridgeSpace {

  private final GetHousehold households;
  private final FridgeRepository fridges;
  private final FridgeShapeRepository shapes;

  public ViewFridgeSpace(
      HouseholdRepository households, FridgeRepository fridges, FridgeShapeRepository shapes) {
    this.households = new GetHousehold(households);
    this.fridges = Objects.requireNonNull(fridges, "fridges");
    this.shapes = Objects.requireNonNull(shapes, "shapes");
  }

  public List<TraySpace> view(UserId actor, HouseholdId household, FridgeId fridgeId) {
    households.get(actor, household);
    Fridge fridge =
        fridges.findByHousehold(household).stream()
            .filter(candidate -> candidate.id().equals(fridgeId))
            .findFirst()
            .orElseThrow(FridgeNotFoundException::new);
    FridgeShape shape =
        shapes
            .find(fridgeId)
            .map(stored -> stored.withCountsOf(fridge))
            .orElseGet(() -> FridgeShape.inferredFrom(fridge));
    return new FridgeRoom(shape).spaces(fridge);
  }
}
