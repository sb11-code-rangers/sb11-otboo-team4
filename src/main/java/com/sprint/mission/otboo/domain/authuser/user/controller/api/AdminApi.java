package com.sprint.mission.otboo.domain.authuser.user.controller.api;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface AdminApi {

  ResponseEntity<CursorPageResponse<UserDto>> searchUserList(UserListParams condition);

  ResponseEntity<UserDto> changeRole(UUID userId, UserRoleUpdateRequest request);

  ResponseEntity<UserDto> changeLock(UUID userId, UserLockUpdateRequest request);
}
