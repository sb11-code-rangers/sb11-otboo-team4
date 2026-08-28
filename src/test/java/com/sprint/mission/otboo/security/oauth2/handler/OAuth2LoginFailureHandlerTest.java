package com.sprint.mission.otboo.security.oauth2.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.security.cookie.properties.RefreshTokenCookieProperties;
import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import com.sprint.mission.otboo.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("OAuth2LoginFailureHandler")
class OAuth2LoginFailureHandlerTest {

  private static final String FAILURE_URI = "http://localhost:8080/#/sign-in";
  private static final String LINK_URI = "http://localhost:8080/#/settings";

  private OAuth2LoginFailureHandler failureHandler;
  private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

  @BeforeEach
  void setUp() {
    OAuth2Properties oAuth2Properties =
        new OAuth2Properties("http://localhost:8080/#/", FAILURE_URI, LINK_URI);
    authorizationRequestRepository = new HttpCookieOAuth2AuthorizationRequestRepository(
        new RefreshTokenCookieProperties(Duration.ofDays(14), false), new JsonMapper());
    failureHandler = new OAuth2LoginFailureHandler(oAuth2Properties, authorizationRequestRepository);
  }

  private MockHttpServletRequest requestWithAuthorizationRequestFor(String registrationId) {
    OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
        .authorizationUri("https://provider.example.com/oauth2/authorize")
        .clientId("client-id")
        .redirectUri("https://otboo.example.com/login/oauth2/code/" + registrationId)
        .scopes(Set.of("email", "profile"))
        .state("state-value")
        .attributes(Map.of(OAuth2ParameterNames.REGISTRATION_ID, registrationId))
        .build();

    MockHttpServletResponse saveResponse = new MockHttpServletResponse();
    authorizationRequestRepository.saveAuthorizationRequest(
        authorizationRequest, new MockHttpServletRequest(), saveResponse);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(saveResponse.getCookies());
    return request;
  }

  @Nested
  @DisplayName("인증 실패 처리 (onAuthenticationFailure)")
  class OnAuthenticationFailure {

    @Test
    @DisplayName("OAuth2AuthenticationException이면 에러 코드를 추출해 실패 URI로 리다이렉트한다")
    void OAuth2AuthenticationException이면_에러_코드를_추출해_실패_URI로_리다이렉트한다() throws Exception {
      // given
      OAuth2AuthenticationException exception =
          new OAuth2AuthenticationException(new OAuth2Error("invalid_request"), "invalid_request");
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      failureHandler.onAuthenticationFailure(request, response, exception);

      // then
      assertThat(response.getRedirectedUrl())
          .startsWith(FAILURE_URI)
          .contains("error_message=invalid_request");
    }

    @Test
    @DisplayName("OAuth2AuthenticationException이 아니면 unknown_error로 리다이렉트한다")
    void OAuth2AuthenticationException이_아니면_unknown_error로_리다이렉트한다() throws Exception {
      // given
      BadCredentialsException exception = new BadCredentialsException("자격 증명 실패");
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      failureHandler.onAuthenticationFailure(request, response, exception);

      // then
      assertThat(response.getRedirectedUrl()).contains("error_message=unknown_error");
    }

    @Test
    @DisplayName("연동(-link) 요청이었으면 연동 리다이렉트 URI로 이동한다")
    void 연동_link_요청이었으면_연동_리다이렉트_URI로_이동한다() throws Exception {
      // given
      OAuth2AuthenticationException exception =
          new OAuth2AuthenticationException(new OAuth2Error("access_denied"), "access_denied");
      MockHttpServletRequest request = requestWithAuthorizationRequestFor("google-link");
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      failureHandler.onAuthenticationFailure(request, response, exception);

      // then
      assertThat(response.getRedirectedUrl())
          .startsWith(LINK_URI)
          .contains("error_message=access_denied");
    }

    @Test
    @DisplayName("로그인 요청이었으면 실패 URI로 이동한다")
    void 로그인_요청이었으면_실패_URI로_이동한다() throws Exception {
      // given
      OAuth2AuthenticationException exception =
          new OAuth2AuthenticationException(new OAuth2Error("access_denied"), "access_denied");
      MockHttpServletRequest request = requestWithAuthorizationRequestFor("google");
      MockHttpServletResponse response = new MockHttpServletResponse();

      // when
      failureHandler.onAuthenticationFailure(request, response, exception);

      // then
      assertThat(response.getRedirectedUrl()).startsWith(FAILURE_URI);
    }
  }
}
