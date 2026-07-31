package com.sprint.mission.otboo.domain.authuser.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.otboo.domain.authuser.auth.exception.AccessDeniedException;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ChangePasswordRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ProfileUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.exception.DuplicateEmailException;
import com.sprint.mission.otboo.domain.authuser.user.service.UserService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.SecurityArgumentResolverConfig.class)
class UserControllerTest {

  @TestConfiguration
  static class SecurityArgumentResolverConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
      resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
  }

  private UsernamePasswordAuthenticationToken authenticationOf(UUID userId) {
    UserPrincipal principal = new UserPrincipal(userId, Role.USER.name());
    return new UsernamePasswordAuthenticationToken(principal, null,
        List.of(new SimpleGrantedAuthority(Role.USER.name())));
  }

  private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Nested
  @DisplayName("회원가입 성공")
  class SignUpSuccess {

    @Test
    @DisplayName("유효한 요청으로 회원가입 시 201과 UserDto를 반환한다")
    void signUp_validRequest_returns201() throws Exception {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();
      UserDto responseDto = new UserDto(
          UUID.randomUUID(), Instant.now(), request.email(), request.name(), Role.USER, false
      );
      given(userService.signUp(any(UserCreateRequest.class))).willReturn(responseDto);

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value(request.email()))
          .andExpect(jsonPath("$.name").value(request.name()))
          .andExpect(jsonPath("$.role").value("USER"))
          .andExpect(jsonPath("$.locked").value(false));
    }
  }

  @Nested
  @DisplayName("회원가입 유효성 검증")
  class SignUpValidation {

    @Test
    @DisplayName("이름이 비어있으면 400을 반환한다")
    void signUp_blankName_returns400() throws Exception {
      UserCreateRequest request = new UserCreateRequest("", "hong@test.com", "password123");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
    void signUp_invalidEmailFormat_returns400() throws Exception {
      UserCreateRequest request = new UserCreateRequest("홍길동", "invalid-email", "password123");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일이 비어있으면 400을 반환한다")
    void signUp_blankEmail_returns400() throws Exception {
      UserCreateRequest request = new UserCreateRequest("홍길동", "", "password123");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 6자 미만이면 400을 반환한다")
    void signUp_passwordTooShort_returns400() throws Exception {
      UserCreateRequest request = new UserCreateRequest("홍길동", "hong@test.com", "1234");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 비어있으면 400을 반환한다")
    void signUp_blankPassword_returns400() throws Exception {
      UserCreateRequest request = new UserCreateRequest("홍길동", "hong@test.com", "");

      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("이메일 중복")
  class SignUpDuplicateEmail {

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 409를 반환한다")
    void signUp_duplicateEmail_returns409() throws Exception {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();
      given(userService.signUp(any(UserCreateRequest.class)))
          .willThrow(DuplicateEmailException.withEmail(request.email()));

      // when & then
      mockMvc.perform(post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("사용자 목록 조회")
  class GetUsers {

    @Test
    @DisplayName("쿼리 파라미터 없이 호출하면 기본값으로 조회해 200과 목록을 반환한다")
    void getUsers_noParams_returns200WithDefaults() throws Exception {
      // given
      UserDto userDto = new UserDto(
          UUID.randomUUID(), Instant.now(), "hong@test.com", "홍길동", Role.USER, false);
      CursorPageResponse<UserDto> response = new CursorPageResponse<>(
          List.of(userDto), "hong@test.com", userDto.id(), false, 1L, "email",
          SortDirection.ASCENDING);
      given(userService.getUsers(any(UserListParams.class))).willReturn(response);

      // when & then
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].email").value("hong@test.com"))
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("쿼리 파라미터를 조건 객체로 그대로 담아 서비스에 전달한다")
    void getUsers_withFilters_passesConditionToService() throws Exception {
      // given
      CursorPageResponse<UserDto> response = new CursorPageResponse<>(
          List.of(), null, null, false, 0L, "createdAt", SortDirection.DESCENDING);
      given(userService.getUsers(any(UserListParams.class))).willReturn(response);

      // when
      mockMvc.perform(get("/api/users")
              .param("emailLike", "hong")
              .param("roleEqual", "ADMIN")
              .param("locked", "true")
              .param("sortBy", "createdAt")
              .param("sortDirection", "DESCENDING")
              .param("limit", "20"))
          .andExpect(status().isOk());

      // then
      ArgumentCaptor<UserListParams> captor = ArgumentCaptor.forClass(
          UserListParams.class);
      verify(userService).getUsers(captor.capture());
      UserListParams captured = captor.getValue();
      assertThat(captured.emailLike()).isEqualTo("hong");
      assertThat(captured.roleEqual()).isEqualTo(Role.ADMIN);
      assertThat(captured.locked()).isTrue();
      assertThat(captured.sortBy()).isEqualTo("createdAt");
      assertThat(captured.sortDirection()).isEqualTo(SortDirection.DESCENDING);
      assertThat(captured.limit()).isEqualTo(20);
    }
  }

  @Nested
  @DisplayName("권한 수정")
  class ChangeRole {

    @Test
    @DisplayName("유효한 요청이면 200과 변경된 UserDto를 반환한다")
    void changeRole_validRequest_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserDto responseDto =
          new UserDto(userId, Instant.now(), "hong@test.com", "홍길동", Role.ADMIN, false);
      given(userService.changeRole(eq(userId), any(UserRoleUpdateRequest.class)))
          .willReturn(responseDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/role", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
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

      verify(userService, never()).changeRole(any(), any());
    }
  }

  @Nested
  @DisplayName("프로필 조회")
  class GetProfile {

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인 프로필을 조회하면 200과 ProfileDto를 반환한다")
    void getProfile_self_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(userId));
      ProfileDto responseDto =
          new ProfileDto(userId, "홍길동", Gender.MALE, LocalDate.of(1995, 1, 1), null, 3, null);
      given(userService.getProfile(userId, userId)).willReturn(responseDto);

      // when & then
      mockMvc.perform(get("/api/users/{userId}/profiles", userId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    @DisplayName("본인이 아닌 사용자의 프로필을 조회하면 403을 반환한다")
    void getProfile_notSelf_returns403() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UUID principalId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(principalId));
      given(userService.getProfile(userId, principalId)).willThrow(
          AccessDeniedException.withNone());

      // when & then
      mockMvc.perform(get("/api/users/{userId}/profiles", userId))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("프로필 수정")
  class ChangeProfile {

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인 프로필을 수정하면 200과 변경된 ProfileDto를 반환한다")
    void changeProfile_self_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(userId));
      ProfileUpdateRequest request = new ProfileUpdateRequest(
          "김철수", Gender.MALE, LocalDate.of(1995, 1, 1), null, 4);
      ProfileDto responseDto =
          new ProfileDto(userId, "김철수", Gender.MALE, LocalDate.of(1995, 1, 1), null, 4, null);
      given(
          userService.changeProfile(eq(userId), eq(userId), any(ProfileUpdateRequest.class), any()))
          .willReturn(responseDto);

      MockMultipartFile requestPart = new MockMultipartFile(
          "request", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

      // when & then
      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}/profiles", userId)
              .file(requestPart))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("김철수"));
    }

    @Test
    @DisplayName("본인이 아닌 사용자의 프로필을 수정하려 하면 403을 반환한다")
    void changeProfile_notSelf_returns403() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UUID principalId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(principalId));
      ProfileUpdateRequest request = new ProfileUpdateRequest(
          "김철수", Gender.MALE, LocalDate.of(1995, 1, 1), null, 4);
      MockMultipartFile requestPart = new MockMultipartFile(
          "request", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
      given(userService.changeProfile(
          eq(userId), eq(principalId), any(ProfileUpdateRequest.class), any()))
          .willThrow(AccessDeniedException.withNone());

      // when & then
      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}/profiles", userId)
              .file(requestPart))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("비밀번호 변경")
  class ChangePassword {

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인 비밀번호를 변경하면 200을 반환한다")
    void changePassword_self_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(userId));
      UserDto responseDto =
          new UserDto(userId, Instant.now(), "hong@test.com", "홍길동", Role.USER, false);
      given(userService.changePassword(eq(userId), eq(userId), any(ChangePasswordRequest.class)))
          .willReturn(responseDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/password", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(new ChangePasswordRequest("newpass1"))))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("본인이 아닌 사용자의 비밀번호를 변경하려 하면 403을 반환한다")
    void changePassword_notSelf_returns403() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UUID principalId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(principalId));
      given(
          userService.changePassword(eq(userId), eq(principalId), any(ChangePasswordRequest.class)))
          .willThrow(AccessDeniedException.withNone());

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/password", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(new ChangePasswordRequest("newpass1"))))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비밀번호가 6자 미만이면 400을 반환한다")
    void changePassword_tooShort_returns400() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(userId));

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/password", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(new ChangePasswordRequest("123"))))
          .andExpect(status().isBadRequest());
    }
  }


  @Nested
  @DisplayName("계정 잠금 상태 변경")
  class ChangeLocked {

    @Test
    @DisplayName("유효한 요청이면 200과 변경된 UserDto를 반환한다")
    void changeLocked_validRequest_returns200() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UserDto responseDto =
          new UserDto(userId, Instant.now(), "hong@test.com", "홍길동", Role.USER, true);
      given(userService.changeLocked(eq(userId), any(UserLockUpdateRequest.class)))
          .willReturn(responseDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/lock", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(new UserLockUpdateRequest(true))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    @DisplayName("locked 값이 없으면 400을 반환한다")
    void changeLocked_missingLocked_returns400() throws Exception {
      mockMvc.perform(patch("/api/users/{userId}/lock", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
          .andExpect(status().isBadRequest());

      verify(userService, never()).changeLocked(any(), any());
    }
  }

}
