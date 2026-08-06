package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import jakarta.validation.Valid;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    String name,

    Gender gender,

    LocalDate birthDate,

    @Valid
    LocationRequest location,

    int temperatureSensitivity
) {

}
