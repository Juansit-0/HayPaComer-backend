package dev.haypacomer.application.inventory;

import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.fridge.TrayId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.inventory.MovementSource;
import dev.haypacomer.domain.inventory.MovementType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record MoveFoodCommand(UUID id, HouseholdId household, FoodItemId item, TrayId tray)
    implements InventoryCommand {

  public MoveFoodCommand {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(household, "household");
    Objects.requireNonNull(item, "item");
    Objects.requireNonNull(tray, "tray");
  }

  @Override
  public String action() {
    return "MOVE_FOOD";
  }

  @Override
  public Map<String, String> detail() {
    return Map.of("tray", tray.value().toString());
  }

  @Override
  public CommandOutcome execute(InventoryWorkspace workspace) {
    HouseholdInventory.Located located = workspace.inventory().locate(household, item);
    workspace
        .guard()
        .requireUsable(
            workspace.household(),
            workspace.actor(),
            workspace.inventory().ownerships().find(item));
    if (located.fridge().findTray(tray).isEmpty()) {
      throw new IllegalArgumentException("Tray not in the fridge that holds this food");
    }
    located.fridge().move(item, tray);
    workspace.inventory().save(household, located.fridge());
    FoodItem moved = located.fridge().findItem(item).orElseThrow();
    workspace
        .inventory()
        .record(
            id,
            household,
            moved,
            workspace.actor(),
            MovementType.ADJUST,
            BigDecimal.ZERO,
            MovementSource.MANUAL,
            workspace.now());
    return new CommandOutcome(id, item, moved.quantity(), false);
  }
}
