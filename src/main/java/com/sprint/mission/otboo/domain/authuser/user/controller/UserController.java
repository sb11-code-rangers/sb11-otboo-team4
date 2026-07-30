package com.sprint.mission.otboo.domain.authuser.user.controller;

import com.sprint.mission.otboo.domain.authuser.user.controller.api.UserApi;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.service.UserService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  @GetMapping("")
  public ResponseEntity<CursorPageResponse<UserDto>> getUsers(
      @Valid @ModelAttribute UserListParams condition) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.getUsers(condition));
  }

  @Override
  @PostMapping("")
  public ResponseEntity<UserDto> signUp(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userService.signUp(request));
  }
}
