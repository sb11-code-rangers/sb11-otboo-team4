package com.sprint.mission.otboo.global.init;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
    @NotBlank String name,
    @NotBlank String email,
    @NotBlank String password
) {

}
