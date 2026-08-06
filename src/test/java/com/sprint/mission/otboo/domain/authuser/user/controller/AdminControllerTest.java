package com.sprint.mission.otboo.domain.authuser.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.exception.UserNotFoundException;
import com.sprint.mission.otboo.domain.authuser.user.service.AdminService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AdminController.class)
@DisplayName("AdminController")
class AdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AdminService adminService;

  private UserDto userDto(UUID id, Role role, boolean locked) {
    return new UserDto(id, Instant.now(), "hong@test.com", "홍길동", role, locked);
  }

  @Nested
  @DisplayName("사용자 목록 조회 - GET /api/users")
  class SearchUserList {

    @Test
    @DisplayName("유효한 요청이면 200과 목록을 반환한다")
    void searchUserList_validRequest_returns200() throws Exception {
      // given
      CursorPageResponse<UserDto> response = new CursorPageResponse<>(
          List.of(), null, null, false, 0L, "email", SortDirection.ASCENDING);
      given(adminService.searchUserList(any())).willReturn(response);

      // when & then
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sortBy").value("email"));
    }

    @Test
    @DisplayName("limit이 범위를 벗어나면 400을 반환한다")
    void searchUserList_limitOutOfRange_returns400() throws Exception {
      // when & then
      mockMvc.perform(get("/api/users").param("limit", "0"))
          .andExpect(status().isBadRequest());

      verify(adminService, never()).searchUserList(any());
    }
  }

  @Nested
  @DisplayName("역할 변경 - PATCH /api/users/{userId}/role")
  class ChangeRole {

    @Test
    @DisplayName("유효한 요청이면 200과 변경된 UserDto를 반환한다")
    void changeRole_validRequest_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      given(adminService.changeRole(eq(userId), any()))
          .willReturn(userDto(userId, Role.ADMIN, false));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("role이 없으면 400을 반환한다")
    void changeRole_missingRole_returns400() throws Exception {
      mockMvc.perform(patch("/api/users/{userId}/role", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest());

      verify(adminService, never()).changeRole(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 404를 반환한다")
    void changeRole_userNotFound_returns404() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      willThrow(UserNotFoundException.withNone()).given(adminService).changeRole(any(), any());

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("잠금 상태 변경 - PATCH /api/users/{userId}/lock")
  class ChangeLock {

    @Test
    @DisplayName("유효한 요청이면 200과 변경된 UserDto를 반환한다")
    void changeLock_validRequest_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      given(adminService.changeLock(eq(userId), any()))
          .willReturn(userDto(userId, Role.USER, true));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    @DisplayName("locked 값이 없으면 400을 반환한다")
    void changeLock_missingLocked_returns400() throws Exception {
      mockMvc.perform(patch("/api/users/{userId}/lock", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest());

      verify(adminService, never()).changeLock(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 404를 반환한다")
    void changeLock_userNotFound_returns404() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      willThrow(UserNotFoundException.withNone()).given(adminService).changeLock(any(), any());

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }
}
