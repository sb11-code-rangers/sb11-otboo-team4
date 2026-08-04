package com.sprint.mission.otboo.domain.social.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.entity.FeedLike;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
@DisplayName("FeedLikeRepository")
class FeedLikeRepositoryTest {

  @Autowired
  private FeedLikeRepository feedLikeRepository;

  @Nested
  class ExistsByFeedIdAndUserId {

    @Test
    @DisplayName("해당 사용자의 좋아요가 있으면 true를 반환한다")
    void 해당_사용자의_좋아요가_있으면_true를_반환한다() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      feedLikeRepository.save(FeedLike.create(feedId, userId));

      // when
      boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feedId, userId);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("해당 사용자의 좋아요가 없으면 false를 반환한다")
    void 해당_사용자의_좋아요가_없으면_false를_반환한다() {
      // given — 같은 피드에 다른 사용자만 좋아요
      UUID feedId = UUID.randomUUID();
      feedLikeRepository.save(FeedLike.create(feedId, UUID.randomUUID()));

      // when
      boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feedId, UUID.randomUUID());

      // then
      assertThat(exists).isFalse();
    }
  }
}