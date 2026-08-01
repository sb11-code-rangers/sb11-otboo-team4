package com.sprint.mission.otboo.security.token.exception;

import com.sprint.mission.otboo.security.token.exception.ExpiredTokenException;
import com.sprint.mission.otboo.security.token.exception.InvalidAccessTokenException;
import com.sprint.mission.otboo.security.token.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TokenExceptionsTest {

  @Nested
  @DisplayName("ExpiredTokenException")
  class Expired {

    @Test
    @DisplayName("withNone은 cause 없이, withCause는 원인 예외를 보존한 채 401로 생성된다")
    void factories_createWithExpectedStatusAndCause() {
      Throwable cause = new RuntimeException("원인");
      assertThat(ExpiredTokenException.withNone().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(ExpiredTokenException.withNone().getCause()).isNull();
      assertThat(ExpiredTokenException.withCause(cause).getCause()).isSameAs(cause);
    }
  }

  @Nested
  @DisplayName("InvalidAccessTokenException")
  class InvalidAccess {

    @Test
    @DisplayName("withNone은 cause 없이, withCause는 원인 예외를 보존한 채 401로 생성된다")
    void factories_createWithExpectedStatusAndCause() {
      Throwable cause = new RuntimeException("원인");
      assertThat(InvalidAccessTokenException.withNone().getStatus()).isEqualTo(
          HttpStatus.UNAUTHORIZED);
      assertThat(InvalidAccessTokenException.withCause(cause).getCause()).isSameAs(cause);
    }
  }

  @Nested
  @DisplayName("InvalidRefreshTokenException")
  class InvalidRefresh {

    @Test
    @DisplayName("withNone은 cause 없이, withCause는 원인 예외를 보존한 채 401로 생성된다")
    void factories_createWithExpectedStatusAndCause() {
      Throwable cause = new RuntimeException("원인");
      assertThat(InvalidRefreshTokenException.withNone().getStatus()).isEqualTo(
          HttpStatus.UNAUTHORIZED);
      assertThat(InvalidRefreshTokenException.withCause(cause).getCause()).isSameAs(cause);
    }
  }
}
