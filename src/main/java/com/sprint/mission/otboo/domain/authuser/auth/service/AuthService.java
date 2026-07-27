package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.sprint.mission.otboo.domain.authuser.auth.dto.request.SignInRequest;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.exception.InvalidCredentialsException;
import com.sprint.mission.otboo.domain.authuser.auth.mapper.AuthMapper;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.global.security.jwt.JwtProvider;
import com.sprint.mission.otboo.global.usersession.UserSession;
import com.sprint.mission.otboo.global.usersession.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRegistry userSessionRegistry;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public SignInDto signIn(SignInRequest request) {

        User foundUser = userRepository.findByEmail(request.username())
                .orElseThrow(InvalidCredentialsException::withNone);

        boolean isAuthenticated = passwordEncoder.matches(request.password(), foundUser.getPassword());

        if (!isAuthenticated) {
            throw InvalidCredentialsException.withNone();
        }

        if (foundUser.isLocked()) {
            throw AccountLockedException.withEmail(request.username());
        }

        // 로그인 성공 이후 세션 / JWT 발급
        UserSession issued = userSessionRegistry.issue();

        String accessToken = jwtProvider.createAccessToken(
                foundUser.getId(),
                foundUser.getRole().name(),
                issued.sessionId(),
                issued.issuedAt()
        );

        String refreshToken = jwtProvider.createRefreshToken(
                foundUser.getId(),
                issued.sessionId(),
                issued.currentRefreshJti(),
                issued.issuedAt()
        );

        SignInDto result = AuthMapper.signInDtoFrom(foundUser, accessToken, refreshToken);

        /**
         * 주의: 항상 마지막에 존재해야함: 트랜잭션 롤백 영향 최소화
         */
        userSessionRegistry.save(foundUser.getId(), issued, jwtProvider.getRefreshTokenExpiresAt(issued.issuedAt()));

        return result;
    }
}
