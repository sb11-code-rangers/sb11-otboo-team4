package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import java.util.List;

public record LocationRequest(
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    List<String> locationNames
) {

}
