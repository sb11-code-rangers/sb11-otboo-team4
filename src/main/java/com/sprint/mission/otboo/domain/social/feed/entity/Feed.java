package com.sprint.mission.otboo.domain.social.feed.entity;

import com.sprint.mission.otboo.global.entity.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "feeds")
@Entity
public class Feed {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Embedded
  private SoftDeletable softDeletable = new SoftDeletable();

  @Column(name = "author_id", nullable = false)
  private UUID authorId;

  // 스냅샷 원본 참조용 (FK 없음)
  @Column(name = "weather_id")
  private UUID weatherId;

  // TODO: 날씨 스냅샷 — 연결 이슈에서 채움 (String → enum)
  @Column(name = "sky_status")
  private String skyStatus;

  @Column(name = "precipitation_type")
  private String precipitationType;

  @Column(name = "precipitation_amount")
  private Double precipitationAmount;

  @Column(name = "precipitation_probability")
  private Double precipitationProbability;

  @Column(name = "temperature_current")
  private Double temperatureCurrent;

  @Column(name = "temperature_compared")
  private Double temperatureCompared;

  // TODO: 착장 스냅샷 — 연결 이슈에서 채움 (String → List<OotdDto>)
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "ootds", columnDefinition = "jsonb")
  private String ootds;

  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private int commentCount;

  // TODO: 날씨·ootds 스냅샷은 연결 이슈에서 채움
  private Feed(UUID authorId, UUID weatherId, String content) {
    this.authorId = authorId;
    this.weatherId = weatherId;
    this.content = content;
    this.likeCount = 0;
    this.commentCount = 0;
  }

  // TODO: 연결 이슈에서 날씨/ootds 파라미터 추가
  public static Feed create(UUID authorId, UUID weatherId, String content) {
    return new Feed(authorId, weatherId, content);
  }
}