package com.sprint.mission.otboo.security.token.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "otboo.auth.token")
public record TokenProperties(

    @NotNull
    @DurationMin(seconds = 1)
    Duration accessTokenExpiration,

    @NotNull
    @DurationMin(seconds = 1)
    Duration refreshTokenExpiration,

    // HS256 최소 요구 키 길이(32바이트)를 base64(padding 포함)로 인코딩했을 때의 최소 길이.
    // nimbus/jjwt 구현체 모두 accessSecret/refreshSecret을 base64로 디코드해서 사용하는 것을 전제로 함.
    @Size(min = 44, message = "accessSecret은 최소 32바이트(HS256 최소 키 길이)를 base64로 인코딩한 값이어야 합니다.")
    String accessSecret,

    @Size(min = 44, message = "refreshSecret은 최소 32바이트(HS256 최소 키 길이)를 base64로 인코딩한 값이어야 합니다.")
    String refreshSecret
) {

}
