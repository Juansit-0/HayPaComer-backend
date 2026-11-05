package dev.haypacomer.agent.chat;

import dev.haypacomer.application.agent.ChatMessage;
import dev.haypacomer.application.agent.ConversationId;
import dev.haypacomer.application.agent.ConversationSummary;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.ConversationStore;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;

public final class ReadConversation {

  static final int LIMIT = 100;

  private final ConversationStore conversations;
  private final GetHousehold households;

  public ReadConversation(ConversationStore conversations, HouseholdRepository households) {
    this.conversations = conversations;
    this.households = new GetHousehold(households);
  }

  public List<ConversationSummary> recent(UserId user, HouseholdId household) {
    households.get(user, household);
    return conversations.recent(user, household, LIMIT);
  }

  public List<ChatMessage> read(UserId user, HouseholdId household, ConversationId id) {
    boolean mine = recent(user, household).stream().anyMatch(summary -> summary.id().equals(id));
    if (!mine) {
      throw new ConversationNotFoundException();
    }
    return conversations.messages(id, LIMIT);
  }
}
