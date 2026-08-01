package com.sprint.mission.otboo.global.security.token.dto;

import java.util.UUID;

public record AccessTokenClaims(
    UUID userId,
    String role,
    UUID sessionId
) {

}
