package dev.haypacomer.application.inventory;

import dev.haypacomer.application.audit.AuditEntry;
import dev.haypacomer.application.port.AuditLog;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.application.port.FoodOwnershipRepository;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InventoryMovementLog;
import dev.haypacomer.application.port.SnapshotStore;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.fridge.ZoneKind;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MarkFoodOpened {

  private final HouseholdInventory inventory;
  private final FoodAccessGuard guard;
  private final ExpiryDesk expiry;
  private final AuditLog audit;
  private final UnitOfWork unitOfWork;
  private final InventoryCaretaker caretaker;
  private final SnapshotStore snapshots;
  private final Clock clock;

  public MarkFoodOpened(
      HouseholdRepository households,
      FridgeRepository fridges,
      FoodOwnershipRepository ownerships,
      InventoryMovementLog movements,
      FoodCatalogRepository catalog,
      FoodAccessGuard guard,
      ExpiryDesk expiry,
      AuditLog audit,
      UnitOfWork unitOfWork,
      SnapshotStore snapshots,
      Clock clock) {
    this.inventory = new HouseholdInventory(households, fridges, ownerships, movements);
    this.guard = Objects.requireNonNull(guard, "guard");
    this.expiry = Objects.requireNonNull(expiry, "expiry");
    this.audit = Objects.requireNonNull(audit, "audit");
    this.unitOfWork = Objects.requireNonNull(unitOfWork, "unitOfWork");
    this.caretaker = new InventoryCaretaker(inventory, catalog);
    this.snapshots = Objects.requireNonNull(snapshots, "snapshots");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public FoodItem open(UserId actor, HouseholdId householdId, FoodItemId itemId) {
    Household household = inventory.household(actor, householdId);
    return unitOfWork.runFor(
        householdId,
        () -> {
          HouseholdInventory.Located located = inventory.locate(householdId, itemId);
          guard.requireUsable(household, actor, inventory.ownerships().find(itemId));
          FoodItem item = located.item();
          ZoneKind zone =
              located.fridge().zoneHolding(itemId).map(Zone::kind).orElse(ZoneKind.SHELF);
          Instant now = clock.instant();
          LocalDate today = LocalDate.ofInstant(now, household.timezone());
          LocalDate current = item.expiresOn().orElse(null);
          LocalDate after = expiry.afterOpening(householdId, item.food(), zone, current, today);
          ExpirySource source =
              after.equals(current)
                  ? item.expirySource().orElse(ExpirySource.USER)
                  : ExpirySource.ESTIMATED;
          InventoryMemento before = caretaker.capture(householdId);
          item.open(today, after, source);
          inventory.save(householdId, located.fridge());
          snapshots.save(
              new InventorySnapshot(
                  UUID.randomUUID(),
                  householdId,
                  SnapshotKind.UNDO,
                  UUID.randomUUID(),
                  actor,
                  "OPEN_FOOD",
                  before,
                  now,
                  null));
          audit.record(
              new AuditEntry(
                  actor,
                  householdId,
                  "OPEN_FOOD",
                  "FOOD_ITEM",
                  itemId.value(),
                  Map.of("expiresOn", after.toString(), "source", source.name()),
                  now));
          return item;
        });
  }
}
