package com.sprint.mission.otboo.domain.authuser.user.dto.request;

public record UserCreateRequest(
    String email,
    String password,
    String name
) {

}
