package com.sprint.mission.otboo.domain.authuser.auth.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends AuthException {

  private static final String MESSAGE = "이미 다른 계정에서 사용 중인 이메일입니다.";

  private EmailAlreadyRegisteredException(Map<String, Object> details, Throwable cause) {
    super(HttpStatus.CONFLICT, MESSAGE, details, cause);
  }

  public static EmailAlreadyRegisteredException withEmail(String email) {
    return new EmailAlreadyRegisteredException(Map.of("email", email), null);
  }
}
