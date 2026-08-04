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

  @Nested
  class DeleteByFeedIdAndUserId {

    @Test
    @DisplayName("좋아요가 있으면 삭제하고 삭제된 행 수 1을 반환한다")
    void 좋아요가_있으면_삭제하고_삭제된_행_수_1을_반환한다() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      feedLikeRepository.save(FeedLike.create(feedId, userId));

      // when
      long deleted = feedLikeRepository.deleteByFeedIdAndUserId(feedId, userId);

      // then
      assertThat(deleted).isEqualTo(1L);
      assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).isFalse();
    }

    @Test
    @DisplayName("좋아요가 없으면 아무것도 삭제하지 않고 0을 반환한다")
    void 좋아요가_없으면_아무것도_삭제하지_않고_0을_반환한다() {
      // given — 저장 없음

      // when
      long deleted = feedLikeRepository.deleteByFeedIdAndUserId(UUID.randomUUID(),
          UUID.randomUUID());

      // then
      assertThat(deleted).isEqualTo(0L);
    }
  }
}