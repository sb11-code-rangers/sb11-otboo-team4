package com.sprint.mission.otboo.security.cookie;

import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieProvider {

  public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

  private final TokenProperties tokenProperties;
  private final RefreshCookieProperties cookieProperties;

  public void attach(HttpServletResponse response, String refreshToken) {
    Duration maxAge = Duration.ofDays(tokenProperties.refreshTokenExpirationDays());
    response.addHeader(HttpHeaders.SET_COOKIE, build(refreshToken, maxAge).toString());
  }

  public void clear(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, build("", Duration.ZERO).toString());
  }

  private ResponseCookie build(String value, Duration maxAge) {
    return ResponseCookie.from(REFRESH_TOKEN, value)
        .httpOnly(true)
        .secure(cookieProperties.secure())
        .sameSite("Strict")
        .path("/api/auth")
        .maxAge(maxAge)
        .build();
  }
}
