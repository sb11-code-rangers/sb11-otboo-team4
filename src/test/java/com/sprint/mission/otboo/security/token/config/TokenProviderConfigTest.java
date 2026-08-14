package com.sprint.mission.otboo.security.token.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import com.sprint.mission.otboo.security.token.properties.enums.TokenImplType;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.token.provider.impl.JjwtTokenProvider;
import com.sprint.mission.otboo.security.token.provider.impl.NimbusTokenProvider;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TokenProviderConfigTest {

  private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(TokenProviderConfig.class)
      .withBean(Clock.class, Clock::systemUTC);

  private TokenProperties propertiesOf(TokenImplType impl) {
    return new TokenProperties(Duration.ofMinutes(15), Duration.ofDays(14), SECRET, SECRET, impl);
  }

  @Nested
  class NimbusSelection {

    @Test
    void 성공_impl값이_nimbus이면_NimbusTokenProvider빈이_등록된다() {
      // given & when
      contextRunner
          .withBean(TokenProperties.class, () -> propertiesOf(TokenImplType.NIMBUS))
          .run(context -> {
            // then
            assertThat(context).hasSingleBean(TokenProvider.class);
            assertThat(context.getBean(TokenProvider.class)).isInstanceOf(NimbusTokenProvider.class);
          });
    }
  }

  @Nested
  class JjwtSelection {

    @Test
    void 성공_impl값이_jjwt이면_JjwtTokenProvider빈이_등록된다() {
      // given & when
      contextRunner
          .withBean(TokenProperties.class, () -> propertiesOf(TokenImplType.JJWT))
          .run(context -> {
            // then
            assertThat(context).hasSingleBean(TokenProvider.class);
            assertThat(context.getBean(TokenProvider.class)).isInstanceOf(JjwtTokenProvider.class);
          });
    }
  }
}
