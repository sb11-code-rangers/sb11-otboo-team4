package com.sprint.mission.otboo.domain.social.feed.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedForbiddenException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FeedService")
class FeedServiceTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  FeedService feedService = new FeedService();

  @Nested
  @DisplayName("피드 등록")
  class CreateFeed {

    @Test
    @DisplayName("작성자 ID가 인증 사용자와 다르면 FeedForbiddenException을 던진다")
    void throwsFeedForbiddenException_whenAuthorIdMismatchesCurrentUser() {
      // given
      UUID currentUserId = UUID.randomUUID();
      FeedCreateRequest request = fm.giveMeBuilder(FeedCreateRequest.class)
          .set("authorId", UUID.randomUUID())
          .sample();

      // when & then
      assertThatThrownBy(() -> feedService.create(request, currentUserId))
          .isInstanceOf(FeedForbiddenException.class);
    }
  }
}