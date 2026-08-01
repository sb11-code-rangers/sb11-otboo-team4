package com.sprint.mission.otboo.global.security.config;

import com.sprint.mission.otboo.global.security.token.properties.TokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    TokenProperties.class
})
public class SecurityPropertiesConfig {

}
