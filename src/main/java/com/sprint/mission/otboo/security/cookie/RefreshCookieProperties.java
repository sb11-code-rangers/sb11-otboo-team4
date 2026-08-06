package com.sprint.mission.otboo.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otboo.security.refresh-cookie")
public record RefreshCookieProperties(
    boolean secure
) {

}
