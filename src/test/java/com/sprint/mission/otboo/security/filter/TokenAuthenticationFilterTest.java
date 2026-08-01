package com.sprint.mission.otboo.security.filter;

import com.sprint.mission.otboo.security.details.UserPrincipal;
import com.sprint.mission.otboo.security.filter.TokenAuthenticationFilter;
import com.sprint.mission.otboo.security.token.dto.AccessTokenClaims;
import com.sprint.mission.otboo.security.token.exception.ExpiredTokenException;
import com.sprint.mission.otboo.security.token.exception.InvalidAccessTokenException;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.usersession.exception.UserSessionExpiredException;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

  @InjectMocks
  TokenAuthenticationFilter filter;

  @Mock
  TokenProvider mockTokenProvider;

  @Mock
  UserSessionRegistry mockUserSessionRegistry;

  @Mock
  FilterChain filterChain;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 인증을 시도하지 않고 필터체인을 그대로 통과시킨다")
  void doFilter_noAuthorizationHeader_passesThroughWithoutAuthenticating() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 UserPrincipal을 채운다")
  void doFilter_validBearerToken_populatesSecurityContext() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    given(mockTokenProvider.parseAccessToken("valid-token"))
        .willReturn(new AccessTokenClaims(userId, "USER", sessionId));

    filter.doFilter(request, response, filterChain);

    UserPrincipal principal =
        (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    assertThat(principal.userId()).isEqualTo(userId);
    assertThat(principal.role()).isEqualTo("USER");
    verify(mockUserSessionRegistry).verifyUserSession(userId, sessionId);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("만료된 토큰이면 X-Token-Expired 헤더를 붙이고 인증 없이 통과시킨다")
  void doFilter_expiredToken_setsExpiredHeaderAndContinuesUnauthenticated() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer expired-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    given(mockTokenProvider.parseAccessToken("expired-token")).willThrow(
        ExpiredTokenException.withNone());

    filter.doFilter(request, response, filterChain);

    assertThat(response.getHeader("X-Token-Expired")).isEqualTo("true");
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("세션이 만료/무효화됐으면 X-User-Session-Expired 헤더를 붙이고 인증 없이 통과시킨다")
  void doFilter_userSessionExpired_setsSessionExpiredHeaderAndContinuesUnauthenticated()
      throws Exception {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer some-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    given(mockTokenProvider.parseAccessToken("some-token"))
        .willReturn(new AccessTokenClaims(userId, "USER", sessionId));
    willThrow(UserSessionExpiredException.withNone())
        .given(mockUserSessionRegistry).verifyUserSession(userId, sessionId);

    filter.doFilter(request, response, filterChain);

    assertThat(response.getHeader("X-User-Session-Expired")).isEqualTo("true");
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("토큰이 유효하지 않으면 헤더 없이 로그만 남기고 인증 없이 통과시킨다")
  void doFilter_invalidToken_logsAndContinuesUnauthenticatedWithoutHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer garbage-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    given(mockTokenProvider.parseAccessToken("garbage-token"))
        .willThrow(InvalidAccessTokenException.withNone());

    filter.doFilter(request, response, filterChain);

    assertThat(response.getHeader("X-Token-Expired")).isNull();
    assertThat(response.getHeader("X-User-Session-Expired")).isNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("예상 못한 런타임 예외가 나도 필터체인은 계속 진행된다")
  void doFilter_unexpectedRuntimeException_stillContinuesChain() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer some-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    given(mockTokenProvider.parseAccessToken(any())).willThrow(new RuntimeException("예상 못한 오류"));

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
