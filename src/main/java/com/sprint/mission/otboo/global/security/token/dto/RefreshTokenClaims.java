package com.sprint.mission.otboo.global.security.token.dto;

import java.util.UUID;

public record RefreshTokenClaims(
    UUID userId,
    UUID sessionId,
    UUID jti
) {

}
