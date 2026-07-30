package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
    @NotNull
    Role role
) {

}
