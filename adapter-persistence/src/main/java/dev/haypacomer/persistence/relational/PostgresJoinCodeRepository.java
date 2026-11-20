package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.JoinCode;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresJoinCodeRepository implements JoinCodeRepository {

  private static final String SELECT =
      "SELECT household_id, code, role, created_by, created_at, expires_at FROM household_join_codes";

  private final JdbcClient jdbc;

  public PostgresJoinCodeRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  @Override
  public void save(JoinCode code) {
    jdbc.sql(
            """
            INSERT INTO household_join_codes (household_id, code, role, created_by, created_at, expires_at)
            VALUES (:household, :code, :role, :createdBy, :createdAt, :expiresAt)
            ON CONFLICT (household_id) DO UPDATE SET code = EXCLUDED.code, role = EXCLUDED.role,
                created_by = EXCLUDED.created_by, created_at = EXCLUDED.created_at,
                expires_at = EXCLUDED.expires_at
            """)
        .param("household", code.household().value())
        .param("code", code.code())
        .param("role", code.role().name())
        .param("createdBy", code.createdBy().value())
        .param("createdAt", Timestamps.toDatabase(code.createdAt()))
        .param("expiresAt", Timestamps.toDatabase(code.expiresAt()))
        .update();
  }

  @Override
  public Optional<JoinCode> findByCode(String code) {
    return jdbc.sql(SELECT + " WHERE code = :code").param("code", code).query(this::map).optional();
  }

  @Override
  public Optional<JoinCode> findByHousehold(HouseholdId household) {
    return jdbc.sql(SELECT + " WHERE household_id = :household")
        .param("household", household.value())
        .query(this::map)
        .optional();
  }

  @Override
  public void delete(HouseholdId household) {
    jdbc.sql("DELETE FROM household_join_codes WHERE household_id = :household")
        .param("household", household.value())
        .update();
  }

  private JoinCode map(ResultSet row, int rowNumber) throws SQLException {
    return new JoinCode(
        new HouseholdId(row.getObject("household_id", UUID.class)),
        row.getString("code"),
        Role.valueOf(row.getString("role")),
        new UserId(row.getObject("created_by", UUID.class)),
        Timestamps.read(row, "created_at"),
        Timestamps.read(row, "expires_at"));
  }
}
