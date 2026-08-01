package com.sprint.mission.otboo.security.cookie;

import com.sprint.mission.otboo.security.cookie.RefreshCookieProperties;
import com.sprint.mission.otboo.security.cookie.RefreshTokenCookieProvider;
import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieProviderTest {

  private RefreshTokenCookieProvider cookieProvider;

  @BeforeEach
  void setUp() {
    TokenProperties tokenProperties = new TokenProperties(15, 14, "access-secret",
        "refresh-secret");
    RefreshCookieProperties cookieProperties = new RefreshCookieProperties(true);
    cookieProvider = new RefreshTokenCookieProvider(tokenProperties, cookieProperties);
  }

  @Test
  @DisplayName("attach는 REFRESH_TOKEN 쿠키를 httpOnly/Secure/Strict로 설정 기간만큼 발급한다")
  void attach_setsRefreshTokenCookieWithExpectedAttributes() {
    MockHttpServletResponse response = new MockHttpServletResponse();

    cookieProvider.attach(response, "refresh-token-value");

    String setCookie = response.getHeader("Set-Cookie");
    assertThat(setCookie).contains("REFRESH_TOKEN=refresh-token-value");
    assertThat(setCookie).contains("HttpOnly");
    assertThat(setCookie).contains("Secure");
    assertThat(setCookie).contains("SameSite=Strict");
    assertThat(setCookie).contains("Path=/api/auth");
    assertThat(setCookie).contains("Max-Age=" + Duration.ofDays(14).toSeconds());
  }

  @Test
  @DisplayName("clear는 값이 빈 문자열이고 Max-Age가 0인 쿠키를 내려 즉시 만료시킨다")
  void clear_setsEmptyValueAndZeroMaxAge() {
    MockHttpServletResponse response = new MockHttpServletResponse();

    cookieProvider.clear(response);

    String setCookie = response.getHeader("Set-Cookie");
    assertThat(setCookie).contains("REFRESH_TOKEN=");
    assertThat(setCookie).contains("Max-Age=0");
  }
}
