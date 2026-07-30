package com.sprint.mission.otboo.domain.authuser.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  private void setCreatedAt(UUID userId, Instant createdAt) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update users set created_at = :createdAt where id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", userId)
        .executeUpdate();
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("User를 저장하면 ID가 생성되고 저장된 값을 조회할 수 있다")
    void save_and_findById() {
      User user = User.create("홍길동", "hong@test.com", "encoded-password");

      User savedUser = userRepository.save(user);
      testEntityManager.flush();
      testEntityManager.clear();

      Optional<User> found = userRepository.findById(savedUser.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("hong@test.com");
      assertThat(found.get().getName()).isEqualTo("홍길동");
      assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 저장하면 무결성 제약 예외가 발생한다")
    void save_duplicateEmail_throwsException() {
      User user1 = User.create("홍길동", "duplicate@test.com", "encoded-password-1");
      userRepository.save(user1);
      testEntityManager.flush();

      User user2 = User.create("김철수", "duplicate@test.com", "encoded-password-2");

      assertThatThrownBy(() -> {
        userRepository.saveAndFlush(user2);
      }).isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  @DisplayName("existsByEmail")
  class ExistsByEmail {

    @Test
    @DisplayName("존재하는 이메일에 대해 existsByEmail은 true를 반환한다")
    void existsByEmail_existingEmail_returnsTrue() {
      User user = User.create("홍길동", "hong@test.com", "encoded-password");
      userRepository.save(user);
      testEntityManager.flush();

      boolean exists = userRepository.existsByEmail("hong@test.com");

      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일에 대해 existsByEmail은 false를 반환한다")
    void existsByEmail_nonExistingEmail_returnsFalse() {
      boolean exists = userRepository.existsByEmail("notfound@test.com");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
    void findById_nonExistingId_returnsEmpty() {
      Optional<User> found = userRepository.findById(UUID.randomUUID());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("search")
  class Search {

    @Test
    @DisplayName("커서 없이 호출하면 limit개까지만 반환하고, 남은 데이터가 있으면 hasNext는 true다")
    void search_noCursor_returnsLimitedResultsWithHasNextAndTotalCount() {
      // given
      for (int i = 0; i < 3; i++) {
        userRepository.save(User.create("사용자" + i, "user" + i + "@test.com", "encoded-password"));
      }
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 2, "email", SortDirection.ASCENDING, null, null, null);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      assertThat(result.data()).hasSize(2);
      assertThat(result.hasNext()).isTrue();
      assertThat(result.totalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("email 오름차순 정렬에서 커서로 다음 페이지를 조회하면 나머지가 중복·누락 없이 조회된다")
    void search_emailAscending_cursorReturnsRemainingUsersWithoutDuplicateOrGap() {
      // given
      userRepository.save(User.create("가", "a@test.com", "encoded-password"));
      userRepository.save(User.create("나", "b@test.com", "encoded-password"));
      userRepository.save(User.create("다", "c@test.com", "encoded-password"));
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams firstPage = new UserListParams(
          null, null, 1, "email", SortDirection.ASCENDING, null, null, null);

      // when
      CursorPageResponse<UserDto> first = userRepository.search(firstPage);

      // then
      assertThat(first.data()).extracting(UserDto::email).containsExactly("a@test.com");
      assertThat(first.hasNext()).isTrue();
      assertThat(first.nextCursor()).isEqualTo("a@test.com");

      // when
      UserListParams secondPage = new UserListParams(
          first.nextCursor(), first.nextIdAfter(), 2, "email", SortDirection.ASCENDING, null, null,
          null);
      CursorPageResponse<UserDto> second = userRepository.search(secondPage);

      // then
      assertThat(second.data()).extracting(UserDto::email)
          .containsExactly("b@test.com", "c@test.com");
      assertThat(second.hasNext()).isFalse();
      assertThat(second.nextCursor()).isNull();
      assertThat(second.nextIdAfter()).isNull();
    }

    @Test
    @DisplayName("emailLike가 주어지면 이메일에 해당 문자열을 포함한 사용자만 조회한다")
    void search_emailLikeFilter_returnsOnlyMatchingUsers() {
      // given
      userRepository.save(User.create("홍길동", "hong@test.com", "encoded-password"));
      userRepository.save(User.create("김철수", "kim@test.com", "encoded-password"));
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 10, "email", SortDirection.ASCENDING, "hong", null, null);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      assertThat(result.data()).extracting(UserDto::email).containsExactly("hong@test.com");
    }

    @Test
    @DisplayName("roleEqual이 주어지면 해당 권한을 가진 사용자만 조회한다")
    void search_roleEqualFilter_returnsOnlyMatchingRole() {
      // given
      userRepository.save(User.create("일반유저", "user@test.com", "encoded-password"));
      userRepository.save(User.createAdmin("관리자", "admin@test.com", "encoded-password"));
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 10, "email", SortDirection.ASCENDING, null, Role.ADMIN, null);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      assertThat(result.data()).extracting(UserDto::email).containsExactly("admin@test.com");
    }

    @Test
    @DisplayName("locked가 주어지면 잠금 상태가 일치하는 사용자만 조회한다")
    void search_lockedFilter_returnsOnlyLockedUsers() {
      // given
      userRepository.save(User.create("정상유저", "active@test.com", "encoded-password"));
      User lockedUser = User.create("잠긴유저", "locked@test.com", "encoded-password");
      lockedUser.lock(LockReason.ADMIN_ACTION);
      userRepository.save(lockedUser);
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 10, "email", SortDirection.ASCENDING, null, null, true);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      assertThat(result.data()).extracting(UserDto::email).containsExactly("locked@test.com");
      assertThat(result.data()).extracting(UserDto::locked).containsExactly(true);
    }

    @Test
    @DisplayName("sortDirection이 DESCENDING이면 이메일 내림차순으로 조회한다")
    void search_emailDescending_ordersEmailDescending() {
      // given
      userRepository.save(User.create("가", "a@test.com", "encoded-password"));
      userRepository.save(User.create("나", "b@test.com", "encoded-password"));
      testEntityManager.flush();
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 10, "email", SortDirection.DESCENDING, null, null, null);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      assertThat(result.data()).extracting(UserDto::email)
          .containsExactly("b@test.com", "a@test.com");
    }

    @Test
    @DisplayName("createdAt이 같으면 id 오름차순으로 tie-break하여 조회한다")
    void search_sameCreatedAt_tieBreaksByIdAscending() {
      // given
      Instant sameTime = Instant.parse("2026-07-28T00:00:00Z");
      User a = userRepository.save(User.create("A", "a@test.com", "encoded-password"));
      User b = userRepository.save(User.create("B", "b@test.com", "encoded-password"));
      testEntityManager.flush();

      setCreatedAt(a.getId(), sameTime);
      setCreatedAt(b.getId(), sameTime);
      testEntityManager.clear();

      UserListParams condition = new UserListParams(
          null, null, 10, "createdAt", SortDirection.ASCENDING, null, null, null);

      // when
      CursorPageResponse<UserDto> result = userRepository.search(condition);

      // then
      List<UUID> ids = result.data().stream().map(UserDto::id).toList();
      assertThat(ids).containsExactlyInAnyOrder(a.getId(), b.getId());
      assertThat(ids.get(0).toString()).isLessThan(ids.get(1).toString());
    }

    @Test
    @DisplayName("createdAt 정렬에서 cursor가 Instant로 파싱할 수 없으면 예외가 전파된다 (정상 흐름에서는 @Valid가 먼저 막아줌)")
    void search_invalidCursorForCreatedAtSort_propagatesDateTimeParseException() {
      // given
      UserListParams condition = new UserListParams(
          "not-an-instant", UUID.randomUUID(), 10, "createdAt", SortDirection.ASCENDING, null, null,
          null);

      // when & then
      assertThatThrownBy(() -> userRepository.search(condition))
          .isInstanceOf(DateTimeParseException.class);
    }
  }
}