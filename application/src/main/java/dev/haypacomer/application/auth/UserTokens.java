package dev.haypacomer.application.auth;

import dev.haypacomer.application.port.UserTokenStore;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.identity.UserToken;
import dev.haypacomer.domain.identity.UserTokenType;
import java.time.Instant;
import java.util.Objects;

final class UserTokens {

  private static final int MAX_OPEN_LINKS = 3;

  private final UserTokenStore store;
  private final OpaqueTokens opaqueTokens;

  UserTokens(UserTokenStore store, OpaqueTokens opaqueTokens) {
    this.store = Objects.requireNonNull(store, "store");
    this.opaqueTokens = Objects.requireNonNull(opaqueTokens, "opaqueTokens");
  }

  // Links in flight are the limit: enough for a lost email, too few to flood someone's inbox.
  boolean mayIssue(UserId user, UserTokenType type, Instant now) {
    return store.countUsable(user, type, now) < MAX_OPEN_LINKS;
  }

  void closeAll(UserId user, UserTokenType type, Instant now) {
    store.useAll(user, type, now);
  }

  String issue(UserId user, UserTokenType type, Instant now) {
    String raw = opaqueTokens.generate();
    store.save(UserToken.issue(user, type, opaqueTokens.hash(raw), now));
    return raw;
  }

  UserToken consume(String raw, UserTokenType type, Instant now) {
    if (raw == null || raw.isBlank()) {
      throw new InvalidTokenException();
    }
    UserToken token =
        store
            .findByHash(opaqueTokens.hash(raw))
            .filter(found -> found.isUsable(type, now))
            .orElseThrow(InvalidTokenException::new);
    store.save(token.use(now));
    return token;
  }
}
