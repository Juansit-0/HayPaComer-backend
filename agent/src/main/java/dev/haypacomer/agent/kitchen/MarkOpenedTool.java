package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.inventory.MarkFoodOpened;
import dev.haypacomer.application.inventory.ViewInventory;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.household.Permission;
import java.util.List;
import java.util.Optional;

public final class MarkOpenedTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "mark_opened",
          "Propose marking the piece of a food that expires first as opened, which shortens its"
              + " expiry by the shelf life rules, after the person confirms it.",
          Permission.MANAGE_OWN_ITEMS,
          List.of(ParameterSpec.required("food", ParameterType.TEXT, "Catalog food name")));

  private final MarkFoodOpened open;
  private final FoodCatalogRepository catalog;
  private final ViewInventory inventory;
  private final KitchenToday today;

  public MarkOpenedTool(
      MarkFoodOpened open,
      FoodCatalogRepository catalog,
      ViewInventory inventory,
      KitchenToday today) {
    this.open = open;
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
    return "Mark "
        + KitchenFoods.firstInStock(catalog, inventory, today, invocation)
            .map(FoodItem::name)
            .orElse(invocation.arguments().get("food").strip())
        + " as opened";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    FoodItem item =
        KitchenFoods.firstInStock(catalog, inventory, today, invocation)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        KitchenFoods.missing(catalog, invocation.arguments().get("food"))));
    FoodItem opened = open.open(invocation.user(), invocation.household(), item.id());
    return Observation.of(
        SPEC.name(),
        "Marked "
            + opened.name()
            + " as opened"
            + opened.expiresOn().map(date -> "; now expires " + date).orElse(""));
  }
}
