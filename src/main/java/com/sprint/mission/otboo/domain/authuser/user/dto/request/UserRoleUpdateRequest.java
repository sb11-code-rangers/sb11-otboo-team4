package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;

public record UserRoleUpdateRequest(
    Role role
) {

}
