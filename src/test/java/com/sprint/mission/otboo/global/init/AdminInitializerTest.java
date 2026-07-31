package com.sprint.mission.otboo.global.init;

import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

  @Mock
  UserRepository mockUserRepository;

  @Mock
  ProfileRepository mockProfileRepository;

  @Mock
  PasswordEncoder mockPasswordEncoder;

  private AdminInitializer adminInitializer(AdminProperties properties) {
    return new AdminInitializer(
        mockUserRepository, mockProfileRepository, mockPasswordEncoder, properties);
  }

  private AdminProperties defaultAdminProperties() {
    return new AdminProperties("최고 관리자", "admin@test.com", "admin123!");
  }

  @Test
  @DisplayName("비밀번호가 설정되지 않았으면 아무 것도 하지 않고 건너뛴다")
  void run_blankPassword_skipsInitialization() throws Exception {
    // given
    AdminProperties properties = new AdminProperties("최고 관리자", "admin@test.com", "   ");
    AdminInitializer initializer = adminInitializer(properties);

    // when
    initializer.run(null);

    // then
    verify(mockUserRepository, never()).existsByEmail(any());
    verify(mockUserRepository, never()).saveAndFlush(any());
    verify(mockProfileRepository, never()).save(any());
  }

  @Test
  @DisplayName("이미 관리자 계정이 존재하면 새로 만들지 않는다")
  void run_adminAlreadyExists_doesNotCreateNewAccount() throws Exception {
    // given
    AdminProperties properties = defaultAdminProperties();
    AdminInitializer initializer = adminInitializer(properties);
    given(mockUserRepository.existsByEmail(properties.email())).willReturn(true);

    // when
    initializer.run(null);

    // then
    verify(mockUserRepository, never()).saveAndFlush(any());
    verify(mockProfileRepository, never()).save(any());
  }

  @Test
  @DisplayName("관리자 계정이 없으면 비밀번호를 암호화해서 User와 기본 Profile을 생성한다")
  void run_adminNotExists_createsAdminUserAndDefaultProfile() throws Exception {
    // given
    AdminProperties properties = defaultAdminProperties();
    AdminInitializer initializer = adminInitializer(properties);
    given(mockUserRepository.existsByEmail(properties.email())).willReturn(false);
    given(mockPasswordEncoder.encode("admin123!")).willReturn("encoded-admin-password");

    // when
    initializer.run(null);

    // then
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(mockUserRepository).saveAndFlush(userCaptor.capture());
    User savedAdmin = userCaptor.getValue();
    assertThat(savedAdmin.getName()).isEqualTo("최고 관리자");
    assertThat(savedAdmin.getEmail()).isEqualTo("admin@test.com");
    assertThat(savedAdmin.getPassword()).isEqualTo("encoded-admin-password");
    assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);

    ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
    verify(mockProfileRepository).save(profileCaptor.capture());
    assertThat(profileCaptor.getValue().getUser()).isEqualTo(savedAdmin);
  }

  @Test
  @DisplayName("existsByEmail 통과 후 동시 생성으로 유니크 제약 위반이 나면 예외를 전파하지 않고, Profile도 생성하지 않는다")
  void run_raceConditionOnSave_doesNotThrowAndDoesNotCreateProfile() throws Exception {
    // given
    AdminProperties properties = defaultAdminProperties();
    AdminInitializer initializer = adminInitializer(properties);
    given(mockUserRepository.existsByEmail(properties.email())).willReturn(false);
    given(mockPasswordEncoder.encode(any())).willReturn("encoded-admin-password");
    given(mockUserRepository.saveAndFlush(any(User.class)))
        .willThrow(
            new DataIntegrityViolationException("duplicate key value violates unique constraint"));

    // when & then
    assertThatCode(() -> initializer.run(null)).doesNotThrowAnyException();

    verify(mockProfileRepository, never()).save(any());
  }
}
