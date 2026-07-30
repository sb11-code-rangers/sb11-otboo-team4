package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LocationValidationDto(
    @NotNull(message = "위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    Double longitude,

    @NotNull(message = "위치 X좌표는 필수입니다.")
    Integer x,

    @NotNull(message = "위치 Y좌표는 필수입니다.")
    Integer y,

    @Size(max = 5, message = "행정구역명은 5개 이하여야 합니다.")
    List<String> locationNames
) {

}
