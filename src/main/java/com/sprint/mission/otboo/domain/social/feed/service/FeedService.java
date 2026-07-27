package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedForbiddenException;
import com.sprint.mission.otboo.domain.social.feed.mapper.FeedMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Transactional
  public FeedDto create(FeedCreateRequest request, UUID currentUserId) {
    if (!request.authorId().equals(currentUserId)) {
      throw FeedForbiddenException.authorMismatch(currentUserId, request.authorId());
    }
    Feed feed = feedRepository.save(
        Feed.create(request.authorId(), request.weatherId(), request.content()));
    log.info("피드 등록 완료: feedId={}, authorId={}", feed.getId(), feed.getAuthorId());
    return feedMapper.toDto(feed, false);
  }
}