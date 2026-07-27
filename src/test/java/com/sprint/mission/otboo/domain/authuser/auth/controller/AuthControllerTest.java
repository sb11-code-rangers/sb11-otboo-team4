package com.sprint.mission.otboo.domain.authuser.auth.controller;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.auth.dto.request.SignInRequest;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.exception.InvalidCredentialsException;
import com.sprint.mission.otboo.domain.authuser.auth.service.AuthService;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.global.cookie.RefreshTokenCookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Nested
    @DisplayName("로그인 성공")
    class SignInSuccess {

        @Test
        @DisplayName("유효한 자격증명으로 로그인하면 200과 JwtDto를 반환하고 리프레시 토큰 쿠키를 첨부한다")
        void signIn_validRequest_returns200AndAttachesRefreshTokenCookie() throws Exception {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            UserDto userDto = new UserDto(UUID.randomUUID(), Instant.now(), request.username(), "홍길동", Role.USER, false);
            JwtDto jwtDto = new JwtDto(userDto, "access-token");
            SignInDto signInDto = new SignInDto(jwtDto, "refresh-token");
            given(authService.signIn(any(SignInRequest.class))).willReturn(signInDto);

            // when & then
            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", request.username())
                            .param("password", request.password()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.userDto.email").value(request.username()))
                    .andExpect(jsonPath("$.refreshToken").doesNotExist());

            verify(refreshTokenCookieProvider).attach(any(HttpServletResponse.class), eq("refresh-token"));
        }
    }

    @Nested
    @DisplayName("로그인 요청 유효성 검증")
    class SignInValidation {

        @Test
        @DisplayName("아이디가 비어있으면 400을 반환하고 로그인을 시도하지 않는다")
        void signIn_blankUsername_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "")
                            .param("password", "password123"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).signIn(any());
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
        void signIn_invalidEmailFormat_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "invalid-email")
                            .param("password", "password123"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 6자 미만이면 400을 반환한다")
        void signIn_passwordTooShort_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "hong@test.com")
                            .param("password", "1234"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 비어있으면 400을 반환한다")
        void signIn_blankPassword_returns400() throws Exception {
            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "hong@test.com")
                            .param("password", ""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("자격증명 실패")
    class InvalidCredentials {

        @Test
        @DisplayName("아이디/비밀번호가 일치하지 않으면 401을 반환하고 쿠키를 첨부하지 않는다")
        void signIn_invalidCredentials_returns401AndDoesNotAttachCookie() throws Exception {
            given(authService.signIn(any(SignInRequest.class)))
                    .willThrow(InvalidCredentialsException.withNone());

            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "hong@test.com")
                            .param("password", "wrongpass1"))  // 13자 이하로 수정 (10자)
                    .andExpect(status().isUnauthorized());

            verify(refreshTokenCookieProvider, never()).attach(any(), any());
        }
    }

    @Nested
    @DisplayName("계정 잠김")
    class AccountLocked {

        @Test
        @DisplayName("계정이 잠겨 있으면 403을 반환하고 쿠키를 첨부하지 않는다")
        void signIn_lockedAccount_returns403AndDoesNotAttachCookie() throws Exception {
            given(authService.signIn(any(SignInRequest.class)))
                    .willThrow(AccountLockedException.withEmail("hong@test.com"));

            mockMvc.perform(post("/api/auth/sign-in")
                            .param("username", "hong@test.com")
                            .param("password", "password123"))
                    .andExpect(status().isForbidden());

            verify(refreshTokenCookieProvider, never()).attach(any(), any());
        }
    }
}