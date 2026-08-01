package com.sprint.mission.otboo.security.usersession.dto;

import java.time.Instant;
import java.util.UUID;

public record UserSession(
    UUID sessionId,
    UUID currentRefreshJti,
    Instant issuedAt
) {

  public static UserSession issue(Instant now) {
    return new UserSession(UUID.randomUUID(), UUID.randomUUID(), now);
  }

  public UserSession rotate() {
    return new UserSession(sessionId, UUID.randomUUID(), issuedAt);
  }
}
