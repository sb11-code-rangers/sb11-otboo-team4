package com.sprint.mission.otboo.domain.authuser.user.mapper;

import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserMapperTest {

    private final AuthUserMapper authUserMapper = new AuthUserMapper();

    @Test
    @DisplayName("User 엔티티를 UserDto로 정확히 변환한다")
    void userDtoFromUser_mapsAllFieldsCorrectly() {
        // given
        User user = User.createUser("홍길동", "hong@test.com", "encoded-password");

        // when
        UserDto dto = authUserMapper.userDtoFromUser(user);

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
    void userDtoFromUser_mapsAdminRole() {
        // given
        User admin = User.createAdmin("관리자", "admin@test.com", "encoded-password");

        // when
        UserDto dto = authUserMapper.userDtoFromUser(admin);

        // then
        assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }
}
