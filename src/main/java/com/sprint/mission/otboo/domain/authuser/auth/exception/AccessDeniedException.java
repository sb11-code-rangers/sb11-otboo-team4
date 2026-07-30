package com.sprint.mission.otboo.domain.authuser.auth.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends AuthException {

  private static final String MESSAGE = "접근 권한이 없습니다.";

  private AccessDeniedException(Map<String, Object> details) {
    super(HttpStatus.FORBIDDEN, MESSAGE, details);
  }

  public static AccessDeniedException withNone() {
    return new AccessDeniedException(Map.of());
  }
}
