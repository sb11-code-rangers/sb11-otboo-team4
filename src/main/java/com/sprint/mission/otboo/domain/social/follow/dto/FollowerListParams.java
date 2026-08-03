package com.sprint.mission.otboo.domain.social.follow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FollowerListParams(
    @NotNull UUID followeeId,
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) @Max(100) Integer limit,
    String nameLike
) implements CursorListParams {

}