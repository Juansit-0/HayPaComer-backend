package dev.haypacomer.application.port;

import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.identity.UserToken;
import dev.haypacomer.domain.identity.UserTokenType;
import java.time.Instant;
import java.util.Optional;

public interface UserTokenStore {

  void save(UserToken token);

  Optional<UserToken> findByHash(String tokenHash);

  int countUsable(UserId user, UserTokenType type, Instant now);

  void useAll(UserId user, UserTokenType type, Instant at);
}
