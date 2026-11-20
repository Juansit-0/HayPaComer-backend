package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.market.SetMarketBudget;
import dev.haypacomer.domain.household.Permission;
import java.math.BigDecimal;
import java.util.List;

public final class SetMarketBudgetTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "set_market_budget",
          "Propose setting the monthly market budget in the household currency, after the"
              + " person confirms it.",
          Permission.MANAGE_MARKET_LIST,
          List.of(
              ParameterSpec.required(
                  "amount", ParameterType.MONEY, "Monthly amount in the household currency")));

  private final SetMarketBudget budget;

  public SetMarketBudgetTool(SetMarketBudget budget) {
    this.budget = budget;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Set the monthly market budget to " + amount(invocation).toPlainString();
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    BigDecimal saved = budget.set(invocation.user(), invocation.household(), amount(invocation));
    return Observation.of(
        SPEC.name(), "Monthly market budget set to " + saved.stripTrailingZeros().toPlainString());
  }

  private static BigDecimal amount(ToolInvocation invocation) {
    return new BigDecimal(invocation.arguments().get("amount").strip());
  }
}
