package com.sprint.mission.otboo.domain.social.common.repository.querydsl;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserSummaryQueryRepository {

  UserSummary findByUserId(UUID userId);

  boolean existsByUserId(UUID userId);

  List<UserSummary> findByUserIds(Collection<UUID> userIds);
}