package com.sprint.mission.otboo.domain.authuser.auth.mapper;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.RefreshDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

  private final UserMapper userMapper;

  public SignInDto signInDtoFrom(UserDto userDto, String accessToken, String refreshToken) {
    return new SignInDto(
        new JwtDto(userDto, accessToken),
        refreshToken
    );
  }

  // OAuth2SignInService는 UserDto가 아니라 User 엔티티를 그대로 들고 있어서 오버로드가 필요하다.
  public SignInDto signInDtoFrom(User user, String accessToken, String refreshToken) {
    return new SignInDto(
        new JwtDto(userMapper.userDtoFrom(user), accessToken),
        refreshToken
    );
  }

  public RefreshDto refreshDtoFrom(User user, String accessToken, String refreshToken) {
    return new RefreshDto(
        new JwtDto(userMapper.userDtoFrom(user), accessToken),
        refreshToken
    );
  }
}
