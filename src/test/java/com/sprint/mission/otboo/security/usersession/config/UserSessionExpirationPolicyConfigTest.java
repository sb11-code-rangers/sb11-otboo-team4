package com.sprint.mission.otboo.security.usersession.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.security.usersession.policy.UserSessionExpirationPolicy;
import com.sprint.mission.otboo.security.usersession.policy.impl.AbsoluteExpirationPolicy;
import com.sprint.mission.otboo.security.usersession.policy.impl.SlidingExpirationPolicy;
import com.sprint.mission.otboo.security.usersession.properties.UserSessionProperties;
import com.sprint.mission.otboo.security.usersession.properties.enums.ConcurrentPolicyType;
import com.sprint.mission.otboo.security.usersession.properties.enums.ExpirationPolicyType;
import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class UserSessionExpirationPolicyConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(UserSessionExpirationPolicyConfig.class);

  private UserSessionProperties propertiesOf(ExpirationPolicyType expirationPolicy) {
    return new UserSessionProperties(
        Duration.ofDays(14), null, expirationPolicy, ConcurrentPolicyType.MULTI, null);
  }

  @Nested
  class AbsoluteSelection {

    @Test
    void 성공_expirationPolicy가_absolute이면_AbsoluteExpirationPolicy빈이_등록된다() {
      // given & when
      contextRunner
          .withBean(UserSessionProperties.class,
              () -> propertiesOf(ExpirationPolicyType.ABSOLUTE))
          .run(context -> {
            // then
            assertThat(context).hasSingleBean(UserSessionExpirationPolicy.class);
            assertThat(context.getBean(UserSessionExpirationPolicy.class))
                .isInstanceOf(AbsoluteExpirationPolicy.class);
          });
    }
  }

  @Nested
  class SlidingSelection {

    @Test
    void 성공_expirationPolicy가_sliding이면_SlidingExpirationPolicy빈이_등록된다() {
      // given & when
      contextRunner
          .withBean(UserSessionProperties.class,
              () -> propertiesOf(ExpirationPolicyType.SLIDING))
          .run(context -> {
            // then
            assertThat(context).hasSingleBean(UserSessionExpirationPolicy.class);
            assertThat(context.getBean(UserSessionExpirationPolicy.class))
                .isInstanceOf(SlidingExpirationPolicy.class);
          });
    }
  }
}
