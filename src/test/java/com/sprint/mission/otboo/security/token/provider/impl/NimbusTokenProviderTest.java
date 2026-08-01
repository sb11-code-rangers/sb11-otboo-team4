package com.sprint.mission.otboo.security.token.provider.impl;

import com.sprint.mission.otboo.security.token.properties.TokenProperties;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.token.provider.TokenProviderContractTest;

import java.time.Clock;

class NimbusTokenProviderTest extends TokenProviderContractTest {

  @Override
  protected TokenProvider tokenProvider(TokenProperties properties, Clock clock) {
    return new NimbusTokenProvider(properties, clock);
  }
}
