package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.inventory.DiscardFoodCommand;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.ViewInventory;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.household.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DiscardFoodTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "discard_food",
          "Propose throwing away the piece of a food that expires first, after the person"
              + " confirms it.",
          Permission.MANAGE_OWN_ITEMS,
          List.of(ParameterSpec.required("food", ParameterType.TEXT, "Catalog food name")));

  private final ExecuteInventoryCommand commands;
  private final FoodCatalogRepository catalog;
  private final ViewInventory inventory;
  private final KitchenToday today;

  public DiscardFoodTool(
      ExecuteInventoryCommand commands,
      FoodCatalogRepository catalog,
      ViewInventory inventory,
      KitchenToday today) {
    this.commands = commands;
    this.catalog = catalog;
    this.inventory = inventory;
    this.today = today;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    return KitchenFoods.firstInStock(catalog, inventory, today, invocation).isPresent()
        ? Optional.empty()
        : Optional.of(KitchenFoods.missing(catalog, invocation.arguments().get("food")));
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return KitchenFoods.firstInStock(catalog, inventory, today, invocation)
        .map(item -> "Throw away " + item.quantity() + " of " + item.name())
        .orElse("Throw away " + invocation.arguments().get("food").strip());
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    FoodItem item =
        KitchenFoods.firstInStock(catalog, inventory, today, invocation)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        KitchenFoods.missing(catalog, invocation.arguments().get("food"))));
    commands.execute(
        invocation.user(),
        new DiscardFoodCommand(UUID.randomUUID(), invocation.household(), item.id()));
    return Observation.of(SPEC.name(), "Threw away " + item.quantity() + " of " + item.name());
  }
}
