package com.sprint.mission.otboo.global.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.refresh-cookie")
public record RefreshCookieProperties(
    boolean secure
) {

}
