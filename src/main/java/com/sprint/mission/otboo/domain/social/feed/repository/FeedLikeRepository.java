package com.sprint.mission.otboo.domain.social.feed.repository;

import com.sprint.mission.otboo.domain.social.feed.entity.FeedLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

  boolean existsByFeedIdAndUserId(UUID feedId, UUID userId);
}