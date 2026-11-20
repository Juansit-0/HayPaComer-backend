package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.port.AccessRequestRepository;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.inventory.AccessRequest;
import dev.haypacomer.domain.member.MemberId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresAccessRequestRepository implements AccessRequestRepository {

  private static final String SELECT =
      "SELECT id, household_id, food_item_id, requester_member_id, owner_member_id, status,"
          + " created_at, decided_at FROM food_access_requests";

  private final JdbcClient jdbc;

  public PostgresAccessRequestRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  @Override
  public void save(AccessRequest request) {
    jdbc.sql(
            """
            INSERT INTO food_access_requests (id, household_id, food_item_id, requester_member_id,
                                              owner_member_id, status, created_at, decided_at)
            VALUES (:id, :household, :item, :requester, :owner, :status, :createdAt, :decidedAt)
            ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, decided_at = EXCLUDED.decided_at
            """)
        .param("id", request.id())
        .param("household", request.household().value())
        .param("item", request.item().value())
        .param("requester", request.requester().value())
        .param("owner", request.owner().value())
        .param("status", request.status().name())
        .param("createdAt", Timestamps.toDatabase(request.createdAt()))
        .param(
            "decidedAt",
            request.decidedAt() == null ? null : Timestamps.toDatabase(request.decidedAt()),
            Types.TIMESTAMP_WITH_TIMEZONE)
        .update();
  }

  @Override
  public Optional<AccessRequest> find(HouseholdId household, UUID id) {
    return jdbc.sql(SELECT + " WHERE id = :id AND household_id = :household")
        .param("id", id)
        .param("household", household.value())
        .query(this::map)
        .optional();
  }

  @Override
  public Optional<AccessRequest> findPending(FoodItemId item, MemberId requester) {
    return jdbc.sql(
            SELECT
                + " WHERE food_item_id = :item AND requester_member_id = :requester"
                + " AND status = 'PENDING'")
        .param("item", item.value())
        .param("requester", requester.value())
        .query(this::map)
        .optional();
  }

  @Override
  public List<AccessRequest> involving(HouseholdId household, MemberId member) {
    return jdbc.sql(
            SELECT
                + " WHERE household_id = :household AND ("
                + " (owner_member_id = :member AND status = 'PENDING')"
                + " OR (requester_member_id = :member"
                + "     AND (status = 'PENDING' OR decided_at > now() - interval '3 days')))"
                + " ORDER BY created_at DESC LIMIT 50")
        .param("household", household.value())
        .param("member", member.value())
        .query(this::map)
        .list();
  }

  private AccessRequest map(ResultSet row, int rowNumber) throws SQLException {
    OffsetDateTime decidedAt = row.getObject("decided_at", OffsetDateTime.class);
    return new AccessRequest(
        row.getObject("id", UUID.class),
        new HouseholdId(row.getObject("household_id", UUID.class)),
        new FoodItemId(row.getObject("food_item_id", UUID.class)),
        new MemberId(row.getObject("requester_member_id", UUID.class)),
        new MemberId(row.getObject("owner_member_id", UUID.class)),
        AccessRequest.Status.valueOf(row.getString("status")),
        Timestamps.read(row, "created_at"),
        decidedAt == null ? null : decidedAt.toInstant());
  }
}
