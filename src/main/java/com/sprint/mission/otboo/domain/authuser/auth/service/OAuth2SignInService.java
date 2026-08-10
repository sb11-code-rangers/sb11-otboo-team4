package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.auth.exception.AccountLockedException;
import com.sprint.mission.otboo.domain.authuser.auth.mapper.AuthMapper;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.SocialAccount;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.SocialAccountRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.usersession.dto.UserSession;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuth2SignInService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final UserSessionRegistry userSessionRegistry;
  private final TokenProvider tokenProvider;
  private final AuthMapper authMapper;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  @Transactional
  public SignInDto signIn(OAuth2Provider provider, String providerId, String providerEmail,
      String providerName) {

    User user = findLinkedUser(provider, providerId)
        .orElseGet(() -> resolveAnonymousUser(provider, providerId, providerEmail, providerName));

    if (user.isLocked()) {
      throw AccountLockedException.withNone();
    }

    Instant now = Instant.now(clock);
    UserSession issued = userSessionRegistry.issue(user.getId(), now);
    String accessToken = tokenProvider.createAccessToken(user.getId(), issued.sessionId(),
        user.getRole().name(), now);
    String refreshToken = tokenProvider.createRefreshToken(user.getId(), issued.sessionId(),
        issued.currentRefreshJti(), now);

    return authMapper.signInDtoFrom(user, accessToken, refreshToken);
  }

  private Optional<User> findLinkedUser(OAuth2Provider provider, String providerId) {
    return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
        .map(SocialAccount::getUser);
  }

  // 로그인 화면에서의 익명 시도 - 이메일이 이미 가입되어 있으면 병합, 없으면 새로 만든다.
  private User resolveAnonymousUser(OAuth2Provider provider, String providerId, String email,
      String name) {
    return userRepository.findByEmail(email)
        .map(existingUser -> mergeWithPasswordInvalidation(existingUser, provider, providerId, email))
        .orElseGet(() -> createSocialUser(provider, providerId, email, name));
  }

  // 익명 방문자의 소셜 이메일이 이미 가입된 로컬 계정과 같은 경우 - 연동 + 비밀번호 무효화.
  // 비밀번호를 그대로 두면 공격자가 피해자 이메일로 먼저 로컬 가입한 뒤 소셜 로그인으로
  // 계정을 가로챌 수 있어서, 병합 시점에 기존 비밀번호를 무작위 값으로 덮어쓰고 세션을 회수한다.
  private User mergeWithPasswordInvalidation(User existingUser, OAuth2Provider provider,
      String providerId, String email) {
    existingUser.changePassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    userSessionRegistry.revokeAll(existingUser.getId());
    socialAccountRepository.save(SocialAccount.link(existingUser, provider, providerId, email));
    return existingUser;
  }

  private User createSocialUser(OAuth2Provider provider, String providerId, String email,
      String name) {
    User newUser = User.create(name, email, passwordEncoder.encode(UUID.randomUUID().toString()));
    User savedUser = userRepository.saveAndFlush(newUser);
    profileRepository.save(Profile.create(savedUser));
    socialAccountRepository.save(SocialAccount.link(savedUser, provider, providerId, email));
    return savedUser;
  }
}
