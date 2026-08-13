package com.sprint.mission.otboo.global.temppassword.config;

import com.sprint.mission.otboo.global.temppassword.generator.TempPasswordGenerator;
import com.sprint.mission.otboo.global.temppassword.generator.impl.FixedTempPasswordGenerator;
import com.sprint.mission.otboo.global.temppassword.generator.impl.RandomTempPasswordGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempPasswordGeneratorConfig {

  @Bean
  @ConditionalOnProperty(name = "otboo.temp-password.generator", havingValue = "random", matchIfMissing = true)
  public TempPasswordGenerator randomTempPasswordGenerator() {
    return new RandomTempPasswordGenerator();
  }

  @Bean
  @ConditionalOnProperty(name = "otboo.temp-password.generator", havingValue = "fixed")
  public TempPasswordGenerator fixedTempPasswordGenerator() {
    return new FixedTempPasswordGenerator();
  }
}
