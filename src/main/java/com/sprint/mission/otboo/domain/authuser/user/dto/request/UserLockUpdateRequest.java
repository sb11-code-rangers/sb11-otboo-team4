package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserLockUpdateRequest(
    @NotNull
    Boolean locked
) {

}
