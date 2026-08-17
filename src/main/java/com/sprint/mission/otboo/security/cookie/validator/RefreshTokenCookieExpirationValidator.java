package com.sprint.mission.otboo.security.cookie.validator;

import com.sprint.mission.otboo.security.cookie.properties.RefreshTokenCookieProperties;
import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieExpirationValidator {

  private final RefreshTokenCookieProperties cookieProperties;
  private final TokenProperties tokenProperties;

  @PostConstruct
  public void validate() {
    if (cookieProperties.refreshTokenExpiration().compareTo(tokenProperties.refreshTokenExpiration()) < 0) {
      throw new IllegalStateException(
          "refresh-cookie.refresh-token-expiration은 token.refresh-token-expiration보다 짧을 수 없습니다.");
    }
  }
}
