package com.sprint.mission.otboo.security.cookie.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.otboo.security.cookie.properties.RefreshTokenCookieProperties;
import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import com.sprint.mission.otboo.security.token.properties.enums.TokenImplType;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RefreshTokenCookieExpirationValidator")
class RefreshTokenCookieExpirationValidatorTest {

  private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

  private static RefreshTokenCookieProperties cookieProperties(Duration refreshTokenExpiration) {
    return new RefreshTokenCookieProperties(refreshTokenExpiration, false);
  }

  private static TokenProperties tokenProperties(Duration refreshTokenExpiration) {
    return new TokenProperties(Duration.ofMinutes(30), refreshTokenExpiration, SECRET, SECRET,
        TokenImplType.NIMBUS);
  }

  @Nested
  @DisplayName("쿠키/토큰 만료시간 정합성 검증")
  class Validate {

    @Test
    @DisplayName("실패_쿠키_만료시간이_토큰_만료시간보다_짧으면_예외가_발생한다")
    void 실패_쿠키_만료시간이_토큰_만료시간보다_짧으면_예외가_발생한다() {
      // given
      RefreshTokenCookieProperties cookie = cookieProperties(Duration.ofDays(1));
      TokenProperties token = tokenProperties(Duration.ofDays(14));
      RefreshTokenCookieExpirationValidator validator =
          new RefreshTokenCookieExpirationValidator(cookie, token);

      // when & then
      assertThatThrownBy(validator::validate)
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("성공_쿠키_만료시간이_토큰_만료시간과_같으면_예외가_발생하지_않는다")
    void 성공_쿠키_만료시간이_토큰_만료시간과_같으면_예외가_발생하지_않는다() {
      // given
      RefreshTokenCookieProperties cookie = cookieProperties(Duration.ofDays(14));
      TokenProperties token = tokenProperties(Duration.ofDays(14));
      RefreshTokenCookieExpirationValidator validator =
          new RefreshTokenCookieExpirationValidator(cookie, token);

      // when & then
      assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("성공_쿠키_만료시간이_토큰_만료시간보다_길면_예외가_발생하지_않는다")
    void 성공_쿠키_만료시간이_토큰_만료시간보다_길면_예외가_발생하지_않는다() {
      // given
      RefreshTokenCookieProperties cookie = cookieProperties(Duration.ofDays(30));
      TokenProperties token = tokenProperties(Duration.ofDays(14));
      RefreshTokenCookieExpirationValidator validator =
          new RefreshTokenCookieExpirationValidator(cookie, token);

      // when & then
      assertThatCode(validator::validate).doesNotThrowAnyException();
    }
  }
}
