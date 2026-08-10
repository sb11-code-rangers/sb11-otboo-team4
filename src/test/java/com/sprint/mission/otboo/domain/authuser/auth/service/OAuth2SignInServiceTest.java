package com.sprint.mission.otboo.domain.authuser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.mapper.AuthMapper;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.SocialAccount;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.SocialAccountRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.usersession.dto.UserSession;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2SignInService")
class OAuth2SignInServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @InjectMocks
  private OAuth2SignInService oAuth2SignInService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProfileRepository profileRepository;

  @Mock
  private SocialAccountRepository socialAccountRepository;

  @Mock
  private UserSessionRegistry userSessionRegistry;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private AuthMapper authMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private Clock clock;

  // Hibernate는 persist 시점에 id를 채우는데, 이 테스트는 실제 저장을 하지 않으니
  // findById/findByProviderAndProviderId로 되돌려줄 User가 어떤 id를 갖는지는 직접 넣어줘야 한다.
  private User userWithId(UUID id, String name, String email) {
    User user = User.create(name, email, "encoded-password");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  @Nested
  @DisplayName("기존 연동된 소셜 계정으로 로그인")
  class LoginWithLinkedAccount {

    @Test
    @DisplayName("이미 연동된 소셜 계정이면 기존 사용자로 로그인해 토큰을 발급한다")
    void 이미_연동된_소셜_계정이면_기존_사용자로_로그인해_토큰을_발급한다() {
      // given
      User existingUser = userWithId(UUID.randomUUID(), "홍길동", "hong@test.com");
      SocialAccount linkedAccount = SocialAccount.link(existingUser, OAuth2Provider.GOOGLE,
          "google-sub-1", "hong@gmail.com");
      given(socialAccountRepository.findByProviderAndProviderId(OAuth2Provider.GOOGLE, "google-sub-1"))
          .willReturn(Optional.of(linkedAccount));

      given(clock.instant()).willReturn(NOW);
      UUID sessionId = UUID.randomUUID();
      UUID jti = UUID.randomUUID();
      UserSession issued = new UserSession(existingUser.getId(), sessionId, jti, NOW);
      given(userSessionRegistry.issue(existingUser.getId(), NOW)).willReturn(issued);
      given(tokenProvider.createAccessToken(existingUser.getId(), sessionId, "USER", NOW))
          .willReturn("access-token");
      given(tokenProvider.createRefreshToken(existingUser.getId(), sessionId, jti, NOW))
          .willReturn("refresh-token");

      UserDto userDto = new UserDto(existingUser.getId(), null, "hong@test.com", "홍길동", Role.USER,
          false);
      SignInDto expected = new SignInDto(new JwtDto(userDto, "access-token"), "refresh-token");
      given(authMapper.signInDtoFrom(existingUser, "access-token", "refresh-token"))
          .willReturn(expected);

      // when
      SignInDto result = oAuth2SignInService.signIn(
          OAuth2Provider.GOOGLE, "google-sub-1", "hong@gmail.com", "홍길동");

      // then
      assertThat(result).isEqualTo(expected);
      verify(userRepository, never()).saveAndFlush(any());
    }
  }

  @Nested
  @DisplayName("익명 로그인 시도 (자동 가입/병합)")
  class AnonymousMergeOrCreate {

    @Test
    @DisplayName("이메일이 가입되어 있지 않으면 새 계정을 생성한다")
    void 이메일이_가입되어_있지_않으면_새_계정을_생성한다() {
      // given
      given(socialAccountRepository.findByProviderAndProviderId(OAuth2Provider.KAKAO, "kakao-1"))
          .willReturn(Optional.empty());
      given(passwordEncoder.encode(any())).willReturn("random-encoded-password");

      UUID newUserId = UUID.randomUUID();
      given(userRepository.saveAndFlush(any(User.class))).willAnswer(invocation -> {
        User newUser = invocation.getArgument(0);
        ReflectionTestUtils.setField(newUser, "id", newUserId);
        return newUser;
      });

      given(clock.instant()).willReturn(NOW);
      UUID sessionId = UUID.randomUUID();
      UUID jti = UUID.randomUUID();
      UserSession issued = new UserSession(newUserId, sessionId, jti, NOW);
      given(userSessionRegistry.issue(newUserId, NOW)).willReturn(issued);
      given(tokenProvider.createAccessToken(newUserId, sessionId, "USER", NOW))
          .willReturn("access-token");
      given(tokenProvider.createRefreshToken(newUserId, sessionId, jti, NOW))
          .willReturn("refresh-token");

      UserDto expectedUserDto = new UserDto(newUserId, null, "길동이_kakao-1@kakao.com", "길동이",
          Role.USER, false);
      SignInDto expected = new SignInDto(new JwtDto(expectedUserDto, "access-token"), "refresh-token");
      given(authMapper.signInDtoFrom(any(User.class), eq("access-token"), eq("refresh-token")))
          .willReturn(expected);

      // when
      SignInDto result = oAuth2SignInService.signIn(
          OAuth2Provider.KAKAO, "kakao-1", "길동이_kakao-1@kakao.com", "길동이");

      // then
      assertThat(result).isEqualTo(expected);
      verify(profileRepository).save(any(Profile.class));
    }
  }
}
