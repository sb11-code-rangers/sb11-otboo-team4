package com.sprint.mission.otboo.security.oauth2.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.security.oauth2.properties.OAuth2Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@DisplayName("OAuth2LoginFailureHandler")
class OAuth2LoginFailureHandlerTest {

  private static final String FAILURE_URI = "http://localhost:8080/#/sign-in";

  private OAuth2LoginFailureHandler failureHandler;

  @BeforeEach
  void setUp() {
    OAuth2Properties oAuth2Properties = new OAuth2Properties("http://localhost:8080/#/", FAILURE_URI);
    failureHandler = new OAuth2LoginFailureHandler(oAuth2Properties);
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
  }
}
