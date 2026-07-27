package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.auth.dto.request.SignInRequest;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.exception.InvalidCredentialsException;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.global.security.jwt.JwtProvider;
import com.sprint.mission.otboo.global.usersession.UserSession;
import com.sprint.mission.otboo.global.usersession.UserSessionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .build();

    @InjectMocks AuthService authService;
    @Mock UserRepository mockUserRepository;
    @Mock UserSessionRegistry mockUserSessionRegistry;
    @Mock JwtProvider mockJwtProvider;
    @Mock PasswordEncoder mockPasswordEncoder;

    @Nested
    @DisplayName("로그인 성공")
    class SignInSuccess {

        @Test
        @DisplayName("자격증명이 유효하면 세션을 발급하고 토큰을 생성해 SignInDto를 반환한다")
        void signIn_success_returnsSignInDto() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");

            UserSession issuedSession = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
            Instant refreshExpiresAt = Instant.now().plus(14, ChronoUnit.DAYS);

            given(mockUserRepository.findByEmail(request.username())).willReturn(Optional.of(user));
            given(mockPasswordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
            given(mockUserSessionRegistry.issue()).willReturn(issuedSession);
            given(mockJwtProvider.createAccessToken(
                    user.getId(), user.getRole().name(), issuedSession.sessionId(), issuedSession.issuedAt()))
                    .willReturn("access-token");
            given(mockJwtProvider.createRefreshToken(
                    user.getId(), issuedSession.sessionId(), issuedSession.currentRefreshJti(), issuedSession.issuedAt()))
                    .willReturn("refresh-token");
            given(mockJwtProvider.getRefreshTokenExpiresAt(issuedSession.issuedAt())).willReturn(refreshExpiresAt);

            // when
            SignInDto result = authService.signIn(request);

            // then
            assertThat(result.jwtDto().accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.jwtDto().userDto().email()).isEqualTo(user.getEmail());
            verify(mockUserSessionRegistry).save(user.getId(), issuedSession, refreshExpiresAt);
        }

        @Test
        @DisplayName("세션 저장(save)은 토큰 생성이 모두 끝난 뒤 가장 마지막에 호출된다 (트랜잭션 롤백 영향 최소화)")
        void signIn_success_savesSessionLast() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");
            UserSession issuedSession = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

            given(mockUserRepository.findByEmail(request.username())).willReturn(Optional.of(user));
            given(mockPasswordEncoder.matches(any(), any())).willReturn(true);
            given(mockUserSessionRegistry.issue()).willReturn(issuedSession);
            given(mockJwtProvider.createAccessToken(any(), any(), any(), any())).willReturn("access-token");
            given(mockJwtProvider.createRefreshToken(any(), any(), any(), any())).willReturn("refresh-token");
            given(mockJwtProvider.getRefreshTokenExpiresAt(any())).willReturn(Instant.now());

            // when
            authService.signIn(request);

            // then
            InOrder inOrder = inOrder(mockUserSessionRegistry, mockJwtProvider);
            inOrder.verify(mockUserSessionRegistry).issue();
            inOrder.verify(mockJwtProvider).createAccessToken(any(), any(), any(), any());
            inOrder.verify(mockJwtProvider).createRefreshToken(any(), any(), any(), any());
            inOrder.verify(mockUserSessionRegistry).save(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("자격증명 실패")
    class InvalidCredentials {

        @Test
        @DisplayName("이메일로 사용자를 찾을 수 없으면 InvalidCredentialsException을 던지고 세션을 발급하지 않는다")
        void signIn_userNotFound_throwsInvalidCredentialsExceptionAndDoesNotIssueSession() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            given(mockUserRepository.findByEmail(request.username())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(mockUserSessionRegistry, never()).issue();
            verify(mockJwtProvider, never()).createAccessToken(any(), any(), any(), any());
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 InvalidCredentialsException을 던지고 세션을 발급하지 않는다")
        void signIn_passwordMismatch_throwsInvalidCredentialsExceptionAndDoesNotIssueSession() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");

            given(mockUserRepository.findByEmail(request.username())).willReturn(Optional.of(user));
            given(mockPasswordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(mockUserSessionRegistry, never()).issue();
        }
    }

    @Nested
    @DisplayName("계정 잠김")
    class AccountLocked {

        @Test
        @DisplayName("비밀번호는 일치하지만 계정이 잠겨 있으면 AccountLockedException을 던지고 세션을 발급하지 않는다")
        void signIn_lockedAccount_throwsAccountLockedExceptionAndDoesNotIssueSession() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User lockedUser = User.create("홍길동", request.username(), "encoded-password");
            lockedUser.lock(LockReason.ADMIN_ACTION);

            given(mockUserRepository.findByEmail(request.username())).willReturn(Optional.of(lockedUser));
            given(mockPasswordEncoder.matches(request.password(), lockedUser.getPassword())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(AccountLockedException.class);

            verify(mockUserSessionRegistry, never()).issue();
        }
    }
}
