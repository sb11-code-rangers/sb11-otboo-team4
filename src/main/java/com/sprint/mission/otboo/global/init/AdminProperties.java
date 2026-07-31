package com.sprint.mission.otboo.global.init;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
    String name,
    String email,
    String password
) {

}
