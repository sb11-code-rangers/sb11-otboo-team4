package com.sprint.mission.otboo.domain.authuser.user.controller;

import com.sprint.mission.otboo.domain.authuser.user.controller.api.UserApi;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ChangePasswordRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ProfileUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.service.UserService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.security.jwt.filter.CurrentUser;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
  
  @Override
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> changeRole(
      @PathVariable UUID userId,
      @Valid @RequestBody UserRoleUpdateRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.changeRole(userId, request));
  }

  @Override
  @GetMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> getProfile(
      @PathVariable UUID userId,
      @CurrentUser UserPrincipal principal) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.getProfile(userId, principal.userId()));
  }

  @Override
  @PatchMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> changeProfile(
      @PathVariable UUID userId,
      @Valid @RequestPart ProfileUpdateRequest request,
      @RequestPart(required = false) MultipartFile image,
      @CurrentUser UserPrincipal principal) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.changeProfile(userId, principal.userId(), request, image));
  }

  @Override
  @PatchMapping("/{userId}/password")
  public ResponseEntity<UserDto> changePassword(
      @PathVariable UUID userId,
      @Valid @RequestBody ChangePasswordRequest request,
      @CurrentUser UserPrincipal principal) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.changePassword(userId, principal.userId(), request));
  }

  @Override
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{userId}/lock")
  public ResponseEntity<UserDto> changeLocked(
      @PathVariable UUID userId,
      @Valid @RequestBody UserLockUpdateRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.changeLocked(userId, request));
  }
}
