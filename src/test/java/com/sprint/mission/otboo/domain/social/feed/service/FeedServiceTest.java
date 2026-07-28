package com.sprint.mission.otboo.domain.social.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedForbiddenException;
import com.sprint.mission.otboo.domain.social.feed.mapper.FeedMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedService")
class FeedServiceTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @InjectMocks
  FeedService feedService;

  @Mock
  FeedRepository feedRepository;

  @Mock
  FeedMapper feedMapper;

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

      /// when & then
      assertThatThrownBy(() -> feedService.create(request, currentUserId))
          .isInstanceOf(FeedForbiddenException.class)
          .satisfies(ex -> {
            FeedForbiddenException fe = (FeedForbiddenException) ex;
            assertThat(fe.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(fe.getDetails())
                .containsEntry("currentUserId", currentUserId)
                .containsKey("requestedAuthorId");
          });
    }

    @Test
    @DisplayName("정상 요청이면 피드를 저장하고 FeedDto를 반환한다")
    void savesFeedAndReturnsDto_whenRequestIsValid() {
      // given
      UUID currentUserId = UUID.randomUUID();
      FeedCreateRequest request = fm.giveMeBuilder(FeedCreateRequest.class)
          .set("authorId", currentUserId)
          .sample();

      FeedDto expected = new FeedDto(
          UUID.randomUUID(), null, null, request.content(), 0L, 0, false);
      when(feedRepository.save(any(Feed.class))).thenAnswer(inv -> inv.getArgument(0));
      when(feedMapper.toDto(any(Feed.class), any(Boolean.class))).thenReturn(expected);

      // when
      FeedDto result = feedService.create(request, currentUserId);

      // then
      ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
      verify(feedRepository).save(captor.capture());
      Feed saved = captor.getValue();
      assertThat(saved.getAuthorId()).isEqualTo(currentUserId);
      assertThat(saved.getWeatherId()).isEqualTo(request.weatherId());
      assertThat(saved.getContent()).isEqualTo(request.content());
      assertThat(saved.getLikeCount()).isZero();
      assertThat(saved.getCommentCount()).isZero();
      assertThat(result).isEqualTo(expected);
    }
  }
}