package dev.haypacomer.web.agent;

import dev.haypacomer.agent.chat.ChefChat;
import dev.haypacomer.agent.chat.ReadConversation;
import dev.haypacomer.agent.confirm.ApproveConfirmation;
import dev.haypacomer.agent.kitchen.AddToMarketTool;
import dev.haypacomer.agent.kitchen.ClearCheckedMarketTool;
import dev.haypacomer.agent.kitchen.CookingStepTool;
import dev.haypacomer.agent.kitchen.DiscardFoodTool;
import dev.haypacomer.agent.kitchen.EstimateExpiryTool;
import dev.haypacomer.agent.kitchen.GenerateWeekPlanTool;
import dev.haypacomer.agent.kitchen.InvestigateColdTool;
import dev.haypacomer.agent.kitchen.KitchenToday;
import dev.haypacomer.agent.kitchen.ListRecipesTool;
import dev.haypacomer.agent.kitchen.MarkOpenedTool;
import dev.haypacomer.agent.kitchen.MissingToMarketTool;
import dev.haypacomer.agent.kitchen.PlanToMarketTool;
import dev.haypacomer.agent.kitchen.QueryInventoryTool;
import dev.haypacomer.agent.kitchen.ReviewBudgetTool;
import dev.haypacomer.agent.kitchen.SetMarketBudgetTool;
import dev.haypacomer.agent.kitchen.StartCookingTool;
import dev.haypacomer.agent.kitchen.StockFoodTool;
import dev.haypacomer.agent.kitchen.SuggestDishesTool;
import dev.haypacomer.agent.kitchen.UndoLastChangeTool;
import dev.haypacomer.agent.kitchen.UpdateMarketTool;
import dev.haypacomer.agent.kitchen.UseFoodTool;
import dev.haypacomer.agent.kitchen.ViewColdChainTool;
import dev.haypacomer.agent.kitchen.ViewCookingTool;
import dev.haypacomer.agent.kitchen.ViewExpiriesTool;
import dev.haypacomer.agent.kitchen.ViewMarketListTool;
import dev.haypacomer.agent.kitchen.ViewWeeklyPlanTool;
import dev.haypacomer.agent.kitchen.WastePatternsTool;
import dev.haypacomer.agent.memory.RecallMemoryTool;
import dev.haypacomer.agent.memory.RememberTool;
import dev.haypacomer.agent.supervisor.KeywordRouter;
import dev.haypacomer.agent.supervisor.PlannerFactory;
import dev.haypacomer.agent.supervisor.Supervisor;
import dev.haypacomer.agent.tools.GuardrailChain;
import dev.haypacomer.agent.tools.PermissionGuardrail;
import dev.haypacomer.agent.tools.SchemaGuardrail;
import dev.haypacomer.agent.tools.ToolRegistry;
import dev.haypacomer.application.agent.ConfirmationDesk;
import dev.haypacomer.application.agent.ListPendingConfirmations;
import dev.haypacomer.application.agent.RejectConfirmation;
import dev.haypacomer.application.agent.RememberForHousehold;
import dev.haypacomer.application.agent.ViewAgentRun;
import dev.haypacomer.application.agent.ViewHouseholdMemory;
import dev.haypacomer.application.ai.SuggestDishes;
import dev.haypacomer.application.analytics.FindWastePatterns;
import dev.haypacomer.application.coldchain.InvestigateColdIncidents;
import dev.haypacomer.application.coldchain.ListColdChains;
import dev.haypacomer.application.fridge.ListFridges;
import dev.haypacomer.application.inventory.EstimateExpiry;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.MarkFoodOpened;
import dev.haypacomer.application.inventory.UndoLastChange;
import dev.haypacomer.application.inventory.ViewInventory;
import dev.haypacomer.application.market.AddMissingToMarketList;
import dev.haypacomer.application.market.AddToMarketList;
import dev.haypacomer.application.market.SetMarketBudget;
import dev.haypacomer.application.market.UpdateMarketList;
import dev.haypacomer.application.market.ViewMarketBudget;
import dev.haypacomer.application.market.ViewMarketList;
import dev.haypacomer.application.planning.AddPlanDeltaToMarketList;
import dev.haypacomer.application.planning.GenerateWeeklyPlan;
import dev.haypacomer.application.planning.ListRecipes;
import dev.haypacomer.application.planning.ViewCurrentPlan;
import dev.haypacomer.application.port.AgentRunStore;
import dev.haypacomer.application.port.AiAuditLog;
import dev.haypacomer.application.port.ConfirmationStore;
import dev.haypacomer.application.port.ConversationStore;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.session.AdvanceCookingSession;
import dev.haypacomer.application.session.ResumeCookingSession;
import dev.haypacomer.application.session.StartCookingSession;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

  @Bean
  ToolRegistry agentTools(
      ViewHouseholdMemory viewMemory,
      RememberForHousehold remember,
      ViewInventory inventory,
      ViewMarketList market,
      AddToMarketList addToMarket,
      FoodCatalogRepository catalog,
      ListColdChains coldChains,
      ViewCurrentPlan plans,
      ViewMarketBudget budget,
      InvestigateColdIncidents coldIncidents,
      FindWastePatterns wastePatterns,
      EstimateExpiry estimateExpiry,
      HouseholdRepository households,
      Clock clock,
      KitchenUseCases kitchen) {
    KitchenToday today = new KitchenToday(households, clock);
    return new ToolRegistry(
        List.of(
            new RecallMemoryTool(viewMemory),
            new RememberTool(remember),
            new QueryInventoryTool(inventory, today),
            new ViewExpiriesTool(inventory, today),
            new EstimateExpiryTool(estimateExpiry),
            new ViewMarketListTool(market),
            new AddToMarketTool(addToMarket, catalog),
            new ViewColdChainTool(coldChains),
            new ViewWeeklyPlanTool(plans),
            new ReviewBudgetTool(budget),
            new InvestigateColdTool(coldIncidents),
            new WastePatternsTool(wastePatterns),
            new StockFoodTool(kitchen.commands(), catalog, kitchen.fridges()),
            new UseFoodTool(kitchen.commands(), catalog, inventory, today),
            new DiscardFoodTool(kitchen.commands(), catalog, inventory, today),
            new MarkOpenedTool(kitchen.open(), catalog, inventory, today),
            new UndoLastChangeTool(kitchen.undo()),
            new UpdateMarketTool(kitchen.updateMarket(), market, catalog),
            new ClearCheckedMarketTool(kitchen.updateMarket(), market),
            new SetMarketBudgetTool(kitchen.setBudget()),
            new MissingToMarketTool(kitchen.missing(), kitchen.recipes()),
            new PlanToMarketTool(kitchen.planDelta(), plans),
            new ListRecipesTool(kitchen.recipes()),
            new SuggestDishesTool(kitchen.suggest(), kitchen.recipes()),
            new StartCookingTool(kitchen.start(), kitchen.sessions(), kitchen.recipes()),
            new CookingStepTool(kitchen.advance(), kitchen.sessions()),
            new ViewCookingTool(kitchen.sessions()),
            new GenerateWeekPlanTool(kitchen.plan(), kitchen.recipes())));
  }

  @Bean
  KitchenUseCases agentKitchenUseCases(
      ExecuteInventoryCommand commands,
      ListFridges fridges,
      MarkFoodOpened open,
      UndoLastChange undo,
      UpdateMarketList updateMarket,
      SetMarketBudget setBudget,
      AddMissingToMarketList missing,
      AddPlanDeltaToMarketList planDelta,
      ListRecipes recipes,
      SuggestDishes suggest,
      StartCookingSession start,
      AdvanceCookingSession advance,
      ResumeCookingSession sessions,
      GenerateWeeklyPlan plan) {
    return new KitchenUseCases(
        commands,
        fridges,
        open,
        undo,
        updateMarket,
        setBudget,
        missing,
        planDelta,
        recipes,
        suggest,
        start,
        advance,
        sessions,
        plan);
  }

  record KitchenUseCases(
      ExecuteInventoryCommand commands,
      ListFridges fridges,
      MarkFoodOpened open,
      UndoLastChange undo,
      UpdateMarketList updateMarket,
      SetMarketBudget setBudget,
      AddMissingToMarketList missing,
      AddPlanDeltaToMarketList planDelta,
      ListRecipes recipes,
      SuggestDishes suggest,
      StartCookingSession start,
      AdvanceCookingSession advance,
      ResumeCookingSession sessions,
      GenerateWeeklyPlan plan) {}

  @Bean
  GuardrailChain agentGuardrails(HouseholdRepository households) {
    return new GuardrailChain(List.of(new SchemaGuardrail(), new PermissionGuardrail(households)));
  }

  @Bean
  Supervisor supervisor(
      HouseholdRepository households,
      ToolRegistry tools,
      GuardrailChain guardrails,
      AgentRunStore runs,
      ConfirmationStore confirmations,
      AiAuditLog audit,
      PlannerFactory planners,
      Clock clock) {
    return new Supervisor(
        households,
        tools,
        guardrails,
        new KeywordRouter(),
        planners,
        runs,
        confirmations,
        audit,
        clock);
  }

  @Bean
  ChefChat chefChat(Supervisor supervisor, ConversationStore conversations, Clock clock) {
    return new ChefChat(supervisor, conversations, clock);
  }

  @Bean
  ReadConversation readConversation(
      ConversationStore conversations, HouseholdRepository households) {
    return new ReadConversation(conversations, households);
  }

  @Bean
  ConfirmationDesk confirmationDesk(
      ConfirmationStore confirmations, AgentRunStore runs, Clock clock) {
    return new ConfirmationDesk(confirmations, runs, clock);
  }

  @Bean
  ApproveConfirmation approveConfirmation(
      ConfirmationDesk desk, ToolRegistry tools, GuardrailChain guardrails) {
    return new ApproveConfirmation(desk, tools, guardrails);
  }

  @Bean
  RejectConfirmation rejectConfirmation(ConfirmationDesk desk) {
    return new RejectConfirmation(desk);
  }

  @Bean
  ListPendingConfirmations listPendingConfirmations(ConfirmationStore confirmations, Clock clock) {
    return new ListPendingConfirmations(confirmations, clock);
  }

  @Bean
  ViewAgentRun viewAgentRun(HouseholdRepository households, AgentRunStore runs) {
    return new ViewAgentRun(households, runs);
  }
}
