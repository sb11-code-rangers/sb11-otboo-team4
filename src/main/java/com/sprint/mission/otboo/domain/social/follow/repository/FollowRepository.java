package com.sprint.mission.otboo.domain.social.follow.repository;

import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.repository.querydsl.FollowCustomRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowCustomRepository {

  boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  long countByFolloweeId(UUID followeeId);

  long countByFollowerId(UUID followerId);
}