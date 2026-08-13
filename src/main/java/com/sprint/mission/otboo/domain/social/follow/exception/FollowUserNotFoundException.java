package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class FollowUserNotFoundException extends FollowException {

  private static final String MESSAGE = "팔로우 사용자 정보를 찾을 수 없습니다.";

  private FollowUserNotFoundException(Map<String, Object> details) {
    super(HttpStatus.NOT_FOUND, MESSAGE, details);
  }

  public static FollowUserNotFoundException withNone() {
    return new FollowUserNotFoundException(Collections.emptyMap());
  }
}