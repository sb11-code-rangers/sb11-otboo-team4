package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class FollowNotFoundException extends FollowException {

  private FollowNotFoundException(Map<String, Object> details) {
    super(HttpStatus.NOT_FOUND, "팔로우를 찾을 수 없습니다.", details);
  }

  public static FollowNotFoundException of(UUID followId) {
    return new FollowNotFoundException(Map.of("followId", followId));
  }
}