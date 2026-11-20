package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.market.UpdateMarketList;
import dev.haypacomer.application.market.ViewMarketList;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.market.MarketItem;
import java.util.List;
import java.util.Optional;

public final class ClearCheckedMarketTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "clear_checked_market",
          "Propose clearing the bought food out of the market cart (kept for the month's"
              + " spending), after the person confirms it.",
          Permission.MANAGE_MARKET_LIST,
          List.of());

  private final UpdateMarketList update;
  private final ViewMarketList view;

  public ClearCheckedMarketTool(UpdateMarketList update, ViewMarketList view) {
    this.update = update;
    this.view = view;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    return inCart(invocation) > 0 ? Optional.empty() : Optional.of("The cart is already empty");
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Clear " + inCart(invocation) + " bought foods from the market cart";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    long cleared = inCart(invocation);
    update.update(invocation.user(), invocation.household(), new UpdateMarketList.ClearChecked());
    return Observation.of(SPEC.name(), "Cleared " + cleared + " bought foods from the cart");
  }

  private long inCart(ToolInvocation invocation) {
    return view.view(invocation.user(), invocation.household()).items().stream()
        .filter(MarketItem::isInCart)
        .count();
  }
}
