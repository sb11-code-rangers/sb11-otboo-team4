package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.sprint.mission.otboo.domain.authuser.auth.dto.request.SignInRequest;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.exception.InvalidCredentialsException;
import com.sprint.mission.otboo.domain.authuser.auth.mapper.AuthMapper;
import com.sprint.mission.otboo.global.security.details.CustomUserDetails;
import com.sprint.mission.otboo.global.security.jwt.JwtProvider;
import com.sprint.mission.otboo.global.usersession.UserSession;
import com.sprint.mission.otboo.global.usersession.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserSessionRegistry userSessionRegistry;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    public SignInDto signIn(SignInRequest request) {

        Authentication authentication  = authenticate(request);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        // 로그인 성공 이후 세션 / JWT 발급
        UserSession issued = userSessionRegistry.issue();

        String accessToken = jwtProvider.createAccessToken(
                principal.getUserId(),
                principal.getRole().name(),
                issued.sessionId(),
                issued.issuedAt()
        );

        String refreshToken = jwtProvider.createRefreshToken(
                principal.getUserId(),
                issued.sessionId(),
                issued.currentRefreshJti(),
                issued.issuedAt()
        );

        SignInDto result = authMapper.signInDtoFrom(principal, accessToken, refreshToken);

        userSessionRegistry.save(principal.getUserId(), issued, jwtProvider.getRefreshTokenExpiresAt(issued.issuedAt()));

        return result;
    }

    private Authentication authenticate(SignInRequest request) {
        try {
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(), request.password()
                    )
            );
        } catch (LockedException e) {
            throw AccountLockedException.withEmail(request.username());
        } catch (AuthenticationException e) {
            throw InvalidCredentialsException.withNone();
        }
    }
}
