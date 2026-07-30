package com.sprint.mission.otboo.domain.authuser.user.repository.querydsl;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserSearchCondition;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;

public interface UserRepositoryCustom {

  CursorPageResponse<UserDto> search(UserSearchCondition condition);
}
