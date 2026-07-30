package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    String name,

    Gender gender,

    @Past(message = "생년월일은 오늘 이전 날짜여야 합니다.")
    LocalDate birthDate,

    @Valid
    LocationValidationDto location,

    @Min(value = 1, message = "체감 온도는 1 이상이어야 합니다.")
    @Max(value = 5, message = "체감 온도는 5 이하여야 합니다.")
    int temperatureSensitivity
) {

}
