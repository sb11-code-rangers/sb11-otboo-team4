package com.sprint.mission.otboo.domain.social.feed.exception;

import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class OotdNotFoundException extends FeedException {

  private static final String MESSAGE = "등록할 수 없는 의상이 포함되어 있습니다.";

  private OotdNotFoundException(Map<String, Object> details) {
    super(HttpStatus.BAD_REQUEST, MESSAGE, details);
  }

  public static OotdNotFoundException withNone() {
    return new OotdNotFoundException(Collections.emptyMap());
  }
}