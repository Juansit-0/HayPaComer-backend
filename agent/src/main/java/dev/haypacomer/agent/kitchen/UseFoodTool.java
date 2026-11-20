package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.inventory.CommandOutcome;
import dev.haypacomer.application.inventory.ConsumeFoodCommand;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.InventoryEntry;
import dev.haypacomer.application.inventory.ViewInventory;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.inventory.MovementSource;
import dev.haypacomer.domain.quantity.Grams;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UseFoodTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "use_food",
          "Propose discounting grams the person used of a food, from the piece that expires"
              + " first, after they confirm it.",
          Permission.MANAGE_OWN_ITEMS,
          List.of(
              ParameterSpec.required("food", ParameterType.TEXT, "Catalog food name"),
              ParameterSpec.required("grams", ParameterType.GRAMS, "Grams used")));

  private final ExecuteInventoryCommand commands;
  private final FoodCatalogRepository catalog;
  private final ViewInventory inventory;
  private final KitchenToday today;

  public UseFoodTool(
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
    String text = invocation.arguments().get("food");
    Optional<FoodMetadata> food = KitchenFoods.resolve(catalog, text);
    if (food.isEmpty()) {
      return Optional.of(KitchenFoods.unknown(text));
    }
    List<InventoryEntry> stock = KitchenFoods.inStock(inventory, today, invocation, food.get());
    if (stock.isEmpty()) {
      return Optional.of("No " + food.get().name() + " in stock");
    }
    if (item(invocation).isPresent()) {
      return Optional.empty();
    }
    Grams largest =
        stock.stream()
            .map(entry -> entry.food().item().quantity())
            .max(Grams::compareTo)
            .orElseThrow();
    return Optional.of(
        "Only " + largest + " of " + food.get().name() + " in one piece; use at most that");
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Use "
        + grams(invocation)
        + " of "
        + item(invocation).map(FoodItem::name).orElse(invocation.arguments().get("food").strip())
        + item(invocation)
            .flatMap(FoodItem::expiresOn)
            .map(date -> " (expires " + date + ")")
            .orElse("");
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    FoodItem item =
        item(invocation)
            .orElseThrow(
                () -> new IllegalStateException(problem(invocation).orElse("Not in stock")));
    CommandOutcome outcome =
        commands.execute(
            invocation.user(),
            new ConsumeFoodCommand(
                UUID.randomUUID(),
                invocation.household(),
                item.id(),
                grams(invocation),
                MovementSource.AGENT_CONFIRMED));
    return Observation.of(
        SPEC.name(),
        "Used " + grams(invocation) + " of " + item.name() + "; " + outcome.remaining() + " left");
  }

  private Optional<FoodItem> item(ToolInvocation invocation) {
    Grams grams = grams(invocation);
    return KitchenFoods.resolve(catalog, invocation.arguments().get("food"))
        .flatMap(
            food ->
                KitchenFoods.inStock(inventory, today, invocation, food).stream()
                    .map(entry -> entry.food().item())
                    .filter(item -> item.quantity().isAtLeast(grams))
                    .findFirst());
  }

  private static Grams grams(ToolInvocation invocation) {
    return Grams.of(invocation.arguments().get("grams").strip());
  }
}
