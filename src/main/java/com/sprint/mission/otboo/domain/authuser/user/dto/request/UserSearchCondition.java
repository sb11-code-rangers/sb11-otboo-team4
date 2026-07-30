package com.sprint.mission.otboo.domain.authuser.user.dto.request;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.global.dto.SortDirection;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

import java.util.UUID;

public record UserSearchCondition(
    String cursor,

    UUID idAfter,

    @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
    Integer limit,

    @Pattern(regexp = "^(email|createdAt)$", message = "sortBy는 email 또는 createdAt만 가능합니다.")
    String sortBy,

    SortDirection sortDirection,

    @Size(max = 255, message = "emailLike는 255자를 넘을 수 없습니다.")
    String emailLike,

    Role roleEqual,

    Boolean locked
) {

  private static final int DEFAULT_LIMIT = 10;
  private static final String DEFAULT_SORT_BY = "email";
  private static final SortDirection DEFAULT_SORT_DIRECTION = SortDirection.ASCENDING;

  public UserSearchCondition {
    if (limit == null || limit <= 0) {
      limit = DEFAULT_LIMIT;
    }
    if (!StringUtils.hasText(sortBy)) {
      sortBy = DEFAULT_SORT_BY;
    }
    if (sortDirection == null) {
      sortDirection = DEFAULT_SORT_DIRECTION;
    }
  }

  @AssertTrue(message = "cursor, idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return (cursor == null && idAfter == null) || (cursor != null && idAfter != null);
  }
}
