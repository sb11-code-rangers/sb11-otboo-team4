package com.sprint.mission.otboo.security.usersession.registry.impl;

import com.sprint.mission.otboo.security.usersession.dto.UserSession;
import com.sprint.mission.otboo.security.usersession.registry.impl.UserSessionRedisRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataRedisTest
@ActiveProfiles("test")
class UserSessionRedisRegistryTest {

  @Container
  static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @Autowired
  private StringRedisTemplate redisTemplate;

  private UserSessionRedisRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new UserSessionRedisRegistry(redisTemplate);
  }

  @Nested
  @DisplayName("save / find")
  class SaveAndFind {

    @Test
    @DisplayName("저장한 세션을 find로 그대로 조회할 수 있다")
    void save_thenFind_returnsSameSession() {
      UUID userId = UUID.randomUUID();
      UserSession session = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());

      registry.save(userId, session, Instant.now().plus(14, ChronoUnit.DAYS));
      Optional<UserSession> found = registry.find(userId);

      assertThat(found).isPresent();
      assertThat(found.get().sessionId()).isEqualTo(session.sessionId());
      assertThat(found.get().currentRefreshJti()).isEqualTo(session.currentRefreshJti());
    }

    @Test
    @DisplayName("존재하지 않는 유저의 세션을 조회하면 빈 Optional을 반환한다")
    void find_noSession_returnsEmpty() {
      assertThat(registry.find(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("save는 전달받은 만료 시각으로 Redis 키의 TTL을 설정한다")
    void save_setsExpirationOnRedisKey() {
      UUID userId = UUID.randomUUID();
      UserSession session = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
      Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);

      registry.save(userId, session, expiresAt);
      Long ttlSeconds = redisTemplate.getExpire("auth:user-session:" + userId);

      assertThat(ttlSeconds).isNotNull();
      assertThat(ttlSeconds).isGreaterThan(0)
          .isLessThanOrEqualTo(Duration.ofMinutes(10).toSeconds());
    }
  }

  @Nested
  @DisplayName("revoke")
  class Revoke {

    @Test
    @DisplayName("세션을 폐기하면 이후 find는 빈 Optional을 반환한다")
    void revoke_thenFind_returnsEmpty() {
      UUID userId = UUID.randomUUID();
      UserSession session = new UserSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now());
      registry.save(userId, session, Instant.now().plus(14, ChronoUnit.DAYS));

      registry.revoke(userId);

      assertThat(registry.find(userId)).isEmpty();
    }
  }
}
