package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.auth.dto.request.SignInRequest;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.exception.InvalidCredentialsException;
import com.sprint.mission.otboo.domain.authuser.auth.mapper.AuthMapper;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.mapper.UserMapper;
import com.sprint.mission.otboo.global.security.details.CustomUserDetails;
import com.sprint.mission.otboo.global.security.jwt.JwtProvider;
import com.sprint.mission.otboo.global.usersession.UserSession;
import com.sprint.mission.otboo.global.usersession.UserSessionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    @Mock UserSessionRegistry mockUserSessionRegistry;
    @Mock JwtProvider mockJwtProvider;
    @Mock AuthenticationManager mockAuthenticationManager;
    @Spy AuthMapper authMapper = new AuthMapper(new UserMapper());

    @Nested
    @DisplayName("로그인 성공")
    class SignInSuccess {

        @Test
        @DisplayName("AuthenticationManager 인증에 성공하면 세션을 발급하고 토큰을 생성해 SignInDto를 반환한다")
        void signIn_success_returnsSignInDto() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");
            CustomUserDetails principal = new CustomUserDetails(user);

            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(mockAuthenticationManager.authenticate(any())).willReturn(authentication);

            UserSession issuedSession = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
            Instant refreshExpiresAt = Instant.now().plus(14, ChronoUnit.DAYS);

            given(mockUserSessionRegistry.issue()).willReturn(issuedSession);
            given(mockJwtProvider.createAccessToken(
                    principal.getUserId(), principal.getRole().name(), issuedSession.sessionId(), issuedSession.issuedAt()))
                    .willReturn("access-token");
            given(mockJwtProvider.createRefreshToken(
                    principal.getUserId(), issuedSession.sessionId(), issuedSession.currentRefreshJti(), issuedSession.issuedAt()))
                    .willReturn("refresh-token");
            given(mockJwtProvider.getRefreshTokenExpiresAt(issuedSession.issuedAt())).willReturn(refreshExpiresAt);

            // when
            SignInDto result = authService.signIn(request);

            // then
            assertThat(result.jwtDto().accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.jwtDto().userDto().email()).isEqualTo(user.getEmail());
            verify(mockUserSessionRegistry).save(principal.getUserId(), issuedSession, refreshExpiresAt);
        }

        @Test
        @DisplayName("AuthenticationManager에는 폼에 입력된 username/password 그대로 인증되지 않은 토큰을 전달한다")
        void signIn_success_authenticatesWithRawCredentials() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");
            CustomUserDetails principal = new CustomUserDetails(user);

            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(mockAuthenticationManager.authenticate(any())).willReturn(authentication);
            given(mockUserSessionRegistry.issue())
                    .willReturn(new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now()));
            given(mockJwtProvider.createAccessToken(any(), any(), any(), any())).willReturn("access-token");
            given(mockJwtProvider.createRefreshToken(any(), any(), any(), any())).willReturn("refresh-token");
            given(mockJwtProvider.getRefreshTokenExpiresAt(any())).willReturn(Instant.now());

            // when
            authService.signIn(request);

            // then
            ArgumentCaptor<Authentication> tokenCaptor = ArgumentCaptor.forClass(Authentication.class);
            verify(mockAuthenticationManager).authenticate(tokenCaptor.capture());
            UsernamePasswordAuthenticationToken captured = (UsernamePasswordAuthenticationToken) tokenCaptor.getValue();
            assertThat(captured.getPrincipal()).isEqualTo(request.username());
            assertThat(captured.getCredentials()).isEqualTo(request.password());
            assertThat(captured.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("세션 저장(save)은 토큰 생성이 모두 끝난 뒤 가장 마지막에 호출된다 (트랜잭션 롤백 영향 최소화)")
        void signIn_success_savesSessionLast() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            User user = User.create("홍길동", request.username(), "encoded-password");
            CustomUserDetails principal = new CustomUserDetails(user);

            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(mockAuthenticationManager.authenticate(any())).willReturn(authentication);

            UserSession issuedSession = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
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
        @DisplayName("AuthenticationManager가 BadCredentialsException을 던지면 InvalidCredentialsException으로 변환하고 세션을 발급하지 않는다")
        void signIn_badCredentials_throwsInvalidCredentialsExceptionAndDoesNotIssueSession() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            given(mockAuthenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(mockUserSessionRegistry, never()).issue();
            verify(mockJwtProvider, never()).createAccessToken(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("계정 잠김")
    class AccountLocked {

        @Test
        @DisplayName("AuthenticationManager가 LockedException을 던지면 AccountLockedException으로 변환하고 세션을 발급하지 않는다")
        void signIn_locked_throwsAccountLockedExceptionAndDoesNotIssueSession() {
            // given
            SignInRequest request = fixtureMonkey.giveMeBuilder(SignInRequest.class).sample();
            given(mockAuthenticationManager.authenticate(any()))
                    .willThrow(new LockedException("Account is locked"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(request))
                    .isInstanceOf(AccountLockedException.class);

            verify(mockUserSessionRegistry, never()).issue();
        }
    }
}
