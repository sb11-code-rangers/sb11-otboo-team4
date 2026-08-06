package com.sprint.mission.otboo.domain.authuser.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

// AdminController는 클래스 레벨 @PreAuthorize("hasAuthority('ADMIN')")로 보호되는데, 이 애노테이션이
// 컨트롤러를 AOP 프록시로 감싸기 때문에 @WebMvcTest(AdminController.class) 슬라이스에서는
// 컨트롤러 빈 자체가 등록되지 않는 프레임워크 이슈가 있었다(GET/PATCH 모두 핸들러를 못 찾음).
// 그래서 이 테스트만 전체 컨텍스트(@SpringBootTest)로 띄워 실제 @PreAuthorize 적용을 검증한다.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminController")
class AdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AdminService adminService;

  private Authentication adminAuthentication() {
    return new UsernamePasswordAuthenticationToken(
        "admin", null, List.of(new SimpleGrantedAuthority("ADMIN")));
  }

  private Authentication userAuthentication() {
    return new UsernamePasswordAuthenticationToken(
        "user", null, List.of(new SimpleGrantedAuthority("USER")));
  }

  private UserDto userDto(UUID id, Role role, boolean locked) {
    return new UserDto(id, Instant.now(), "hong@test.com", "홍길동", role, locked);
  }

  @Nested
  @DisplayName("사용자 목록 조회 - GET /api/users")
  class SearchUserList {

    @Test
    @DisplayName("관리자면 200과 목록을 반환한다")
    void 관리자면_200과_목록을_반환한다() throws Exception {
      // given
      CursorPageResponse<UserDto> response = new CursorPageResponse<>(
          List.of(), null, null, false, 0L, "email", SortDirection.ASCENDING);
      given(adminService.searchUserList(any())).willReturn(response);

      // when & then
      mockMvc.perform(get("/api/users").with(authentication(adminAuthentication())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sortBy").value("email"));
    }

    @Test
    @DisplayName("관리자가 아니면 403을 반환한다")
    void 관리자가_아니면_403을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/users").with(authentication(userAuthentication())))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("limit이 범위를 벗어나면 400을 반환한다")
    void limit이_범위를_벗어나면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/users")
              .param("limit", "0")
              .with(authentication(adminAuthentication())))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("역할 변경 - PATCH /api/users/{userId}/role")
  class ChangeRole {

    @Test
    @DisplayName("관리자면 200과 변경된 UserDto를 반환한다")
    void 관리자면_200과_변경된_UserDto를_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      given(adminService.changeRole(eq(userId), any()))
          .willReturn(userDto(userId, Role.ADMIN, false));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(authentication(adminAuthentication()))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("관리자가 아니면 403을 반환한다")
    void 관리자가_아니면_403을_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(authentication(userAuthentication()))
              .with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 404를 반환한다")
    void 존재하지_않는_사용자면_404를_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
      willThrow(UserNotFoundException.withNone()).given(adminService).changeRole(any(), any());

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(authentication(adminAuthentication()))
              .with(csrf()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("잠금 상태 변경 - PATCH /api/users/{userId}/lock")
  class ChangeLock {

    @Test
    @DisplayName("관리자면 200과 변경된 UserDto를 반환한다")
    void 관리자면_200과_변경된_UserDto를_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);
      given(adminService.changeLock(eq(userId), any()))
          .willReturn(userDto(userId, Role.USER, true));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(authentication(adminAuthentication()))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    @DisplayName("관리자가 아니면 403을 반환한다")
    void 관리자가_아니면_403을_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserLockUpdateRequest request = new UserLockUpdateRequest(true);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(authentication(userAuthentication()))
              .with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("locked 값이 없으면 400을 반환한다")
    void locked_값이_없으면_400을_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}")
              .with(authentication(adminAuthentication()))
              .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }
}
