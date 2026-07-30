package com.sprint.mission.otboo.domain.authuser.user.controller.api;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.ChangePasswordRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ProfileUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "프로필 관리", description = "프로필 관련 API")
public interface UserApi {

  @Operation(summary = "계정 목록 조회", description = "커서 기반 페이지네이션으로 계정 목록을 조회합니다. 이메일/권한/잠금 여부로 필터링할 수 있습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "계정 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값 유효성 검증 실패 또는 잘못된 cursor 값")
  })
  ResponseEntity<CursorPageResponse<UserDto>> getUsers(UserListParams condition);

  @Operation(summary = "사용자 등록(회원가입)", description = "사용자 등록(회원가입) API")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "사용자 등록(회원가입) 성공"),
      @ApiResponse(responseCode = "400", description = "사용자 등록(회원가입) 실패"),
      @ApiResponse(responseCode = "409", description = "사용자 등록(회원가입) 이메일 중복")
  })
  ResponseEntity<UserDto> signUp(UserCreateRequest request);

  ResponseEntity<UserDto> changeRole(UUID userId, UserRoleUpdateRequest request);

  ResponseEntity<ProfileDto> getProfile(UUID userId, UserPrincipal principal);

  ResponseEntity<ProfileDto> changeProfile(UUID userId, ProfileUpdateRequest request,
      MultipartFile image, UserPrincipal principal);

  ResponseEntity<UserDto> changePassword(UUID userId, ChangePasswordRequest request,
      UserPrincipal principal);

  ResponseEntity<UserDto> changeLocked(UUID userId, UserLockUpdateRequest request);
}
