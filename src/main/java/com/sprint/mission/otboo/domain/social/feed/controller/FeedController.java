package com.sprint.mission.otboo.domain.social.feed.controller;

import com.sprint.mission.otboo.domain.social.feed.controller.api.FeedApi;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
@RestController
public class FeedController implements FeedApi {

  private final FeedService feedService;

  @PostMapping
  @Override
  public ResponseEntity<FeedDto> createFeed(@Valid @RequestBody FeedCreateRequest request) {
    log.debug("피드 등록 요청 - authorId={}", request.authorId());
    // TODO: @CurrentUserId 도입 시 request.authorId() → 인증 사용자 ID로 교체
    FeedDto created = feedService.create(request, request.authorId());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }
}