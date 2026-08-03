package com.sprint.mission.otboo.domain.social.follow.repository.querydsl;

import com.sprint.mission.otboo.domain.social.follow.dto.FollowerListParams;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;

public interface FollowCustomRepository {

  CursorPageResponse<Follow> findFollowings(FollowingListParams params);

  CursorPageResponse<Follow> findFollowers(FollowerListParams params);
}