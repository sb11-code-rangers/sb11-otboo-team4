package com.sprint.mission.otboo.domain.social.follow.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.authuser.user.entity.QUser.user;
import static com.sprint.mission.otboo.domain.social.follow.entity.QFollow.follow;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowerListParams;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.repository.querydsl.FollowCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<Follow> findFollowings(FollowingListParams params) {
    return findFollows(params.followerId(), params.cursor(), params.idAfter(),
        params.limit(), params.nameLike(), Direction.FOLLOWINGS);
  }

  @Override
  public CursorPageResponse<Follow> findFollowers(FollowerListParams params) {
    return findFollows(params.followeeId(), params.cursor(), params.idAfter(),
        params.limit(), params.nameLike(), Direction.FOLLOWERS);
  }

  private CursorPageResponse<Follow> findFollows(
      UUID baseId, String cursor, UUID idAfter, int limit, String nameLike, Direction direction) {
    List<Follow> raw = fetchFollows(baseId, cursor, idAfter, limit, nameLike, direction);

    boolean hasNext = raw.size() > limit;
    List<Follow> page = hasNext ? raw.subList(0, limit) : raw;

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !page.isEmpty()) {
      Follow last = page.get(page.size() - 1);
      nextCursor = last.getCreatedAt().toString();
      nextIdAfter = last.getId();
    }

    return new CursorPageResponse<>(page, nextCursor, nextIdAfter, hasNext,
        countFollows(baseId, nameLike, direction), "createdAt", SortDirection.DESCENDING
    );
  }

  private List<Follow> fetchFollows(
      UUID baseId, String cursor, UUID idAfter, int limit, String nameLike, Direction direction) {
    JPAQuery<Follow> query = queryFactory.selectFrom(follow);

    if (StringUtils.hasText(nameLike)) {
      query.join(user).on(direction.joinCondition());
    }

    return query
        .where(
            direction.baseCondition(baseId),
            userNameContains(nameLike),
            cursorCondition(cursor, idAfter)
        )
        .orderBy(follow.createdAt.desc(), follow.id.desc())
        .limit(limit + 1L)
        .fetch();
  }

  private long countFollows(UUID baseId, String nameLike, Direction direction) {
    JPAQuery<Long> query = queryFactory.select(follow.count()).from(follow);

    if (StringUtils.hasText(nameLike)) {
      query.join(user).on(direction.joinCondition());
    }

    return Optional.ofNullable(
        query.where(
            direction.baseCondition(baseId),
            userNameContains(nameLike)
        ).fetchOne()
    ).orElse(0L);
  }

  private BooleanExpression userNameContains(String name) {
    return StringUtils.hasText(name) ? user.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }
    Instant instant = Instant.parse(cursor);
    return follow.createdAt.lt(instant)
        .or(follow.createdAt.eq(instant).and(follow.id.lt(idAfter)));
  }

  private enum Direction {
    FOLLOWINGS {
      @Override
      BooleanExpression baseCondition(UUID id) {
        return follow.followerId.eq(id);
      }

      @Override
      BooleanExpression joinCondition() {
        return user.id.eq(follow.followeeId);
      }
    },
    FOLLOWERS {
      @Override
      BooleanExpression baseCondition(UUID id) {
        return follow.followeeId.eq(id);
      }

      @Override
      BooleanExpression joinCondition() {
        return user.id.eq(follow.followerId);
      }
    };

    abstract BooleanExpression baseCondition(UUID id);

    abstract BooleanExpression joinCondition();
  }
}