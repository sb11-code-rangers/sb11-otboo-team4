package com.sprint.mission.otboo.global.file.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "otboo.file")
public record FileProperties(
    @NotBlank String impl,
    @NotBlank String publicBaseUrl,
    @Positive long maxSizeBytes,
    @NotEmpty Set<String> allowedExtensions,
    Local local,
    S3 s3
) {

  public record Local(String uploadDir) {

  }

  public record S3(String bucket, String region) {

  }
}
