package com.sprint.mission.otboo.domain.social.directmessage.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class DirectMessageUserNotFoundException extends DirectMessageException {

  private static final String MESSAGE = "대화 상대 정보를 찾을 수 없습니다.";

  private DirectMessageUserNotFoundException(Map<String, Object> details) {
    super(HttpStatus.NOT_FOUND, MESSAGE, details);
  }

  public static DirectMessageUserNotFoundException withNone() {
    return new DirectMessageUserNotFoundException(Collections.emptyMap());
  }
}