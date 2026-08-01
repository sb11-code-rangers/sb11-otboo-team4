package com.sprint.mission.otboo.security.details;

import java.util.UUID;

public record UserPrincipal(
    UUID userId,
    String role
) {

}
