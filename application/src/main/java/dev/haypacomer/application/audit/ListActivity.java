package dev.haypacomer.application.audit;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.port.AuditLog;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListActivity {

  public static final String PRIVATE = "private";

  private static final int MAX_LIMIT = 100;

  private final GetHousehold households;
  private final AuditLog audit;

  public ListActivity(HouseholdRepository households, AuditLog audit) {
    this.households = new GetHousehold(households);
    this.audit = Objects.requireNonNull(audit, "audit");
  }

  public List<AuditEntry> list(UserId actor, HouseholdId household, int limit) {
    households.get(actor, household);
    return audit.recent(household, Math.clamp(limit, 1, MAX_LIMIT)).stream()
        .map(entry -> visibleTo(actor, entry))
        .toList();
  }

  private AuditEntry visibleTo(UserId actor, AuditEntry entry) {
    if (entry.actor().equals(actor) || !entry.detail().containsKey(PRIVATE)) {
      return entry;
    }
    return new AuditEntry(
        entry.actor(),
        entry.household(),
        entry.action(),
        entry.entity(),
        entry.entityId(),
        Map.of(),
        entry.at());
  }
}
