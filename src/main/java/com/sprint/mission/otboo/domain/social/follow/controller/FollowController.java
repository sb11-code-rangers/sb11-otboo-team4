package com.sprint.mission.otboo.domain.social.follow.controller;

import com.sprint.mission.otboo.domain.social.follow.controller.api.FollowApi;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowSummaryDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowerListParams;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.service.FollowService;
import com.sprint.mission.otboo.security.details.CurrentUser;
import com.sprint.mission.otboo.security.details.UserPrincipal;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/follows")
@RestController
public class FollowController implements FollowApi {

  private final FollowService followService;

  @PostMapping
  @Override
  public ResponseEntity<FollowDto> createFollow(
      @Valid @RequestBody FollowCreateRequest request,
      @CurrentUser UserPrincipal principal) {
    log.debug("팔로우 생성 요청: userId={}", principal.userId());
    FollowDto created = followService.create(request, principal.userId());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/summary")
  @Override
  public ResponseEntity<FollowSummaryDto> getFollowSummary(
      @RequestParam UUID userId,
      @CurrentUser UserPrincipal principal) {
    log.debug("팔로우 요약 조회 요청: userId={}", userId);
    FollowSummaryDto summary = followService.getSummary(userId, principal.userId());
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/followings")
  @Override
  public ResponseEntity<CursorPageResponse<FollowDto>> getFollowings(
      @Valid @ModelAttribute FollowingListParams params) {
    log.debug("팔로잉 목록 조회 요청: followerId={}", params.followerId());
    return ResponseEntity.ok(followService.getFollowings(params));
  }

  @GetMapping("/followers")
  @Override
  public ResponseEntity<CursorPageResponse<FollowDto>> getFollowers(
      @Valid @ModelAttribute FollowerListParams params) {
    log.debug("팔로워 목록 조회 요청: followeeId={}", params.followeeId());
    return ResponseEntity.ok(followService.getFollowers(params));
  }

  @DeleteMapping("/{followId}")
  @Override
  public ResponseEntity<Void> cancelFollow(
      @PathVariable UUID followId,
      @CurrentUser UserPrincipal principal) {
    log.debug("팔로우 취소 요청: followId={}, userId={}", followId, principal.userId());
    followService.delete(followId, principal.userId());
    return ResponseEntity.noContent().build();
  }
}