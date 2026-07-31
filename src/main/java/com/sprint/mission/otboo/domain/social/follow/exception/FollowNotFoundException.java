package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class FollowNotFoundException extends FollowException {

  private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
  private static final String MESSAGE = "팔로우를 찾을 수 없습니다.";

  private FollowNotFoundException(Map<String, Object> details) {
    super(STATUS, MESSAGE, details);
  }

  public static FollowNotFoundException of(UUID followId) {
    return new FollowNotFoundException(Map.of("followId", followId));
  }
}