package com.sprint.mission.otboo.domain.authuser.auth.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends AuthException implements OAuth2FailureCode {

  private static final String MESSAGE = "이미 다른 계정에서 사용 중인 이메일입니다.";

  private EmailAlreadyRegisteredException(Map<String, Object> details, Throwable cause) {
    super(HttpStatus.CONFLICT, MESSAGE, details, cause);
  }

  public static EmailAlreadyRegisteredException withEmail(String email) {
    return new EmailAlreadyRegisteredException(Map.of("email", email), null);
  }

  public static EmailAlreadyRegisteredException withNone() {
    return new EmailAlreadyRegisteredException(Map.of(), null);
  }

  @Override
  public String errorCode() {
    return "email_already_registered";
  }
}
