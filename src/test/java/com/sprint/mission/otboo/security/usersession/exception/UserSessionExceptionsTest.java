package com.sprint.mission.otboo.security.usersession.exception;

import com.sprint.mission.otboo.security.usersession.exception.RefreshTokenReusedException;
import com.sprint.mission.otboo.security.usersession.exception.UserSessionExpiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserSessionExceptionsTest {

  @Nested
  @DisplayName("UserSessionExpiredException")
  class Expired {

    @Test
    @DisplayName("401 상태로 생성된다")
    void withNone_createsWithUnauthorizedStatus() {
      assertThat(UserSessionExpiredException.withNone().getStatus()).isEqualTo(
          HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  @DisplayName("RefreshTokenReusedException")
  class Reused {

    @Test
    @DisplayName("401 상태로 생성된다")
    void withNone_createsWithUnauthorizedStatus() {
      assertThat(RefreshTokenReusedException.withNone().getStatus()).isEqualTo(
          HttpStatus.UNAUTHORIZED);
    }
  }
}
