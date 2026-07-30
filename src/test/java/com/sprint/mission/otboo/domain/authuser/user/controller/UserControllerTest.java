package com.sprint.mission.otboo.domain.authuser.user.controller;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.exception.DuplicateEmailException;
import com.sprint.mission.otboo.domain.authuser.user.service.UserService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {

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
}
