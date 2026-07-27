package com.sprint.mission.otboo.domain.authuser.auth.mapper;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.mapper.UserMapper;

public final class AuthMapper {

    private AuthMapper() {}

    public static SignInDto signInDtoFrom(User user, String accessToken, String refreshToken) {
        return new SignInDto(
                jwtDtoFrom(user, accessToken),
                refreshToken
        );
    }

    public static JwtDto jwtDtoFrom(User user, String accessToken) {
        return new JwtDto(
                UserMapper.userDtoFromUser(user),
                accessToken
        );
    }
}
