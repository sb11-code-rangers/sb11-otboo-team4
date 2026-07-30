package com.sprint.mission.otboo.domain.authuser.user.dto.response;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
    UUID userId,
    String name,
    Gender gender,
    LocalDate birthDate,
    LocationDto location,
    int temperatureSensitivity,
    String profileImageUrl
) {

}
