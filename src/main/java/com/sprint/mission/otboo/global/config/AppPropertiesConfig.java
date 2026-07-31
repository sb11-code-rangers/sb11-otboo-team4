package com.sprint.mission.otboo.global.config;

import com.sprint.mission.otboo.global.cookie.CookieProperties;
import com.sprint.mission.otboo.global.init.AdminProperties;
import com.sprint.mission.otboo.global.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    CookieProperties.class,
    AdminProperties.class
})
public class AppPropertiesConfig {

}
