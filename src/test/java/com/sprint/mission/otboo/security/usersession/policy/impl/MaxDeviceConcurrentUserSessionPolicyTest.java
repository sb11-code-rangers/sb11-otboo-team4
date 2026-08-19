package com.sprint.mission.otboo.security.usersession.policy.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.security.usersession.dto.UserSession;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaxDeviceConcurrentUserSessionPolicyTest {

  private static final int MAX_DEVICES = 3;

  @Mock
  private UserSessionRegistry registry;

  private final MaxDeviceConcurrentUserSessionPolicy policy =
      new MaxDeviceConcurrentUserSessionPolicy(MAX_DEVICES);

  @Nested
  class Issue {

    @Test
    void 성공_registry의_evictOldestAndIssue를_maxDevices와_함께_호출하고_결과를_그대로_반환한다() {
      // given
      UUID userId = UUID.randomUUID();
      Instant now = Instant.parse("2026-01-10T00:00:00Z");
      UserSession expected = new UserSession(userId, UUID.randomUUID(), UUID.randomUUID(), now);
      given(registry.evictOldestAndIssue(userId, MAX_DEVICES, now)).willReturn(expected);

      // when
      UserSession result = policy.issue(userId, now, registry);

      // then
      assertThat(result).isEqualTo(expected);
    }

    @Test
    void 성공_evictOldestAndIssue만_호출하고_findAllByUserId나_revoke나_일반_issue는_호출하지_않는다() {
      // given
      UUID userId = UUID.randomUUID();
      Instant now = Instant.parse("2026-01-10T00:00:00Z");
      given(registry.evictOldestAndIssue(userId, MAX_DEVICES, now))
          .willReturn(new UserSession(userId, UUID.randomUUID(), UUID.randomUUID(), now));

      // when
      policy.issue(userId, now, registry);

      // then
      verify(registry).evictOldestAndIssue(userId, MAX_DEVICES, now);
      verify(registry, never()).findAllByUserId(any(UUID.class));
      verify(registry, never()).revoke(any(UUID.class), any(UUID.class));
      verify(registry, never()).issue(any(UUID.class), any(Instant.class));
    }
  }
}
