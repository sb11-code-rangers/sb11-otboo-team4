package com.sprint.mission.otboo.domain.social.feed.repository;

import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.FeedCustomRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedCustomRepository {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Feed f set f.likeCount = f.likeCount + 1 where f.id = :feedId")
  void incrementLikeCount(@Param("feedId") UUID feedId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Feed f set f.likeCount = f.likeCount - 1 where f.id = :feedId")
  void decrementLikeCount(@Param("feedId") UUID feedId);
}