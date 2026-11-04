package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.port.UserTokenStore;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.identity.UserToken;
import dev.haypacomer.domain.identity.UserTokenType;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresUserTokenStore implements UserTokenStore {

  private final JdbcClient jdbc;

  public PostgresUserTokenStore(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  @Override
  public void save(UserToken token) {
    jdbc.sql(
            """
            INSERT INTO user_tokens (id, user_id, type, token_hash, expires_at, used_at)
            VALUES (:id, :user, :type, :hash, :expiresAt, :usedAt)
            ON CONFLICT (id) DO UPDATE SET used_at = EXCLUDED.used_at
            """)
        .param("id", token.id())
        .param("user", token.user().value())
        .param("type", token.type().name())
        .param("hash", HexFormat.of().parseHex(token.tokenHash()))
        .param("expiresAt", Timestamps.toDatabase(token.expiresAt()))
        .param(
            "usedAt",
            token.usedAt() == null ? null : Timestamps.toDatabase(token.usedAt()),
            Types.TIMESTAMP_WITH_TIMEZONE)
        .update();
  }

  @Override
  public int countUsable(UserId user, UserTokenType type, Instant now) {
    return jdbc.sql(
            "SELECT count(*) FROM user_tokens WHERE user_id = :user AND type = :type"
                + " AND used_at IS NULL AND expires_at > :now")
        .param("user", user.value())
        .param("type", type.name())
        .param("now", Timestamps.toDatabase(now))
        .query(Integer.class)
        .single();
  }

  @Override
  public void useAll(UserId user, UserTokenType type, Instant at) {
    jdbc.sql(
            "UPDATE user_tokens SET used_at = :at WHERE user_id = :user AND type = :type"
                + " AND used_at IS NULL")
        .param("user", user.value())
        .param("type", type.name())
        .param("at", Timestamps.toDatabase(at))
        .update();
  }

  @Override
  public Optional<UserToken> findByHash(String tokenHash) {
    return jdbc.sql(
            "SELECT id, user_id, type, token_hash, expires_at, used_at FROM user_tokens"
                + " WHERE token_hash = :hash")
        .param("hash", HexFormat.of().parseHex(tokenHash))
        .query(
            (row, rowNumber) -> {
              OffsetDateTime usedAt = row.getObject("used_at", OffsetDateTime.class);
              return new UserToken(
                  row.getObject("id", UUID.class),
                  new UserId(row.getObject("user_id", UUID.class)),
                  UserTokenType.valueOf(row.getString("type")),
                  HexFormat.of().formatHex(row.getBytes("token_hash")),
                  Timestamps.read(row, "expires_at"),
                  usedAt == null ? null : usedAt.toInstant());
            })
        .optional();
  }
}
