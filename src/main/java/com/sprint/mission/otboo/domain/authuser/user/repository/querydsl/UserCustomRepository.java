package com.sprint.mission.otboo.domain.authuser.user.repository.querydsl;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;

public interface UserCustomRepository {

  CursorPageResponse<UserDto> search(UserListParams condition);
}
