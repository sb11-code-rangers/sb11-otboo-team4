package com.sprint.mission.otboo.global.security.details;

import java.util.UUID;

public record UserPrincipal(
    UUID userId,
    String role
) {

}
