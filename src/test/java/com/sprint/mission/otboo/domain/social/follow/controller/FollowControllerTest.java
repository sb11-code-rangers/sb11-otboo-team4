package com.sprint.mission.otboo.domain.social.follow.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowSummaryDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowerListParams;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.exception.FollowForbiddenException;
import com.sprint.mission.otboo.domain.social.follow.exception.FollowNotFoundException;
import com.sprint.mission.otboo.domain.social.follow.service.FollowService;
import com.sprint.mission.otboo.security.details.UserPrincipal;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FollowController.class)
@Import(FollowControllerTest.SecurityArgumentResolverConfig.class)
@DisplayName("FollowController")
class FollowControllerTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  FollowService followService;

  private Authentication authenticationOf(UUID userId) {
    UserPrincipal principal = new UserPrincipal(userId, "USER");
    return new UsernamePasswordAuthenticationToken(
        principal, null, List.of(new SimpleGrantedAuthority("USER")));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @TestConfiguration
  static class SecurityArgumentResolverConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
      resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
  }

  @Nested
  @DisplayName("팔로우 생성 - POST /api/follows")
  class CreateFollow {

    @Test
    @DisplayName("정상 요청이면 201과 FollowDto를 반환한다")
    void 정상_요청이면_201과_FollowDto를_반환한다() throws Exception {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(followerId));

      FollowCreateRequest request = fm.giveMeBuilder(FollowCreateRequest.class)
          .set("followerId", followerId)
          .set("followeeId", followeeId)
          .sample();

      UserSummary follower = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followerId).sample();
      UserSummary followee = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followeeId).sample();
      FollowDto response = new FollowDto(UUID.randomUUID(), followee, follower);
      when(followService.create(any(FollowCreateRequest.class), eq(followerId)))
          .thenReturn(response);

      // when & then
      mockMvc.perform(post("/api/follows")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(response.id().toString()))
          .andExpect(jsonPath("$.follower.userId").value(followerId.toString()))
          .andExpect(jsonPath("$.followee.userId").value(followeeId.toString()));
    }

    @Test
    @DisplayName("followeeId가 없으면 400을 반환한다")
    void followeeId가_없으면_400을_반환한다() throws Exception {
      // given — followeeId 누락
      FollowCreateRequest request = new FollowCreateRequest(null, UUID.randomUUID());

      // when & then
      mockMvc.perform(post("/api/follows")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("팔로우 요약 조회 - GET /api/follows/summary")
  class getFollowSummary {

    @Test
    @DisplayName("정상 요청이면 200과 FollowSummaryDto를 반환한다")
    void 정상_요청이면_200과_FollowSummaryDto를_반환한다() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      UUID me = UUID.randomUUID();
      UUID followId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(me));

      FollowSummaryDto response =
          new FollowSummaryDto(userId, 10L, 5L, true, followId, false);
      when(followService.getSummary(userId, me)).thenReturn(response);

      // when & then
      mockMvc.perform(get("/api/follows/summary").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.followeeId").value(userId.toString()))
          .andExpect(jsonPath("$.followerCount").value(10))
          .andExpect(jsonPath("$.followingCount").value(5))
          .andExpect(jsonPath("$.followedByMe").value(true))
          .andExpect(jsonPath("$.followedByMeId").value(followId.toString()))
          .andExpect(jsonPath("$.followingMe").value(false));

      verify(followService).getSummary(userId, me);
    }

    @Test
    @DisplayName("userId 파라미터가 없으면 400을 반환한다")
    void userId_파라미터가_없으면_400을_반환한다() throws Exception {
      // given
      UUID me = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(me));

      // when & then
      mockMvc.perform(get("/api/follows/summary"))
          .andExpect(status().isBadRequest());

      verify(followService, never()).getSummary(any(), any());
    }
  }

  @Nested
  @DisplayName("팔로잉 목록 조회 - GET /api/follows/followings")
  class GetFollowings {

    @Test
    @DisplayName("정상 요청이면 200과 CursorPageResponse를 반환한다")
    void 정상_요청이면_200과_CursorPageResponse를_반환한다() throws Exception {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      FollowDto dto = new FollowDto(
          UUID.randomUUID(),
          new UserSummary(followeeId, "팔로위", null),
          new UserSummary(followerId, "팔로워", null));

      CursorPageResponse<FollowDto> response = new CursorPageResponse<>(
          List.of(dto), null, null, false, 1L, "createdAt", SortDirection.DESCENDING);
      when(followService.getFollowings(any(FollowingListParams.class))).thenReturn(response);

      // when
      var result = mockMvc.perform(get("/api/follows/followings")
          .param("followerId", followerId.toString())
          .param("limit", "10")
          .param("nameLike", "팔로위"));

      // then
      result
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.sortBy").value("createdAt"))
          .andExpect(jsonPath("$.data[0].followee.userId").value(followeeId.toString()));

      ArgumentCaptor<FollowingListParams> captor =
          ArgumentCaptor.forClass(FollowingListParams.class);
      verify(followService).getFollowings(captor.capture());
      FollowingListParams captured = captor.getValue();
      assertThat(captured.followerId()).isEqualTo(followerId);
      assertThat(captured.limit()).isEqualTo(10);
      assertThat(captured.nameLike()).isEqualTo("팔로위");
    }

    @Test
    @DisplayName("limit이 1 미만이면 400을 반환한다")
    void limit이_1_미만이면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followings")
              .param("followerId", UUID.randomUUID().toString())
              .param("limit", "0"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor만 있고 idAfter가 없으면 400을 반환한다")
    void cursor만_있고_idAfter가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followings")
              .param("followerId", UUID.randomUUID().toString())
              .param("limit", "10")
              .param("cursor", "2026-07-28T00:00:00Z"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor 형식이 잘못되면 400을 반환한다")
    void cursor_형식이_잘못되면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followings")
              .param("followerId", UUID.randomUUID().toString())
              .param("limit", "10")
              .param("cursor", "invalid-instant")
              .param("idAfter", UUID.randomUUID().toString()))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("팔로워 목록 조회 - GET /api/follows/followers")
  class GetFollowers {

    @Test
    @DisplayName("정상 요청이면 200과 CursorPageResponse를 반환한다")
    void 정상_요청이면_200과_CursorPageResponse를_반환한다() throws Exception {
      // given
      UUID followeeId = UUID.randomUUID();
      UUID followerId = UUID.randomUUID();
      FollowDto dto = new FollowDto(
          UUID.randomUUID(),
          new UserSummary(followeeId, "팔로위", null),
          new UserSummary(followerId, "팔로워", null));
      CursorPageResponse<FollowDto> response = new CursorPageResponse<>(
          List.of(dto), null, null, false, 1L, "createdAt", SortDirection.DESCENDING);
      when(followService.getFollowers(any(FollowerListParams.class))).thenReturn(response);

      // when
      var result = mockMvc.perform(get("/api/follows/followers")
          .param("followeeId", followeeId.toString())
          .param("limit", "10")
          .param("nameLike", "팔로워"));

      // then
      result
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.sortBy").value("createdAt"))
          .andExpect(jsonPath("$.data[0].follower.userId").value(followerId.toString()));

      ArgumentCaptor<FollowerListParams> captor =
          ArgumentCaptor.forClass(FollowerListParams.class);
      verify(followService).getFollowers(captor.capture());
      FollowerListParams captured = captor.getValue();
      assertThat(captured.followeeId()).isEqualTo(followeeId);
      assertThat(captured.limit()).isEqualTo(10);
      assertThat(captured.nameLike()).isEqualTo("팔로워");
    }

    @Test
    @DisplayName("limit이 1 미만이면 400을 반환한다")
    void limit이_1_미만이면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followers")
              .param("followeeId", UUID.randomUUID().toString())
              .param("limit", "0"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor만 있고 idAfter가 없으면 400을 반환한다")
    void cursor만_있고_idAfter가_없으면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followers")
              .param("followeeId", UUID.randomUUID().toString())
              .param("limit", "10")
              .param("cursor", "2026-07-28T00:00:00Z"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor 형식이 잘못되면 400을 반환한다")
    void cursor_형식이_잘못되면_400을_반환한다() throws Exception {
      // when & then
      mockMvc.perform(get("/api/follows/followers")
              .param("followeeId", UUID.randomUUID().toString())
              .param("limit", "10")
              .param("cursor", "invalid-instant")
              .param("idAfter", UUID.randomUUID().toString()))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("팔로우 취소 - DELETE /api/follows/{followId}")
  class CancelFollow {

    @Test
    @DisplayName("정상 요청이면 204를 반환한다")
    void 정상_요청이면_204를_반환한다() throws Exception {
      // given
      UUID followId = UUID.randomUUID();
      UUID currentUserId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(currentUserId));

      // when & then
      mockMvc.perform(delete("/api/follows/{followId}", followId))
          .andExpect(status().isNoContent());

      verify(followService).delete(followId, currentUserId);
    }

    @Test
    @DisplayName("존재하지 않는 팔로우면 404를 반환한다")
    void 존재하지_않는_팔로우면_404를_반환한다() throws Exception {
      //given
      UUID followId = UUID.randomUUID();
      UUID currentUserId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(currentUserId));
      willThrow(FollowNotFoundException.of(followId))
          .given(followService).delete(followId, currentUserId);

      // when & then
      mockMvc.perform(delete("/api/follows/{followId}", followId))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("본인의 팔로우가 아니면 403을 반환한다")
    void 본인의_팔로우가_아니면_403을_반환한다() throws Exception {
      // given
      UUID followId = UUID.randomUUID();
      UUID currentUserId = UUID.randomUUID();
      SecurityContextHolder.getContext().setAuthentication(authenticationOf(currentUserId));
      willThrow(FollowForbiddenException.notOwner(followId))
          .given(followService).delete(followId, currentUserId);

      // when & then
      mockMvc.perform(delete("/api/follows/{followId}", followId))
          .andExpect(status().isForbidden());
    }
  }
}