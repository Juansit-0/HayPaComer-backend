package dev.haypacomer.application.inventory;

import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeMemento;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.inventory.Ownership;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class InventoryCaretaker {

  private final HouseholdInventory inventory;
  private final FoodCatalogRepository catalog;

  InventoryCaretaker(HouseholdInventory inventory, FoodCatalogRepository catalog) {
    this.inventory = Objects.requireNonNull(inventory, "inventory");
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  InventoryMemento capture(HouseholdId household) {
    List<Fridge> fridges = inventory.fridges(household);
    Map<FoodItemId, Ownership> ownerships = new HashMap<>();
    fridges.forEach(
        fridge ->
            fridge
                .foodItems()
                .forEach(
                    item ->
                        inventory
                            .ownerships()
                            .find(item.id())
                            .ifPresent(ownership -> ownerships.put(item.id(), ownership))));
    return new InventoryMemento(fridges.stream().map(Fridge::snapshot).toList(), ownerships);
  }

  void restore(HouseholdId household, InventoryMemento memento) {
    Map<String, FoodMetadata> foods = new HashMap<>();
    for (FridgeMemento fridge : memento.fridges()) {
      inventory.save(
          household,
          Fridge.restoredFrom(
              fridge,
              name ->
                  foods.computeIfAbsent(
                      name,
                      key ->
                          catalog
                              .findByName(key)
                              .orElseThrow(() -> new FoodNotInCatalogException(key)))));
    }
    // Who may see a food is its owner's current decision: only food coming back recovers its old
    // terms.
    memento
        .ownerships()
        .forEach(
            (item, ownership) -> {
              if (inventory.ownerships().find(item).isEmpty()) {
                inventory.ownerships().save(item, ownership);
              }
            });
  }
}
