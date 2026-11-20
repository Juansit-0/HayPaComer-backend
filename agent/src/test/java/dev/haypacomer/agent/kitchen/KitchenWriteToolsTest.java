package dev.haypacomer.agent.kitchen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.application.cooking.EvaluateRecipe;
import dev.haypacomer.application.fridge.ListFridges;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.ExpiryDesk;
import dev.haypacomer.application.inventory.FoodAccessGuard;
import dev.haypacomer.application.inventory.MarkFoodOpened;
import dev.haypacomer.application.inventory.UndoLastChange;
import dev.haypacomer.application.market.AddMissingToMarketList;
import dev.haypacomer.application.market.SetMarketBudget;
import dev.haypacomer.application.market.UpdateMarketList;
import dev.haypacomer.application.planning.AddPlanDeltaToMarketList;
import dev.haypacomer.application.planning.GenerateWeeklyPlan;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.application.port.MarketBudgetRepository;
import dev.haypacomer.application.session.AdvanceCookingSession;
import dev.haypacomer.application.session.ResumeCookingSession;
import dev.haypacomer.application.session.StartCookingSession;
import dev.haypacomer.application.support.InMemoryCookingSessionRepository;
import dev.haypacomer.application.support.InMemoryCookingStores;
import dev.haypacomer.application.support.InMemoryKitchenDevices;
import dev.haypacomer.application.support.InMemorySnapshotStore;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.market.MarketSource;
import dev.haypacomer.domain.planning.RescueFirstStrategy;
import dev.haypacomer.domain.quantity.Grams;
import dev.haypacomer.domain.recipe.Recipe;
import dev.haypacomer.domain.recipe.RecipeId;
import dev.haypacomer.domain.recipe.RecipeRequirement;
import dev.haypacomer.domain.recipe.RecipeSource;
import dev.haypacomer.domain.recipe.RecipeStep;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KitchenWriteToolsTest {

  private final KitchenFixture kitchen = new KitchenFixture();
  private final InMemorySnapshotStore snapshots = new InMemorySnapshotStore();
  private final InMemoryCookingStores cooking = new InMemoryCookingStores();
  private final InMemoryCookingSessionRepository sessionStore =
      new InMemoryCookingSessionRepository();
  private final Map<HouseholdId, BigDecimal> budgets = new HashMap<>();
  private final ExecuteInventoryCommand commands =
      new ExecuteInventoryCommand(
          kitchen.households,
          kitchen.fridges,
          kitchen.stores.ownerships,
          kitchen.stores.movements,
          kitchen.stores.catalog,
          new FoodAccessGuard(),
          kitchen.stores.audit,
          kitchen.stores.unitOfWork,
          snapshots,
          kitchen.clock);
  private final ListRecipes recipes = new ListRecipes(kitchen.households, kitchen.planning.recipes);
  private final ResumeCookingSession sessions =
      new ResumeCookingSession(kitchen.households, sessionStore);
  private final UpdateMarketList updateMarket =
      new UpdateMarketList(kitchen.households, kitchen.marketLists, kitchen.clock);

  private ToolInvocation call(Map<String, String> arguments) {
    return new ToolInvocation(kitchen.home.id(), kitchen.owner, arguments);
  }

  private Grams grams(FoodItem item) {
    return kitchen.fridge.findItem(item.id()).map(FoodItem::quantity).orElse(Grams.ZERO);
  }

  @Test
  void stockFoodGoesToAShelfOrTheNamedPlace() {
    kitchen.stores.catalog.save(KitchenFixture.food("Potato"));
    StockFoodTool tool =
        new StockFoodTool(
            commands, kitchen.stores.catalog, new ListFridges(kitchen.households, kitchen.fridges));

    ToolInvocation potato = call(Map.of("food", "potatoes", "grams", "1000"));
    assertEquals(Optional.empty(), tool.problem(potato));
    assertEquals("Put 1000 g of Potato in Kitchen, Shelves Top", tool.describe(potato));
    assertEquals("Stocked 1000 g of Potato in Kitchen, Shelves Top", tool.invoke(potato).content());

    ToolInvocation door = call(Map.of("food", "potato", "grams", "300", "place", "puerta"));
    assertTrue(tool.invoke(door).content().endsWith("in Kitchen, Door Rack"));
    assertEquals(2, kitchen.fridge.itemCount());
    assertTrue(
        tool.problem(call(Map.of("food", "potato", "grams", "1", "place", "garage")))
            .orElseThrow()
            .startsWith("No place called garage; places: Kitchen, Shelves Top"));
    assertEquals(
        Optional.of("Unknown food caviar; use a catalog name"),
        tool.problem(call(Map.of("food", "caviar", "grams", "50"))));
  }

  @Test
  void useFoodDiscountsThePieceThatExpiresFirst() {
    FoodItem later = kitchen.put("Rice", 900, KitchenFixture.TODAY.plusDays(9));
    FoodItem sooner = kitchen.put("Rice", 300, KitchenFixture.TODAY.plusDays(2));
    UseFoodTool tool =
        new UseFoodTool(commands, kitchen.stores.catalog, kitchen.inventory, kitchen.today);

    ToolInvocation use = call(Map.of("food", "rice", "grams", "200"));
    assertEquals(Optional.empty(), tool.problem(use));
    assertEquals("Use 200 g of Rice (expires 2026-10-11)", tool.describe(use));
    assertEquals("Used 200 g of Rice; 100 g left", tool.invoke(use).content());
    assertEquals(Grams.of(100), grams(sooner));
    assertEquals(Grams.of(900), grams(later));

    assertEquals(
        Optional.of("Only 900 g of Rice in one piece; use at most that"),
        tool.problem(call(Map.of("food", "rice", "grams", "5000"))));
    assertEquals(
        Optional.of("No Milk in stock"),
        new UseFoodTool(commands, kitchen.stores.catalog, kitchen.inventory, kitchen.today)
            .problem(milkWithoutStock()));
  }

  private ToolInvocation milkWithoutStock() {
    kitchen.stores.catalog.save(KitchenFixture.food("Milk"));
    return call(Map.of("food", "milk", "grams", "100"));
  }

  @Test
  void discardOpenAndUndoWorkOnTheFirstPiece() {
    FoodItem tomato = kitchen.put("Tomato", 400, KitchenFixture.TODAY.plusDays(1));
    DiscardFoodTool discard =
        new DiscardFoodTool(commands, kitchen.stores.catalog, kitchen.inventory, kitchen.today);
    MarkOpenedTool open =
        new MarkOpenedTool(
            new MarkFoodOpened(
                kitchen.households,
                kitchen.fridges,
                kitchen.stores.ownerships,
                kitchen.stores.movements,
                kitchen.stores.catalog,
                new FoodAccessGuard(),
                ExpiryDesk.LENIENT,
                kitchen.stores.audit,
                kitchen.stores.unitOfWork,
                snapshots,
                kitchen.clock),
            kitchen.stores.catalog,
            kitchen.inventory,
            kitchen.today);
    UndoLastChangeTool undo =
        new UndoLastChangeTool(
            new UndoLastChange(
                kitchen.households,
                kitchen.fridges,
                kitchen.stores.ownerships,
                kitchen.stores.movements,
                kitchen.stores.catalog,
                snapshots,
                kitchen.stores.audit,
                kitchen.stores.unitOfWork,
                kitchen.clock));
    ToolInvocation tomatoes = call(Map.of("food", "tomato"));

    assertEquals("Mark Tomato as opened", open.describe(tomatoes));
    assertTrue(open.invoke(tomatoes).content().startsWith("Marked Tomato as opened"));
    assertTrue(kitchen.fridge.findItem(tomato.id()).orElseThrow().isOpened());
    assertEquals("Throw away 400 g of Tomato", discard.describe(tomatoes));
    assertEquals("Threw away 400 g of Tomato", discard.invoke(tomatoes).content());
    assertEquals(Optional.of("No Tomato in stock"), discard.problem(tomatoes));
    assertEquals(Optional.of("No Tomato in stock"), open.problem(tomatoes));

    assertTrue(undo.invoke(call(Map.of())).content().startsWith("Undid the last change: discard"));
    assertEquals(Optional.empty(), discard.problem(tomatoes));
  }

  @Test
  void marketToolsCheckRemoveClearAndBudget() {
    kitchen.stores.catalog.save(KitchenFixture.food("Rice"));
    kitchen.stores.catalog.save(KitchenFixture.food("Tomato"));
    kitchen
        .addToMarket()
        .add(kitchen.owner, kitchen.home.id(), "Rice", Grams.of(500), MarketSource.MANUAL);
    kitchen
        .addToMarket()
        .add(kitchen.owner, kitchen.home.id(), "Tomato", Grams.of(300), MarketSource.MANUAL);
    UpdateMarketTool update =
        new UpdateMarketTool(updateMarket, kitchen.viewMarket(), kitchen.stores.catalog);
    ClearCheckedMarketTool clear = new ClearCheckedMarketTool(updateMarket, kitchen.viewMarket());

    ToolInvocation check = call(Map.of("food", "rice", "action", "check"));
    assertEquals(Optional.of("The cart is already empty"), clear.problem(call(Map.of())));
    assertEquals("Mark 500 g of Rice as bought", update.describe(check));
    assertEquals("Marked 500 g of Rice as bought", update.invoke(check).content());
    assertEquals(Optional.of("Rice is not pending on the market list"), update.problem(check));
    assertEquals(
        "Put 500 g of Rice back on the market list",
        update.invoke(call(Map.of("food", "rice", "action", "UNCHECK"))).content());
    assertEquals(
        "Removed 300 g of Tomato from the market list",
        update.invoke(call(Map.of("food", "tomato", "action", "REMOVE"))).content());
    assertEquals(
        Optional.of("action must be CHECK, UNCHECK, or REMOVE"),
        update.problem(call(Map.of("food", "rice", "action", "BUY"))));

    update.invoke(check);
    assertEquals("Clear 1 bought foods from the market cart", clear.describe(call(Map.of())));
    assertEquals("Cleared 1 bought foods from the cart", clear.invoke(call(Map.of())).content());

    SetMarketBudgetTool budget =
        new SetMarketBudgetTool(
            new SetMarketBudget(
                kitchen.households,
                new MarketBudgetRepository() {
                  @Override
                  public Optional<BigDecimal> monthly(HouseholdId household) {
                    return Optional.ofNullable(budgets.get(household));
                  }

                  @Override
                  public void save(HouseholdId household, BigDecimal monthly) {
                    budgets.put(household, monthly);
                  }
                }));
    ToolInvocation monthly = call(Map.of("amount", "300000"));
    assertEquals("Set the monthly market budget to 300000", budget.describe(monthly));
    assertEquals("Monthly market budget set to 300000", budget.invoke(monthly).content());
    assertEquals(0, new BigDecimal("300000").compareTo(budgets.get(kitchen.home.id())));
  }

  @Test
  void addToMarketReportsWhatItAddedAndTheTotal() {
    kitchen.stores.catalog.save(KitchenFixture.food("Rice"));
    AddToMarketTool add = new AddToMarketTool(kitchen.addToMarket(), kitchen.stores.catalog);
    add.invoke(call(Map.of("food", "rice", "grams", "1325")));

    assertEquals(
        "Added 500 g of Rice to the market list; 1825 g pending in total",
        add.invoke(call(Map.of("food", "rice", "grams", "500"))).content());
  }

  @Test
  void recipesAreListedCookedAndPlanned() {
    Recipe rice = riceBowl();
    kitchen.planning.recipes.save(kitchen.home.id(), rice);
    kitchen.put("Rice", 100, null);
    InMemoryKitchenDevices devices = new InMemoryKitchenDevices();
    StartCookingTool start =
        new StartCookingTool(
            new StartCookingSession(
                kitchen.households, sessionStore, devices.devices, event -> {}, kitchen.clock),
            sessions,
            recipes);
    CookingStepTool step =
        new CookingStepTool(
            new AdvanceCookingSession(kitchen.households, sessionStore, event -> {}, kitchen.clock),
            sessions);
    ViewCookingTool view = new ViewCookingTool(sessions);

    assertEquals(
        "Rice bowl: 2 servings, 20 min",
        new ListRecipesTool(recipes).invoke(call(Map.of())).content());
    assertEquals("No cooking session is active", view.invoke(call(Map.of())).content());
    assertEquals(
        Optional.of("No cooking session is active; start a recipe first"),
        step.problem(call(Map.of("action", "NEXT"))));
    assertEquals(
        Optional.of("No saved recipe called pizza; saved recipes: Rice bowl"),
        start.problem(call(Map.of("recipe", "pizza"))));

    ToolInvocation cook = call(Map.of("recipe", "rice BOWL", "servings", "4"));
    assertEquals(Optional.empty(), start.problem(cook));
    assertEquals("Start cooking Rice bowl for 4", start.describe(cook));
    assertEquals(
        "Started Rice bowl for 4, PREPARING, 2 steps; next starts step 1",
        start.invoke(cook).content());
    assertTrue(start.problem(cook).orElseThrow().startsWith("A cooking session is already active"));

    ToolInvocation next = call(Map.of("action", "next"));
    assertEquals("Go to the next step of Rice bowl", step.describe(next));
    assertEquals(
        "Rice bowl for 4, COOKING, step 1 of 2: Rinse the rice", step.invoke(next).content());
    assertEquals(
        "Rice bowl for 4, PAUSED, step 1 of 2: Rinse the rice",
        step.invoke(call(Map.of("action", "PAUSE"))).content());
    assertEquals(
        "Rice bowl for 4, PAUSED, step 1 of 2: Rinse the rice",
        view.invoke(call(Map.of())).content());
    step.invoke(call(Map.of("action", "ABANDON")));
    assertEquals("No cooking session is active", view.invoke(call(Map.of())).content());

    MissingToMarketTool missing =
        new MissingToMarketTool(
            new AddMissingToMarketList(
                kitchen.households,
                new EvaluateRecipe(
                    kitchen.households,
                    kitchen.inventory,
                    cooking.profiles,
                    cooking.rules,
                    kitchen.clock),
                kitchen.marketLists,
                kitchen.clock),
            recipes);
    ToolInvocation forRice = call(Map.of("recipe", "rice bowl"));
    assertEquals(
        "Add what Rice bowl for 2 is missing to the market list", missing.describe(forRice));
    assertEquals(
        "Missing for Rice bowl: Rice 200 g (added 200 g, 200 g pending)",
        missing.invoke(forRice).content());

    PlanToMarketTool planToMarket =
        new PlanToMarketTool(
            new AddPlanDeltaToMarketList(
                kitchen.households,
                kitchen.planning.plans,
                kitchen.inventory,
                kitchen.marketLists,
                kitchen.clock),
            kitchen.currentPlan());
    assertEquals(
        Optional.of("No plan for this week; make the weekly plan first"),
        planToMarket.problem(call(Map.of())));

    GenerateWeekPlanTool plan =
        new GenerateWeekPlanTool(
            new GenerateWeeklyPlan(
                kitchen.households,
                kitchen.inventory,
                cooking.profiles,
                kitchen.planning.recipes,
                kitchen.planning.plans,
                new RescueFirstStrategy(),
                kitchen.clock),
            recipes);
    assertEquals("Make this week's meal plan for 2", plan.describe(call(Map.of())));
    assertTrue(plan.invoke(call(Map.of())).content().startsWith("Planned "));
    assertEquals(Optional.empty(), planToMarket.problem(call(Map.of())));
    assertTrue(planToMarket.invoke(call(Map.of())).content().contains("Rice"));
  }

  private static Recipe riceBowl() {
    return new Recipe(
        RecipeId.newId(),
        "Rice bowl",
        2,
        20,
        RecipeSource.MANUAL,
        List.of(RecipeRequirement.of(KitchenFixture.food("Rice"), Grams.of(300))),
        List.of(RecipeStep.of(1, "Rinse the rice"), RecipeStep.of(2, "Boil it")));
  }
}
