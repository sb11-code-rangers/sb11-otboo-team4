package com.sprint.mission.otboo.security.oauth2.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.service.OAuth2SignInService;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.security.cookie.provider.RefreshTokenCookieProvider;
import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2LoginSuccessHandler")
class OAuth2LoginSuccessHandlerTest {

  private static final String SUCCESS_URI = "http://localhost:8080/#/";
  private static final String FAILURE_URI = "http://localhost:8080/#/sign-in";

  @Mock
  private OAuth2SignInService oAuth2SignInService;

  @Mock
  private RefreshTokenCookieProvider refreshTokenCookieProvider;

  private OAuth2LoginSuccessHandler successHandler;

  @BeforeEach
  void setUp() {
    OAuth2Properties oAuth2Properties = new OAuth2Properties(SUCCESS_URI, FAILURE_URI);
    successHandler = new OAuth2LoginSuccessHandler(
        oAuth2SignInService, refreshTokenCookieProvider, oAuth2Properties);
  }

  private OAuth2AuthenticationToken googleToken(String sub, String email, String name,
      String registrationId) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", sub);
    attributes.put("email", email);
    attributes.put("name", name);
    OAuth2User oAuth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");
    return new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), registrationId);
  }

  private OAuth2AuthenticationToken kakaoToken(Long id, Map<String, Object> kakaoAccount,
      String registrationId) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", id);
    if (kakaoAccount != null) {
      attributes.put("kakao_account", kakaoAccount);
    }
    OAuth2User oAuth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
    return new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), registrationId);
  }

  private Map<String, Object> kakaoAccount(boolean emailVerified, String email, String nickname) {
    Map<String, Object> profile = new HashMap<>();
    profile.put("nickname", nickname);
    Map<String, Object> account = new HashMap<>();
    account.put("profile", profile);
    account.put("is_email_verified", emailVerified);
    if (email != null) {
      account.put("email", email);
    }
    return account;
  }

  private SignInDto signInDtoOf(String email, String name) {
    UserDto userDto = new UserDto(UUID.randomUUID(), Instant.now(), email, name, Role.USER, false);
    return new SignInDto(new JwtDto(userDto, "access-token"), "refresh-token");
  }

  @Nested
  @DisplayName("구글 로그인")
  class GoogleLogin {

    @Test
    @DisplayName("구글 로그인이 성공하면 리프레시 토큰 쿠키를 설정하고 성공 URI로 리다이렉트한다")
    void 구글_로그인이_성공하면_리프레시_토큰_쿠키를_설정하고_성공_URI로_리다이렉트한다() throws Exception {
      // given
      OAuth2AuthenticationToken token = googleToken("google-sub-1", "hong@gmail.com", "홍길동", "google");
      SignInDto signInDto = signInDtoOf("hong@gmail.com", "홍길동");
      given(oAuth2SignInService.signIn(
          OAuth2Provider.GOOGLE, "google-sub-1", "hong@gmail.com", "홍길동", null))
          .willReturn(signInDto);

      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      successHandler.onAuthenticationSuccess(request, response, token);

      // then
      verify(refreshTokenCookieProvider).attach(response, signInDto.refreshToken());
      assertThat(response.getRedirectedUrl()).isEqualTo(SUCCESS_URI);
    }
  }

  @Nested
  @DisplayName("카카오 로그인")
  class KakaoLogin {

    @Test
    @DisplayName("카카오 id가 숫자여도 문자열로 변환해 SignInService에 전달한다")
    void 카카오_id가_숫자여도_문자열로_변환해_SignInService에_전달한다() throws Exception {
      // given
      Map<String, Object> kakaoAccount = kakaoAccount(true, "hong@gmail.com", "홍길동");
      OAuth2AuthenticationToken token = kakaoToken(123456789L, kakaoAccount, "kakao");
      given(oAuth2SignInService.signIn(
          OAuth2Provider.KAKAO, "123456789", "hong@gmail.com", "홍길동", null))
          .willReturn(signInDtoOf("hong@gmail.com", "홍길동"));

      // when
      successHandler.onAuthenticationSuccess(
          new MockHttpServletRequest(), new MockHttpServletResponse(), token);

      // then
      verify(oAuth2SignInService).signIn(
          OAuth2Provider.KAKAO, "123456789", "hong@gmail.com", "홍길동", null);
    }
  }
}
