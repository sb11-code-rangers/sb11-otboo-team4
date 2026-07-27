package com.sprint.mission.otboo.domain.social.feed.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * 부분 구현: author/weather/ootds는 크로스도메인 연결 이슈에서 추가
 */
public record FeedDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String content,
    long likeCount,
    int commentCount,
    boolean likedByMe
) {

}