package dev.haypacomer.agent.supervisor;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Specialist(
    String name, String purpose, Set<String> tools, List<String> offlineReads) {

  public Specialist {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(purpose, "purpose");
    tools = Set.copyOf(tools);
    offlineReads = List.copyOf(offlineReads);
    if (!tools.containsAll(offlineReads)) {
      throw new IllegalArgumentException("Offline reads must be tools of " + name);
    }
  }

  public static final Specialist CHEF =
      new Specialist(
          "chef",
          "What to cook now with measured stock, rescuing food that expires first.",
          Set.of(
              "recall_memory",
              "query_inventory",
              "view_expiries",
              "view_weekly_plan",
              "remember",
              "estimate_expiry",
              "stock_food",
              "use_food",
              "discard_food",
              "mark_opened",
              "undo_last_change",
              "list_recipes",
              "suggest_dishes",
              "start_cooking",
              "cooking_step",
              "view_cooking",
              "generate_week_plan"),
          List.of("view_expiries", "query_inventory", "recall_memory"));

  public static final Specialist MARKET =
      new Specialist(
          "market",
          "What to buy: the market list, the plan, and what is missing at home.",
          Set.of(
              "view_market_list",
              "view_weekly_plan",
              "query_inventory",
              "recall_memory",
              "add_to_market",
              "review_budget",
              "update_market",
              "clear_checked_market",
              "set_market_budget",
              "missing_to_market",
              "plan_to_market",
              "generate_week_plan"),
          List.of("view_market_list", "review_budget", "view_weekly_plan"));

  public static final Specialist COLD =
      new Specialist(
          "cold",
          "Fridge temperature, cold chain incidents, and which food needs review.",
          Set.of("view_cold_chain", "investigate_cold", "view_expiries"),
          List.of("view_cold_chain", "investigate_cold", "view_expiries"));

  public static final Specialist COACH =
      new Specialist(
          "coach",
          "Habits that reduce waste, based on what expires and household notes.",
          Set.of(
              "view_expiries",
              "waste_patterns",
              "query_inventory",
              "recall_memory",
              "remember",
              "stock_food",
              "use_food",
              "discard_food",
              "mark_opened",
              "undo_last_change"),
          List.of("view_expiries", "waste_patterns", "recall_memory"));

  public static final List<Specialist> ALL = List.of(CHEF, MARKET, COLD, COACH);
}
