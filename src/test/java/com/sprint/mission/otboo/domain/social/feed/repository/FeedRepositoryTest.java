package com.sprint.mission.otboo.domain.social.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.dto.WeatherSnapshot;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import java.util.List;
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
@DisplayName("FeedRepository")
class FeedRepositoryTest {

  static final WeatherSnapshot DUMMY_SNAPSHOT = new WeatherSnapshot(
      SkyStatus.CLEAR, PrecipitationType.NONE,
      0.0, 0.0, 28.0, 2.0, 16.0, 31.0);

  @Autowired
  private FeedRepository feedRepository;

  private Feed createAndSaveFeed(String content) {
    return feedRepository.save(
        Feed.create(UUID.randomUUID(), UUID.randomUUID(), content, DUMMY_SNAPSHOT, List.of()));
  }

  @Nested
  class LikeCounter {

    @Test
    @DisplayName("좋아요 카운트를 1 증가시킨다")
    void 좋아요_카운트를_1_증가시킨다() {
      // given
      Feed feed = createAndSaveFeed("내용");

      // when
      feedRepository.incrementLikeCount(feed.getId());

      // then
      Feed found = feedRepository.findById(feed.getId()).orElseThrow();
      assertThat(found.getLikeCount()).isEqualTo(1L);
    }
  }
}