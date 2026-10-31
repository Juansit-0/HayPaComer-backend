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
import java.util.Objects;

public final class ViewFridgeShape {

  private final GetHousehold households;
  private final FridgeRepository fridges;
  private final FridgeShapeRepository shapes;

  public ViewFridgeShape(
      HouseholdRepository households, FridgeRepository fridges, FridgeShapeRepository shapes) {
    this.households = new GetHousehold(households);
    this.fridges = Objects.requireNonNull(fridges, "fridges");
    this.shapes = Objects.requireNonNull(shapes, "shapes");
  }

  public FridgeShape view(UserId actor, HouseholdId household, FridgeId fridgeId) {
    households.get(actor, household);
    Fridge fridge =
        fridges.findByHousehold(household).stream()
            .filter(candidate -> candidate.id().equals(fridgeId))
            .findFirst()
            .orElseThrow(FridgeNotFoundException::new);
    return shapes
        .find(fridgeId)
        .map(shape -> shape.withCountsOf(fridge))
        .orElseGet(() -> FridgeShape.inferredFrom(fridge));
  }
}
