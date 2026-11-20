package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.market.UpdateMarketList;
import dev.haypacomer.application.market.ViewMarketList;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.market.MarketItem;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public final class UpdateMarketTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "update_market",
          "Propose marking a market list food as bought (CHECK), back to buy (UNCHECK), or"
              + " removing it (REMOVE), after the person confirms it.",
          Permission.MANAGE_MARKET_LIST,
          List.of(
              ParameterSpec.required("food", ParameterType.TEXT, "Catalog food name"),
              ParameterSpec.required("action", ParameterType.TEXT, "CHECK, UNCHECK, or REMOVE")));

  private enum Action {
    CHECK,
    UNCHECK,
    REMOVE
  }

  private final UpdateMarketList update;
  private final ViewMarketList view;
  private final FoodCatalogRepository catalog;

  public UpdateMarketTool(
      UpdateMarketList update, ViewMarketList view, FoodCatalogRepository catalog) {
    this.update = update;
    this.view = view;
    this.catalog = catalog;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    Optional<Action> action = action(invocation);
    if (action.isEmpty()) {
      return Optional.of("action must be CHECK, UNCHECK, or REMOVE");
    }
    String text = invocation.arguments().get("food");
    Optional<FoodMetadata> food = KitchenFoods.resolve(catalog, text);
    if (food.isEmpty()) {
      return Optional.of(KitchenFoods.unknown(text));
    }
    if (item(invocation).isPresent()) {
      return Optional.empty();
    }
    return Optional.of(
        switch (action.get()) {
          case CHECK -> food.get().name() + " is not pending on the market list";
          case UNCHECK -> food.get().name() + " is not in the cart";
          case REMOVE -> food.get().name() + " is not on the market list";
        });
  }

  @Override
  public String describe(ToolInvocation invocation) {
    String food =
        item(invocation)
            .map(item -> item.grams() + " of " + item.food().name())
            .orElse(invocation.arguments().get("food").strip());
    return switch (action(invocation).orElse(Action.CHECK)) {
      case CHECK -> "Mark " + food + " as bought";
      case UNCHECK -> "Put " + food + " back on the market list";
      case REMOVE -> "Remove " + food + " from the market list";
    };
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    MarketItem item =
        item(invocation)
            .orElseThrow(() -> new IllegalStateException(problem(invocation).orElse("Not found")));
    Action action = action(invocation).orElseThrow();
    update.update(
        invocation.user(),
        invocation.household(),
        switch (action) {
          case CHECK -> new UpdateMarketList.Check(item.id());
          case UNCHECK -> new UpdateMarketList.Uncheck(item.id());
          case REMOVE -> new UpdateMarketList.Remove(item.id());
        });
    String food = item.grams() + " of " + item.food().name();
    return Observation.of(
        SPEC.name(),
        switch (action) {
          case CHECK -> "Marked " + food + " as bought";
          case UNCHECK -> "Put " + food + " back on the market list";
          case REMOVE -> "Removed " + food + " from the market list";
        });
  }

  private static Optional<Action> action(ToolInvocation invocation) {
    try {
      return Optional.of(
          Action.valueOf(invocation.arguments().get("action").strip().toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException unknown) {
      return Optional.empty();
    }
  }

  private Optional<MarketItem> item(ToolInvocation invocation) {
    Optional<Action> action = action(invocation);
    Optional<FoodMetadata> food = KitchenFoods.resolve(catalog, invocation.arguments().get("food"));
    if (action.isEmpty() || food.isEmpty()) {
      return Optional.empty();
    }
    List<MarketItem> items =
        view.view(invocation.user(), invocation.household()).items().stream()
            .filter(item -> item.food().key().equals(food.get().key()))
            .toList();
    Predicate<MarketItem> wanted =
        switch (action.get()) {
          case CHECK -> MarketItem::isPending;
          case UNCHECK -> MarketItem::isInCart;
          case REMOVE -> item -> item.isPending() || item.isInCart();
        };
    return items.stream()
        .filter(wanted)
        .sorted((left, right) -> Boolean.compare(right.isPending(), left.isPending()))
        .findFirst();
  }
}
