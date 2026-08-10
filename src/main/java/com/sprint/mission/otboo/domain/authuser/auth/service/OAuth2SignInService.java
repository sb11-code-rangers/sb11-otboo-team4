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

  // TODO: 아직 연동/자동가입 분기는 없다 - 기존 연동 계정이 없으면 무조건 새로 만든다.
  @Transactional
  public SignInDto signIn(OAuth2Provider provider, String providerId, String providerEmail,
      String providerName) {

    User user = findLinkedUser(provider, providerId)
        .orElseGet(() -> createSocialUser(provider, providerId, providerEmail, providerName));

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

  private User createSocialUser(OAuth2Provider provider, String providerId, String email,
      String name) {
    User newUser = User.create(name, email, passwordEncoder.encode(UUID.randomUUID().toString()));
    User savedUser = userRepository.saveAndFlush(newUser);
    profileRepository.save(Profile.create(savedUser));
    socialAccountRepository.save(SocialAccount.link(savedUser, provider, providerId, email));
    return savedUser;
  }
}
