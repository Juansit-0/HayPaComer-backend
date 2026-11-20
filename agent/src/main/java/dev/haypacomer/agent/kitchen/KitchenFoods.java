package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.application.inventory.InventoryEntry;
import dev.haypacomer.application.inventory.ViewInventory;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.FoodItem;
import java.util.List;
import java.util.Optional;

final class KitchenFoods {

  private KitchenFoods() {}

  static Optional<FoodMetadata> resolve(FoodCatalogRepository catalog, String text) {
    String food = text.strip();
    return lookup(catalog, food)
        .or(
            () ->
                food.endsWith("es")
                    ? lookup(catalog, food.substring(0, food.length() - 2))
                    : Optional.empty())
        .or(
            () ->
                food.endsWith("s")
                    ? lookup(catalog, food.substring(0, food.length() - 1))
                    : Optional.empty());
  }

  static String unknown(String food) {
    return "Unknown food " + food.strip() + "; use a catalog name";
  }

  static List<InventoryEntry> inStock(
      ViewInventory inventory, KitchenToday today, ToolInvocation invocation, FoodMetadata food) {
    return inventory
        .view(invocation.user(), invocation.household(), today.of(invocation.household()))
        .stream()
        .filter(InventoryEntry::usable)
        .filter(entry -> entry.food().item().food().key().equals(food.key()))
        .toList();
  }

  static Optional<FoodItem> firstInStock(
      FoodCatalogRepository catalog,
      ViewInventory inventory,
      KitchenToday today,
      ToolInvocation invocation) {
    return resolve(catalog, invocation.arguments().get("food"))
        .flatMap(
            food ->
                inStock(inventory, today, invocation, food).stream()
                    .map(entry -> entry.food().item())
                    .findFirst());
  }

  static String missing(FoodCatalogRepository catalog, String text) {
    return resolve(catalog, text)
        .map(food -> "No " + food.name() + " in stock")
        .orElse(unknown(text));
  }

  private static Optional<FoodMetadata> lookup(FoodCatalogRepository catalog, String food) {
    return food.isBlank()
        ? Optional.empty()
        : catalog.findByName(food).or(() -> catalog.search(food, 1).stream().findFirst());
  }
}
