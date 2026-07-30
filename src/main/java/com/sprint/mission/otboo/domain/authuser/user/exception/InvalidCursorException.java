package com.sprint.mission.otboo.domain.authuser.user.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidCursorException extends UserException {

  private static final String MESSAGE = "잘못된 cursor 값입니다.";

  private InvalidCursorException(Map<String, Object> details) {
    super(HttpStatus.BAD_REQUEST, MESSAGE, details);
  }

  public static InvalidCursorException withCursor(String instantCursor) {
    return new InvalidCursorException(Map.of("instantCursor", instantCursor));
  }
}
