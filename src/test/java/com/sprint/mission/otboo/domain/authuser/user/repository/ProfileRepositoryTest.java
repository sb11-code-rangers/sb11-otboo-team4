package com.sprint.mission.otboo.domain.authuser.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
class ProfileRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProfileRepository profileRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("Profile을 저장하면 User와 동일한 ID를 공유한다 (@MapsId)")
    void save_profileSharesIdWithUser() {
      // given
      User user = userRepository.save(User.create("홍길동", "hong@test.com", "encoded-password"));
      testEntityManager.flush();

      Profile profile = Profile.createDefault(user);

      // when
      Profile savedProfile = profileRepository.save(profile);
      testEntityManager.flush();
      testEntityManager.clear();

      // then
      assertThat(savedProfile.getId()).isEqualTo(user.getId());

      Optional<Profile> found = profileRepository.findById(user.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getTemperatureSensitivity()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("createDefaultProfile")
  class CreateDefaultProfile {

    @Test
    @DisplayName("Profile은 기본 온도 민감도 3으로 생성된다")
    void createDefaultProfile_hasDefaultTemperatureSensitivity() {
      // given
      User user = userRepository.save(User.create("홍길동", "hong2@test.com", "encoded-password"));
      testEntityManager.flush();

      // when
      Profile profile = Profile.createDefault(user);

      // then
      assertThat(profile.getTemperatureSensitivity()).isEqualTo(3);
      assertThat(profile.getGender()).isNull();
      assertThat(profile.getLocation()).isNull();
    }
  }

  @Nested
  @DisplayName("findByIdWithUser")
  class FindByIdWithUser {

    @Test
    @DisplayName("존재하는 userId로 조회하면 User가 함께 로딩된 Profile을 반환한다")
    void findByIdWithUser_existingId_returnsProfileWithUser() {
      // given
      User user = userRepository.save(User.create("홍길동", "hong3@test.com", "encoded-password"));
      profileRepository.save(Profile.createDefault(user));
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      Optional<Profile> found = profileRepository.findByIdWithUser(user.getId());

      // then
      assertThat(found).isPresent();
      assertThat(found.get().getUser().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회하면 빈 Optional을 반환한다")
    void findByIdWithUser_nonExistingId_returnsEmpty() {
      Optional<Profile> found = profileRepository.findByIdWithUser(UUID.randomUUID());

      assertThat(found).isEmpty();
    }
  }
}