package com.sprint.mission.otboo.domain.social.feed.exception;

import com.sprint.mission.otboo.global.exception.OtbooException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public abstract class FeedException extends OtbooException {

  protected FeedException(HttpStatus status, String message, Map<String, Object> details) {
    super(status, message, details);
  }
}