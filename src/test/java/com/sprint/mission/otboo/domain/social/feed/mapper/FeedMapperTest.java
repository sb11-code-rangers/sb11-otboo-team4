package com.sprint.mission.otboo.domain.social.feed.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FeedMapper")
class FeedMapperTest {

  FeedMapper feedMapper = new FeedMapper();

  @Nested
  @DisplayName("toDto 변환")
  class ToDto {

    @Test
    @DisplayName("Feed 엔티티를 FeedDto로 변환하고 likedByMe를 전달값으로 채운다")
    void mapsFeedToDtoWithGivenLikedByMe() {
      // given
      UUID authorId = UUID.randomUUID();
      UUID weatherId = UUID.randomUUID();
      Feed feed = Feed.create(authorId, weatherId, "오늘의 착장");

      // when
      FeedDto result = feedMapper.toDto(feed, false);

      // then
      assertThat(result.content()).isEqualTo("오늘의 착장");
      assertThat(result.likeCount()).isZero();
      assertThat(result.commentCount()).isZero();
      assertThat(result.likedByMe()).isFalse();
    }
  }
}