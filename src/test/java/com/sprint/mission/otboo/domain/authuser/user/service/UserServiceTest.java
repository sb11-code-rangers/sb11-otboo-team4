package com.sprint.mission.otboo.domain.authuser.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.domain.authuser.auth.exception.AccessDeniedException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ChangePasswordRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.LocationValidationDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ProfileUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.exception.DuplicateEmailException;
import com.sprint.mission.otboo.domain.authuser.user.exception.UserNotFoundException;
import com.sprint.mission.otboo.domain.authuser.user.mapper.UserMapper;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import com.sprint.mission.otboo.global.temppassword.registry.TempPasswordRegistry;
import com.sprint.mission.otboo.global.usersession.UserSessionRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  private static final FixtureMonkey entityFixtureMonkey = FixtureMonkey.builder()
      .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @InjectMocks
  UserService userService;
  @Mock
  UserRepository mockUserRepository;
  @Mock
  ProfileRepository mockProfileRepository;
  @Mock
  PasswordEncoder mockPasswordEncoder;
  @Mock
  UserSessionRegistry mockUserSessionRegistry;
  @Mock
  TempPasswordRegistry mockTempPasswordRegistry;
  @Spy
  UserMapper userMapper = new UserMapper();

  private User fixtureUser(UUID id, String name) {
    return entityFixtureMonkey.giveMeBuilder(User.class)
        .set("id", id)
        .set("name", name)
        .set("role", Role.USER)
        .sample();
  }

  @Nested
  @DisplayName("회원가입 성공")
  class SignUpSuccess {

    @Test
    @DisplayName("회원가입 성공 시 비밀번호를 암호화하고 User, Profile을 저장한 뒤 UserDto를 반환한다")
    void signUp_success() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(request.password())).willReturn("encoded-password");

      User savedUser = User.create(request.name(), request.email(), "encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class))).willReturn(savedUser);

      Profile savedProfile = Profile.createDefault(savedUser);
      given(mockProfileRepository.save(any(Profile.class))).willReturn(savedProfile);

      // when
      UserDto result = userService.signUp(request);

      // then
      assertThat(result.id()).isEqualTo(savedUser.getId());
      assertThat(result.createdAt()).isEqualTo(savedUser.getCreatedAt());
      assertThat(result.email()).isEqualTo(savedUser.getEmail());
      assertThat(result.name()).isEqualTo(savedUser.getName());
      assertThat(result.role()).isEqualTo(savedUser.getRole());
      assertThat(result.locked()).isEqualTo(savedUser.isLocked());
      verify(mockUserRepository).existsByEmail(request.email());
      verify(mockPasswordEncoder).encode(request.password());
      verify(mockUserRepository).saveAndFlush(any(User.class));
      verify(mockProfileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("회원가입 시 평문 비밀번호가 아닌 암호화된 비밀번호로 User를 생성한다")
    void signUp_encodesPasswordBeforeCreatingUser() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(request.password())).willReturn("encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class)))
          .willAnswer(invocation -> invocation.getArgument(0));
      given(mockProfileRepository.save(any(Profile.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      userService.signUp(request);

      // then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(mockUserRepository).saveAndFlush(userCaptor.capture());
      User capturedUser = userCaptor.getValue();

      assertThat(capturedUser.getPassword()).isEqualTo("encoded-password");
      assertThat(capturedUser.getPassword()).isNotEqualTo(request.password());
    }

    @Test
    @DisplayName("회원가입 시 기본 권한은 USER, 잠김 상태는 false로 생성된다")
    void signUp_createsUserWithDefaultRoleAndUnlocked() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(any())).willReturn("encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class)))
          .willAnswer(invocation -> invocation.getArgument(0));
      given(mockProfileRepository.save(any(Profile.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      userService.signUp(request);

      // then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(mockUserRepository).saveAndFlush(userCaptor.capture());
      User capturedUser = userCaptor.getValue();

      assertThat(capturedUser.getRole()).isEqualTo(Role.USER);
      assertThat(capturedUser.isLocked()).isFalse();
    }

    @Test
    @DisplayName("회원가입 시 저장된 User와 연결된 기본 Profile을 생성한다")
    void signUp_createsDefaultProfileLinkedToUser() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      User savedUser = User.create(request.name(), request.email(), "encoded-password");

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(any())).willReturn("encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class))).willReturn(savedUser);
      given(mockProfileRepository.save(any(Profile.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      userService.signUp(request);

      // then
      ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
      verify(mockProfileRepository).save(profileCaptor.capture());
      Profile capturedProfile = profileCaptor.getValue();

      assertThat(capturedProfile.getUser()).isEqualTo(savedUser);
    }

    @Test
    @DisplayName("회원가입은 existsByEmail을 정확히 1회 호출한다")
    void signUp_checksEmailDuplicationExactlyOnce() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(any())).willReturn("encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class)))
          .willAnswer(invocation -> invocation.getArgument(0));
      given(mockProfileRepository.save(any(Profile.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      userService.signUp(request);

      // then
      verify(mockUserRepository, times(1)).existsByEmail(request.email());
    }
  }

  @Nested
  @DisplayName("이메일 중복")
  class SignUpDuplicateEmail {

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입 시도 시 예외가 발생하고 저장은 일어나지 않는다")
    void signUp_duplicateEmail_throwsExceptionAndDoesNotSave() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.signUp(request))
          .isInstanceOf(DuplicateEmailException.class);

      verify(mockUserRepository, never()).saveAndFlush(any());
      verify(mockProfileRepository, never()).save(any());
      verify(mockPasswordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("existsByEmail 체크를 통과했지만 저장 시점에 동시성으로 인해 중복 제약이 걸리면 예외가 발생하고 Profile은 저장되지 않는다")
    void signUp_raceConditionOnSave_throwsExceptionAndDoesNotSaveProfile() {
      // given
      UserCreateRequest request = fixtureMonkey.giveMeBuilder(UserCreateRequest.class).sample();

      given(mockUserRepository.existsByEmail(request.email())).willReturn(false);
      given(mockPasswordEncoder.encode(any())).willReturn("encoded-password");
      given(mockUserRepository.saveAndFlush(any(User.class)))
          .willThrow(new DataIntegrityViolationException(
              "duplicate key value violates unique constraint"));

      // when & then
      assertThatThrownBy(() -> userService.signUp(request))
          .isInstanceOf(RuntimeException.class);

      verify(mockProfileRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("사용자 목록 조회")
  class GetUsers {

    @Test
    @DisplayName("조건을 그대로 리포지토리에 위임하고 결과를 그대로 반환한다")
    void getUsers_delegatesToRepositoryAndReturnsResult() {
      // given
      UserListParams condition = new UserListParams(
          null, null, 10, "email", SortDirection.ASCENDING, null, null, null);
      CursorPageResponse<UserDto> response = new CursorPageResponse<>(
          List.of(), null, null, false, 0L, "email", SortDirection.ASCENDING);
      given(mockUserRepository.search(condition)).willReturn(response);

      // when
      CursorPageResponse<UserDto> result = userService.getUsers(condition);

      // then
      assertThat(result).isEqualTo(response);
      verify(mockUserRepository).search(condition);
    }
  }

  @Nested
  @DisplayName("권한 수정")
  class ChangeRole {

    @Test
    @DisplayName("존재하는 사용자의 권한을 변경하고 세션을 강제로 폐기한다")
    void changeRole_existingUser_changesRoleAndRevokesSession() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      given(mockUserRepository.findById(user.getId())).willReturn(Optional.of(user));

      // when
      UserDto result = userService.changeRole(user.getId(), new UserRoleUpdateRequest(Role.ADMIN));

      // then
      assertThat(result.role()).isEqualTo(Role.ADMIN);
      assertThat(user.getRole()).isEqualTo(Role.ADMIN);
      verify(mockUserSessionRegistry).revoke(user.getId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 UserNotFoundException을 던지고 세션을 건드리지 않는다")
    void changeRole_userNotFound_throwsExceptionAndDoesNotRevokeSession() {
      // given
      UUID userId = UUID.randomUUID();
      given(mockUserRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(
          () -> userService.changeRole(userId, new UserRoleUpdateRequest(Role.ADMIN)))
          .isInstanceOf(UserNotFoundException.class);

      verify(mockUserSessionRegistry, never()).revoke(any());
    }
  }

  @Nested
  @DisplayName("프로필 조회")
  class GetProfile {

    @Test
    @DisplayName("본인이 조회하면 ProfileDto를 반환한다")
    void getProfile_self_returnsProfileDto() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      Profile profile = Profile.createDefault(user);
      ReflectionTestUtils.setField(profile, "id", user.getId());
      given(mockProfileRepository.findByIdWithUser(user.getId())).willReturn(Optional.of(profile));

      // when
      ProfileDto result = userService.getProfile(user.getId(), user.getId());

      // then
      assertThat(result.userId()).isEqualTo(user.getId());
      assertThat(result.name()).isEqualTo("홍길동");
      assertThat(result.temperatureSensitivity()).isEqualTo(3);
    }

    @Test
    @DisplayName("본인이 아니면 AccessDeniedException을 던지고 조회하지 않는다")
    void getProfile_notSelf_throwsAccessDeniedExceptionAndDoesNotQuery() {
      // given
      UUID userId = UUID.randomUUID();
      UUID otherUserId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> userService.getProfile(userId, otherUserId))
          .isInstanceOf(AccessDeniedException.class);

      verify(mockProfileRepository, never()).findByIdWithUser(any());
    }

    @Test
    @DisplayName("프로필이 없으면 UserNotFoundException을 던진다")
    void getProfile_notFound_throwsUserNotFoundException() {
      // given
      UUID userId = UUID.randomUUID();
      given(mockProfileRepository.findByIdWithUser(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.getProfile(userId, userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("프로필 수정")
  class ChangeProfile {

    @Test
    @DisplayName("본인이 수정하면 프로필 필드를 변경한 뒤 변경된 ProfileDto를 반환한다")
    void changeProfile_self_updatesFieldsAndReturnsProfileDto() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      Profile profile = Profile.createDefault(user);
      ReflectionTestUtils.setField(profile, "id", user.getId());
      given(mockProfileRepository.findByIdWithUser(user.getId())).willReturn(Optional.of(profile));

      LocationValidationDto locationRequest =
          new LocationValidationDto(37.5, 127.0, 60, 127, List.of("서울특별시"));
      ProfileUpdateRequest request = new ProfileUpdateRequest(
          "김철수", Gender.MALE, LocalDate.of(1995, 1, 1), locationRequest, 4);

      // when
      ProfileDto result = userService.changeProfile(user.getId(), user.getId(), request, null);

      // then
      assertThat(result.name()).isEqualTo("김철수");
      assertThat(result.gender()).isEqualTo(Gender.MALE);
      assertThat(result.temperatureSensitivity()).isEqualTo(4);
      assertThat(user.getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("본인이 아니면 AccessDeniedException을 던지고 수정하지 않는다")
    void changeProfile_notSelf_throwsAccessDeniedExceptionAndDoesNotUpdate() {
      // given
      UUID userId = UUID.randomUUID();
      UUID otherUserId = UUID.randomUUID();
      ProfileUpdateRequest request =
          new ProfileUpdateRequest("김철수", Gender.MALE, LocalDate.of(1995, 1, 1), null, 3);

      // when & then
      assertThatThrownBy(() -> userService.changeProfile(userId, otherUserId, request, null))
          .isInstanceOf(AccessDeniedException.class);

      verify(mockProfileRepository, never()).findByIdWithUser(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 UserNotFoundException을 던진다")
    void changeProfile_notFound_throwsUserNotFoundException() {
      // given
      UUID userId = UUID.randomUUID();
      given(mockProfileRepository.findByIdWithUser(userId)).willReturn(Optional.empty());
      ProfileUpdateRequest request =
          new ProfileUpdateRequest("김철수", Gender.MALE, LocalDate.of(1995, 1, 1), null, 3);

      // when & then
      assertThatThrownBy(() -> userService.changeProfile(userId, userId, request, null))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("비밀번호 변경")
  class ChangePassword {

    @Test
    @DisplayName("본인이 변경하면 비밀번호를 암호화해서 저장하고, 세션과 임시 비밀번호를 모두 폐기한다")
    void changePassword_self_encodesPasswordAndRevokesSessionAndTempPassword() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      given(mockUserRepository.findById(user.getId())).willReturn(Optional.of(user));
      given(mockPasswordEncoder.encode("newpass1")).willReturn("new-encoded-password");

      // when
      userService.changePassword(user.getId(), user.getId(), new ChangePasswordRequest("newpass1"));

      // then
      assertThat(user.getPassword()).isEqualTo("new-encoded-password");
      verify(mockUserSessionRegistry).revoke(user.getId());
      verify(mockTempPasswordRegistry).revoke(user.getId());
    }

    @Test
    @DisplayName("본인이 아니면 AccessDeniedException을 던지고 아무 것도 변경하지 않는다")
    void changePassword_notSelf_throwsAccessDeniedExceptionAndChangesNothing() {
      // given
      UUID userId = UUID.randomUUID();
      UUID otherUserId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> userService.changePassword(
          userId, otherUserId, new ChangePasswordRequest("newpass1")))
          .isInstanceOf(AccessDeniedException.class);

      verify(mockUserRepository, never()).findById(any());
      verify(mockUserSessionRegistry, never()).revoke(any());
      verify(mockTempPasswordRegistry, never()).revoke(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 UserNotFoundException을 던지고 아무 것도 폐기하지 않는다")
    void changePassword_userNotFound_throwsExceptionAndRevokesNothing() {
      // given
      UUID userId = UUID.randomUUID();
      given(mockUserRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.changePassword(
          userId, userId, new ChangePasswordRequest("newpass1")))
          .isInstanceOf(UserNotFoundException.class);

      verify(mockUserSessionRegistry, never()).revoke(any());
      verify(mockTempPasswordRegistry, never()).revoke(any());
    }
  }


  @Nested
  @DisplayName("계정 잠금 상태 변경")
  class ChangeLocked {

    @Test
    @DisplayName("locked=true면 계정을 ADMIN_ACTION 사유로 잠그고 세션을 폐기한다")
    void changeLocked_true_locksAccountWithAdminActionAndRevokesSession() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      given(mockUserRepository.findById(user.getId())).willReturn(Optional.of(user));

      // when
      UserDto result = userService.changeLocked(user.getId(), new UserLockUpdateRequest(true));

      // then
      assertThat(result.locked()).isTrue();
      assertThat(user.isLocked()).isTrue();
      verify(mockUserSessionRegistry).revoke(user.getId());
    }

    @Test
    @DisplayName("locked=false면 계정 잠금을 해제한다")
    void changeLocked_false_unlocksAccount() {
      // given
      User user = fixtureUser(UUID.randomUUID(), "홍길동");
      user.lock(LockReason.ADMIN_ACTION);
      given(mockUserRepository.findById(user.getId())).willReturn(Optional.of(user));

      // when
      UserDto result = userService.changeLocked(user.getId(), new UserLockUpdateRequest(false));

      // then
      assertThat(result.locked()).isFalse();
      assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 UserNotFoundException을 던진다")
    void changeLocked_userNotFound_throwsUserNotFoundException() {
      // given
      UUID userId = UUID.randomUUID();
      given(mockUserRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(
          () -> userService.changeLocked(userId, new UserLockUpdateRequest(true)))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}
