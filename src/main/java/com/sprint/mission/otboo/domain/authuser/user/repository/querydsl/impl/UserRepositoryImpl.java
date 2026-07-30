package com.sprint.mission.otboo.domain.authuser.user.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.authuser.user.entity.QUser.user;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserSearchCondition;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.exception.InvalidCursorException;
import com.sprint.mission.otboo.domain.authuser.user.repository.querydsl.UserRepositoryCustom;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<UserDto> search(UserSearchCondition condition) {
    List<UserDto> content = queryFactory
        .select(
            Projections.constructor(UserDto.class,
                user.id,
                user.createdAt,
                user.email,
                user.name,
                user.role,
                user.locked
            )
        )
        .from(user)
        .where(
            emailLikeCondition(condition.emailLike()),
            roleEqualCondition(condition.roleEqual()),
            lockedCondition(condition.locked()),
            cursorCondition(condition)
        )
        .orderBy(orderSpecifiers(condition))
        .limit(condition.limit() + 1)
        .fetch();

    boolean hasNext = content.size() > condition.limit();
    if (hasNext) {
      content = content.subList(0, condition.limit());
    }

    UserDto last = content.isEmpty() ? null : content.get(content.size() - 1);
    String nextCursor = last == null ? null
        : ("email".equals(condition.sortBy()) ? last.email() : last.createdAt().toString());
    UUID nextIdAfter = last == null ? null : last.id();

    // TODO: 매 페이지 요청 카운트 쿼리 최적화 (성능 측정 Phase에서 진행)
    Long totalCount = Optional.ofNullable(
        queryFactory
            .select(user.count())
            .from(user)
            .where(
                emailLikeCondition(condition.emailLike()),
                roleEqualCondition(condition.roleEqual()),
                lockedCondition(condition.locked())
            )
            .fetchOne()
    ).orElse(0L);

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        condition.sortBy(),
        condition.sortDirection()
    );
  }

  private BooleanExpression emailLikeCondition(String emailLike) {
    return StringUtils.hasText(emailLike) ? user.email.containsIgnoreCase(emailLike) : null;
  }

  private BooleanExpression roleEqualCondition(Role roleEqual) {
    return roleEqual != null ? user.role.eq(roleEqual) : null;
  }

  private BooleanExpression lockedCondition(Boolean locked) {
    return locked != null ? user.locked.eq(locked) : null;
  }

  private BooleanExpression cursorCondition(UserSearchCondition condition) {

    if (!StringUtils.hasText(condition.cursor()) || condition.idAfter() == null) {
      // 첫 페이지 요청이라고 생각
      return null;
    }

    boolean ascending = condition.sortDirection() == SortDirection.ASCENDING;

    if ("email".equals(condition.sortBy())) {

      if (ascending) {
        // 오름차순
        return user.email.gt(condition.cursor())
            .or(user.email.eq(condition.cursor()).and(user.id.gt(condition.idAfter())));
      } else {
        // 내림차순
        return user.email.lt(condition.cursor())
            .or(user.email.eq(condition.cursor()).and(user.id.lt(condition.idAfter())));
      }

    } else {

      Instant cursorInstant;
      try {
        cursorInstant = Instant.parse(condition.cursor());
      } catch (DateTimeParseException e) {
        throw InvalidCursorException.withCursor(condition.cursor());
      }

      if (ascending) {
        // 오름차순
        return user.createdAt.gt(cursorInstant)
            .or(user.createdAt.eq(cursorInstant).and(user.id.gt(condition.idAfter())));
      } else {
        return user.createdAt.lt(cursorInstant)
            .or(user.createdAt.eq(cursorInstant).and(user.id.lt(condition.idAfter())));
      }
    }
  }

  private OrderSpecifier<?>[] orderSpecifiers(UserSearchCondition condition) {

    String sortBy = condition.sortBy();
    boolean ascending = condition.sortDirection() == SortDirection.ASCENDING;

    OrderSpecifier<?> primary = "email".equals(sortBy)
        ? (ascending ? user.email.asc() : user.email.desc())
        : (ascending ? user.createdAt.asc() : user.createdAt.desc());

    OrderSpecifier<?> tieBreaker = ascending ? user.id.asc() : user.id.desc();

    return new OrderSpecifier<?>[]{primary, tieBreaker};
  }
}
