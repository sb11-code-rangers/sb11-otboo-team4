package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

  @Transactional
  public void create(FeedCreateRequest request, UUID currentUserId) {
  }
}