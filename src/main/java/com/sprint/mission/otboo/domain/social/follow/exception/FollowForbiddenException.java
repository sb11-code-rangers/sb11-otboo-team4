package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class FollowForbiddenException extends FollowException {

  private static final HttpStatus STATUS = HttpStatus.FORBIDDEN;
  private static final String MESSAGE = "본인만 수행할 수 있습니다.";

  private FollowForbiddenException() {
    super(STATUS, MESSAGE, Map.of());
  }

  private FollowForbiddenException(Map<String, Object> details) {
    super(STATUS, MESSAGE, details);
  }

  public static FollowForbiddenException followerMismatch() {
    return new FollowForbiddenException();
  }

  public static FollowForbiddenException notOwner(UUID followId) {
    return new FollowForbiddenException(Map.of("followId", followId));
  }
}