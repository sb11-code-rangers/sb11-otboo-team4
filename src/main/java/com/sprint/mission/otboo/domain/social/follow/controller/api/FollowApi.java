package com.sprint.mission.otboo.domain.social.follow.controller.api;

import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "팔로우 관리")
public interface FollowApi {

  @Operation(summary = "팔로우 생성", operationId = "createFollow")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "팔로우 생성 성공"),
      @ApiResponse(responseCode = "400", description = "팔로우 생성 실패"),
      @ApiResponse(responseCode = "403", description = "본인만 수행할 수 있음")
  })
  ResponseEntity<FollowDto> createFollow(@Valid @RequestBody FollowCreateRequest request,
      UserPrincipal principal);

  @Operation(summary = "팔로우 취소", operationId = "cancelFollow")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "팔로우 취소 성공"),
      @ApiResponse(responseCode = "400", description = "팔로우 취소 실패"),
      @ApiResponse(responseCode = "403", description = "본인만 수행할 수 있음"),
      @ApiResponse(responseCode = "404", description = "팔로우를 찾을 수 없음")
  })
  ResponseEntity<Void> cancelFollow(UUID followId, UserPrincipal principal);
}