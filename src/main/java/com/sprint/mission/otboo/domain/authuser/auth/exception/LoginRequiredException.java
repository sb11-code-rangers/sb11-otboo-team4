package com.sprint.mission.otboo.domain.authuser.auth.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class LoginRequiredException extends AuthException implements OAuth2FailureCode {

  private static final String MESSAGE = "계정을 연동하려면 먼저 로그인해주세요.";

  private LoginRequiredException(Map<String, Object> details, Throwable cause) {
    super(HttpStatus.UNAUTHORIZED, MESSAGE, details, cause);
  }

  public static LoginRequiredException withNone() {
    return new LoginRequiredException(Map.of(), null);
  }

  public static LoginRequiredException withCause(Throwable cause) {
    return new LoginRequiredException(Map.of(), cause);
  }

  @Override
  public String errorCode() {
    return "login_required";
  }
}
