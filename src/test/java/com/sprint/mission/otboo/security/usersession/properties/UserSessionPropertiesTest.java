package com.sprint.mission.otboo.security.usersession.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.security.usersession.properties.enums.ConcurrentPolicyType;
import com.sprint.mission.otboo.security.usersession.properties.enums.ExpirationPolicyType;
import com.sprint.mission.otboo.security.usersession.properties.enums.UserSessionRegistryType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserSessionPropertiesTest {

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  private static UserSessionProperties properties(Duration userSessionExpiration) {
    return new UserSessionProperties(userSessionExpiration, 3, ExpirationPolicyType.ABSOLUTE,
        ConcurrentPolicyType.MULTI, UserSessionRegistryType.REDIS);
  }

  @Nested
  class UserSessionExpiration {

    @Test
    void 성공_1초_이상이면_위반이_없다() {
      // given
      UserSessionProperties props = properties(Duration.ofDays(14));

      // when
      Set<ConstraintViolation<UserSessionProperties>> violations = VALIDATOR.validate(props);

      // then
      assertThat(violations).isEmpty();
    }

    @Test
    void 실패_null이면_위반이_발생한다() {
      // given
      UserSessionProperties props = properties(null);

      // when
      Set<ConstraintViolation<UserSessionProperties>> violations = VALIDATOR.validate(props);

      // then
      assertThat(violations).isNotEmpty();
    }

    @Test
    void 실패_1초_미만이면_위반이_발생한다() {
      // given
      UserSessionProperties props = properties(Duration.ofMillis(999));

      // when
      Set<ConstraintViolation<UserSessionProperties>> violations = VALIDATOR.validate(props);

      // then
      assertThat(violations).isNotEmpty();
    }
  }
}
