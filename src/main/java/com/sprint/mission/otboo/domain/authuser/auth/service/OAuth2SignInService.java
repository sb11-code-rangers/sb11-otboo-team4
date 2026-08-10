package com.sprint.mission.otboo.domain.authuser.auth.service;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
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

  // TODO: 지금은 findByProviderAndProviderId 결과를 안 쓰고 무조건 새 계정을 만든다.
  // 기존 연동 계정 로그인 분기는 다음 테스트에서 추가한다.
  @Transactional
  public SignInDto signIn(OAuth2Provider provider, String providerId, String providerEmail,
      String providerName) {

    socialAccountRepository.findByProviderAndProviderId(provider, providerId);

    User newUser = User.create(providerName, providerEmail,
        passwordEncoder.encode(UUID.randomUUID().toString()));
    User savedUser = userRepository.saveAndFlush(newUser);
    profileRepository.save(Profile.create(savedUser));
    socialAccountRepository.save(SocialAccount.link(savedUser, provider, providerId, providerEmail));

    Instant now = Instant.now(clock);
    UserSession issued = userSessionRegistry.issue(savedUser.getId(), now);
    String accessToken = tokenProvider.createAccessToken(savedUser.getId(), issued.sessionId(),
        savedUser.getRole().name(), now);
    String refreshToken = tokenProvider.createRefreshToken(savedUser.getId(), issued.sessionId(),
        issued.currentRefreshJti(), now);

    return authMapper.signInDtoFrom(savedUser, accessToken, refreshToken);
  }
}
