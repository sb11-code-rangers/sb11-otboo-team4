package com.sprint.mission.otboo.domain.authuser.user.controller.api;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import org.springframework.http.ResponseEntity;

public interface UserApi {

    ResponseEntity<UserDto> signUp(UserCreateRequest request);
}
