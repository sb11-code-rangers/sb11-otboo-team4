package com.sprint.mission.otboo.security.details;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.security.details.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class CustomUserDetailsTest {

  @Test
  @DisplayName("User 필드를 그대로 위임해서 노출한다")
  void constructor_exposesUserFieldsDirectly() {
    User user = User.create("홍길동", "hong@test.com", "encoded-password");

    CustomUserDetails principal = new CustomUserDetails(user);

    assertThat(principal.getUserId()).isEqualTo(user.getId());
    assertThat(principal.getEmail()).isEqualTo(user.getEmail());
    assertThat(principal.getName()).isEqualTo(user.getName());
    assertThat(principal.isLocked()).isFalse();
    assertThat(principal.getUsername()).isEqualTo(user.getEmail());
    assertThat(principal.getPassword()).isEqualTo(user.getPassword());
  }

  @Test
  @DisplayName("역할 이름 그대로의 GrantedAuthority 하나를 반환한다")
  void getAuthorities_returnsSingleAuthorityMatchingRoleName() {
    User admin = User.createAdmin("관리자", "admin@test.com", "encoded-password");

    CustomUserDetails principal = new CustomUserDetails(admin);

    assertThat(principal.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ADMIN");
  }

  @Test
  @DisplayName("잠긴 계정은 isAccountNonLocked가 false를 반환한다")
  void isAccountNonLocked_lockedUser_returnsFalse() {
    User user = User.create("홍길동", "hong@test.com", "encoded-password");
    user.lock(LockReason.ADMIN_ACTION);

    CustomUserDetails principal = new CustomUserDetails(user);

    assertThat(principal.isAccountNonLocked()).isFalse();
  }

  @Test
  @DisplayName("eraseCredentials를 호출하면 비밀번호가 null이 된다")
  void eraseCredentials_clearsPassword() {
    User user = User.create("홍길동", "hong@test.com", "encoded-password");
    CustomUserDetails principal = new CustomUserDetails(user);

    principal.eraseCredentials();

    assertThat(principal.getPassword()).isNull();
  }
}
