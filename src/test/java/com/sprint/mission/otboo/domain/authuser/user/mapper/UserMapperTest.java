package com.sprint.mission.otboo.domain.authuser.user.mapper;

import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import com.sprint.mission.otboo.global.security.details.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.LocationValidationDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.LocationDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Location;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

  private UserMapper userMapper;

  @BeforeEach
  void setUp() {
    userMapper = new UserMapper();
  }

  @Nested
  @DisplayName("userDtoFrom(User)")
  class UserDtoFromUser {

    @Test
    @DisplayName("User 엔티티를 UserDto로 정확히 변환한다")
    void userDtoFrom_user_mapsAllFieldsCorrectly() {
      // given
      User user = User.create("홍길동", "hong@test.com", "encoded-password");

      // when
      UserDto dto = userMapper.userDtoFrom(user);

      // then
      assertThat(dto.id()).isEqualTo(user.getId());
      assertThat(dto.email()).isEqualTo(user.getEmail());
      assertThat(dto.name()).isEqualTo(user.getName());
      assertThat(dto.role()).isEqualTo(Role.USER);
      assertThat(dto.locked()).isFalse();
      assertThat(dto.createdAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("ADMIN 권한 User도 role 필드에 정확히 매핑된다")
    void userDtoFrom_user_mapsAdminRole() {
      // given
      User admin = User.createAdmin("관리자", "admin@test.com", "encoded-password");

      // when
      UserDto dto = userMapper.userDtoFrom(admin);

      // then
      assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }
  }

  @Nested
  @DisplayName("userDtoFrom(CustomUserDetails)")
  class UserDtoFromCustomUserDetails {

    @Test
    @DisplayName("CustomUserDetails를 UserDto로 정확히 변환한다")
    void userDtoFrom_customUserDetails_mapsAllFieldsCorrectly() {
      // given
      User user = User.create("홍길동", "hong@test.com", "encoded-password");
      CustomUserDetails principal = new CustomUserDetails(user);

      // when
      UserDto dto = userMapper.userDtoFrom(principal);

      // then
      assertThat(dto.id()).isEqualTo(user.getId());
      assertThat(dto.email()).isEqualTo(user.getEmail());
      assertThat(dto.name()).isEqualTo(user.getName());
      assertThat(dto.role()).isEqualTo(Role.USER);
      assertThat(dto.locked()).isFalse();
      assertThat(dto.createdAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("잠긴 계정의 CustomUserDetails는 locked=true로 매핑된다")
    void userDtoFrom_customUserDetails_mapsLockedState() {
      // given
      User user = User.create("홍길동", "hong@test.com", "encoded-password");
      user.lock(LockReason.ADMIN_ACTION);
      CustomUserDetails principal = new CustomUserDetails(user);

      // when
      UserDto dto = userMapper.userDtoFrom(principal);

      // then
      assertThat(dto.locked()).isTrue();
    }
  }

  @Nested
  @DisplayName("profileDtoFrom(Profile)")
  class ProfileDtoFromProfile {

    @Test
    @DisplayName("위치 정보가 있는 Profile을 ProfileDto로 정확히 변환한다")
    void profileDtoFrom_withLocation_mapsAllFieldsCorrectly() {
      // given
      User user = User.create("홍길동", "hong@test.com", "encoded-password");
      UUID userId = UUID.randomUUID();
      ReflectionTestUtils.setField(user, "id", userId);
      Profile profile = Profile.createDefault(user);
      ReflectionTestUtils.setField(profile, "id", userId);

      Location location = Location.create(37.5, 127.0, 60, 127, List.of("서울특별시"));
      profile.changeFields("홍길동", Gender.MALE, LocalDate.of(1995, 1, 1), location, 4);

      // when
      ProfileDto dto = userMapper.profileDtoFrom(profile);

      // then
      assertThat(dto.userId()).isEqualTo(userId);
      assertThat(dto.name()).isEqualTo("홍길동");
      assertThat(dto.gender()).isEqualTo(Gender.MALE);
      assertThat(dto.birthDate()).isEqualTo(LocalDate.of(1995, 1, 1));
      assertThat(dto.temperatureSensitivity()).isEqualTo(4);
      assertThat(dto.location()).isNotNull();
      assertThat(dto.location().x()).isEqualTo(60);
      assertThat(dto.location().y()).isEqualTo(127);
    }

    @Test
    @DisplayName("위치 정보가 없는 Profile은 location이 null인 ProfileDto로 변환된다")
    void profileDtoFrom_withoutLocation_mapsNullLocation() {
      // given
      User user = User.create("홍길동", "hong@test.com", "encoded-password");
      UUID userId = UUID.randomUUID();
      ReflectionTestUtils.setField(user, "id", userId);
      Profile profile = Profile.createDefault(user);
      ReflectionTestUtils.setField(profile, "id", userId);

      // when
      ProfileDto dto = userMapper.profileDtoFrom(profile);

      // then
      assertThat(dto.location()).isNull();
      assertThat(dto.temperatureSensitivity()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("locationDtoFrom(Location)")
  class LocationDtoFromLocation {

    @Test
    @DisplayName("Location 엔티티를 LocationDto로 정확히 변환한다 (x/y 필드명 유지)")
    void locationDtoFrom_mapsAllFieldsCorrectly() {
      // given
      Location location = Location.create(37.5, 127.0, 60, 127, List.of("서울특별시", "강남구"));

      // when
      LocationDto dto = userMapper.locationDtoFrom(location);

      // then
      assertThat(dto.latitude()).isEqualTo(37.5);
      assertThat(dto.longitude()).isEqualTo(127.0);
      assertThat(dto.x()).isEqualTo(60);
      assertThat(dto.y()).isEqualTo(127);
      assertThat(dto.locationNames()).containsExactly("서울특별시", "강남구");
    }
  }

  @Nested
  @DisplayName("locationFrom(LocationValidationDto)")
  class LocationFromLocationValidationDto {

    @Test
    @DisplayName("LocationValidationDto를 Location 엔티티로 정확히 변환한다")
    void locationFrom_validDto_mapsAllFieldsCorrectly() {
      // given
      LocationValidationDto request =
          new LocationValidationDto(37.5, 127.0, 60, 127, List.of("서울특별시"));

      // when
      Location location = userMapper.locationFrom(request);

      // then
      assertThat(location.getLatitude()).isEqualTo(37.5);
      assertThat(location.getLongitude()).isEqualTo(127.0);
      assertThat(location.getLocationX()).isEqualTo(60);
      assertThat(location.getLocationY()).isEqualTo(127);
      assertThat(location.getLocationNames()).containsExactly("서울특별시");
    }

    @Test
    @DisplayName("dto가 null이면 null을 반환한다")
    void locationFrom_nullDto_returnsNull() {
      // when
      Location location = userMapper.locationFrom(null);

      // then
      assertThat(location).isNull();
    }
  }

}
