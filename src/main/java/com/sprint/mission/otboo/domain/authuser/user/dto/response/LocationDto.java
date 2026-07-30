package com.sprint.mission.otboo.domain.authuser.user.dto.response;

import java.util.List;

public record LocationDto(
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    List<String> locationNames
) {

}
