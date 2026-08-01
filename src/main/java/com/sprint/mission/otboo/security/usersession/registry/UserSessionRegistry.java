package com.sprint.mission.otboo.security.usersession.registry;

import com.sprint.mission.otboo.security.usersession.dto.UserSession;
import com.sprint.mission.otboo.security.usersession.exception.RefreshTokenReusedException;
import com.sprint.mission.otboo.security.usersession.exception.UserSessionExpiredException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRegistry {

  default UserSession issue(UUID userId, Instant now, Duration refreshTokenTtl) {
    UserSession session = UserSession.issue(now);
    return save(userId, session, now.plus(refreshTokenTtl));
  }

  default UserSession rotate(UUID userId, UUID sessionId, UUID currentRefreshJti,
      Duration refreshTokenTtl) {
    UserSession current = verifyUserSession(userId, sessionId);

    if (!current.currentRefreshJti().equals(currentRefreshJti)) {
      revoke(userId);
      throw RefreshTokenReusedException.withNone();
    }

    UserSession rotated = current.rotate();
    return save(userId, rotated, rotated.issuedAt().plus(refreshTokenTtl));
  }

  default UserSession verifyUserSession(UUID userId, UUID sessionId) {
    UserSession current = find(userId).orElseThrow(UserSessionExpiredException::withNone);

    if (!current.sessionId().equals(sessionId)) {
      throw UserSessionExpiredException.withNone();
    }

    return current;
  }

  UserSession save(UUID userId, UserSession session, Instant expiresAt);

  void revoke(UUID userId);

  Optional<UserSession> find(UUID userId);
}
