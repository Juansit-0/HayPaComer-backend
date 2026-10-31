package dev.haypacomer.application.fridge;

import dev.haypacomer.application.audit.AuditEntry;
import dev.haypacomer.application.coldchain.FridgeNotFoundException;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.AuditLog;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.FridgeShapeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;

public final class ChangeFridgeShape {

  private final GetHousehold households;
  private final FridgeRepository fridges;
  private final FridgeShapeRepository shapes;
  private final AuditLog audit;
  private final UnitOfWork unitOfWork;
  private final Clock clock;

  public ChangeFridgeShape(
      HouseholdRepository households,
      FridgeRepository fridges,
      FridgeShapeRepository shapes,
      AuditLog audit,
      UnitOfWork unitOfWork,
      Clock clock) {
    this.households = new GetHousehold(households);
    this.fridges = Objects.requireNonNull(fridges, "fridges");
    this.shapes = Objects.requireNonNull(shapes, "shapes");
    this.audit = Objects.requireNonNull(audit, "audit");
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public FridgeShape change(
      UserId actor, HouseholdId household, FridgeId fridgeId, FridgeShape shape) {
    Objects.requireNonNull(shape, "shape");
    households.get(actor, household).requirePermission(actor, Permission.MANAGE_FRIDGES);
    return unitOfWork.runFor(
        household,
        () -> {
          Fridge fridge =
              fridges.findByHousehold(household).stream()
                  .filter(candidate -> candidate.id().equals(fridgeId))
                  .findFirst()
                  .orElseThrow(FridgeNotFoundException::new);
          shape.fit(fridge);
          fridges.save(household, fridge);
          shapes.save(fridgeId, shape);
          audit.record(
              new AuditEntry(
                  actor,
                  household,
                  "CHANGE_FRIDGE_SHAPE",
                  "FRIDGE",
                  fridgeId.value(),
                  Map.of(
                      "type", shape.type().name(),
                      "shelves", String.valueOf(shape.shelves()),
                      "doorRacks", String.valueOf(shape.doorRacks()),
                      "drawers", String.valueOf(shape.drawers()),
                      "freezerShelves", String.valueOf(shape.freezerShelves())),
                  clock.instant()));
          return shape.withCountsOf(fridge);
        });
  }
}
