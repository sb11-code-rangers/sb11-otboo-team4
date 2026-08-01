package com.sprint.mission.otboo.security.token.dto;

import java.util.UUID;

public record RefreshTokenClaims(
    UUID userId,
    UUID sessionId,
    UUID jti
) {

}
