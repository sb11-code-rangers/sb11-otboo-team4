package com.sprint.mission.otboo.domain.authuser.user.exception;

import com.sprint.mission.otboo.global.exception.OtbooException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

public class DuplicateEmailException extends OtbooException {

    private static final String message = "이미 사용 중인 이메일입니다.";

    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT, message, Collections.emptyMap());
    }

    public DuplicateEmailException(Map<String, Object> details) {
        super(HttpStatus.CONFLICT, message, details);
    }
}
