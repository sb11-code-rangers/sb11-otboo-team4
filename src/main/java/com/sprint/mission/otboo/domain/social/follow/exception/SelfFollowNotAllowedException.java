package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class SelfFollowNotAllowedException extends FollowException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String MESSAGE = "자기 자신을 팔로우할 수 없습니다.";

  private SelfFollowNotAllowedException() {
    super(STATUS, MESSAGE, Map.of());
  }

  public static SelfFollowNotAllowedException withNone() {
    return new SelfFollowNotAllowedException();
  }
}