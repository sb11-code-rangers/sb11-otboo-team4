package com.sprint.mission.otboo.domain.authuser.auth.dto.response;


import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;

public record RefreshDto(
    UserDto userDto,
    String accessToken,
    String refreshToken
) {

}
