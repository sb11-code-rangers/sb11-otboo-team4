package com.sprint.mission.otboo.global.temppassword.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.global.temppassword.generator.TempPasswordGenerator;
import com.sprint.mission.otboo.global.temppassword.generator.impl.FixedTempPasswordGenerator;
import com.sprint.mission.otboo.global.temppassword.generator.impl.RandomTempPasswordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@DisplayName("TempPasswordGeneratorConfig")
class TempPasswordGeneratorConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(TempPasswordGeneratorConfig.class);

  @Test
  @DisplayName("generator 값이 없으면 RandomTempPasswordGenerator가 등록된다")
  void generator_값이_없으면_RandomTempPasswordGenerator가_등록된다() {
    // when & then
    contextRunner.run(context -> {
      assertThat(context).hasSingleBean(TempPasswordGenerator.class);
      assertThat(context.getBean(TempPasswordGenerator.class))
          .isInstanceOf(RandomTempPasswordGenerator.class);
    });
  }

  @Test
  @DisplayName("generator가 fixed면 FixedTempPasswordGenerator가 등록된다")
  void generator가_fixed면_FixedTempPasswordGenerator가_등록된다() {
    // when & then
    contextRunner.withPropertyValues("otboo.temp-password.generator=fixed")
        .run(context -> assertThat(context.getBean(TempPasswordGenerator.class))
            .isInstanceOf(FixedTempPasswordGenerator.class));
  }
}
