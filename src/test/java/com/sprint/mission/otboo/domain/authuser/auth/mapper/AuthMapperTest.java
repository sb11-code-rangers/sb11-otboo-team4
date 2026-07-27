package com.sprint.mission.otboo.domain.authuser.auth.mapper;

import com.sprint.mission.otboo.domain.authuser.auth.dto.response.JwtDto;
import com.sprint.mission.otboo.domain.authuser.auth.dto.response.SignInDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMapperTest {

    @Nested
    @DisplayName("signInDtoFrom")
    class SignInDtoFrom {

        @Test
        @DisplayName("User와 토큰들을 SignInDto로 정확히 변환한다")
        void signInDtoFrom_mapsUserAndTokensCorrectly() {
            // given
            User user = User.create("홍길동", "hong@test.com", "encoded-password");

            // when
            SignInDto result = AuthMapper.signInDtoFrom(user, "access-token", "refresh-token");

            // then
            assertThat(result.jwtDto().accessToken()).isEqualTo("access-token");
            assertThat(result.jwtDto().userDto().email()).isEqualTo(user.getEmail());
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
        }
    }

    @Nested
    @DisplayName("jwtDtoFrom")
    class JwtDtoFrom {

        @Test
        @DisplayName("User와 accessToken을 JwtDto로 정확히 변환한다")
        void jwtDtoFrom_mapsUserAndAccessTokenCorrectly() {
            // given
            User user = User.create("홍길동", "hong@test.com", "encoded-password");

            // when
            JwtDto result = AuthMapper.jwtDtoFrom(user, "access-token");

            // then
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.userDto().email()).isEqualTo(user.getEmail());
        }
    }
}
