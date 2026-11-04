package dev.haypacomer.application.port;

import dev.haypacomer.domain.identity.RefreshToken;
import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

  void save(RefreshToken token);

  boolean claim(UUID id, Instant at);

  Optional<RefreshToken> findByHash(String tokenHash);

  void revokeFamily(UUID family, Instant at);

  void revokeAll(UserId user, Instant at);
}
