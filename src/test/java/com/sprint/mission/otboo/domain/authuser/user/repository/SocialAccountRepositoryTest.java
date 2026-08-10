package com.sprint.mission.otboo.domain.authuser.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.otboo.domain.authuser.user.entity.SocialAccount;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.OAuth2Provider;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import java.util.Optional;
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

// application-test.yaml의 datasource가 testcontainers jdbc url(jdbc:tc:postgresql:...)이라
// 별도 @Container 선언 없이도 실제 Postgres 컨테이너에 대해 UNIQUE(provider, provider_id) 제약까지 검증된다.
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
@DisplayName("SocialAccountRepository")
class SocialAccountRepositoryTest {

  @Autowired
  private SocialAccountRepository socialAccountRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  private User persistUser(String email) {
    return userRepository.save(User.create("홍길동", email, "encoded-password"));
  }

  @Nested
  @DisplayName("provider와 providerId로 조회 (findByProviderAndProviderId)")
  class FindByProviderAndProviderId {

    @Test
    @DisplayName("연동된 계정이 있으면 SocialAccount를 반환한다")
    void 연동된_계정이_있으면_SocialAccount를_반환한다() {
      // given
      User user = persistUser("hong@test.com");
      socialAccountRepository.save(
          SocialAccount.link(user, OAuth2Provider.GOOGLE, "google-sub-1", "hong@gmail.com"));
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      Optional<SocialAccount> found = socialAccountRepository
          .findByProviderAndProviderId(OAuth2Provider.GOOGLE, "google-sub-1");

      // then
      assertThat(found).isPresent();
      assertThat(found.get().getProviderEmail()).isEqualTo("hong@gmail.com");
      assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("연동된 계정이 없으면 빈 Optional을 반환한다")
    void 연동된_계정이_없으면_빈_Optional을_반환한다() {
      // when & then
      assertThat(socialAccountRepository
          .findByProviderAndProviderId(OAuth2Provider.KAKAO, "no-such-id"))
          .isEmpty();
    }

    @Test
    @DisplayName("같은 providerId여도 provider가 다르면 다른 계정으로 취급한다")
    void 같은_providerId여도_provider가_다르면_다른_계정으로_취급한다() {
      // given
      User googleUser = persistUser("google-user@test.com");
      User kakaoUser = persistUser("kakao-user@test.com");
      socialAccountRepository.save(
          SocialAccount.link(googleUser, OAuth2Provider.GOOGLE, "1234", "google-user@gmail.com"));
      socialAccountRepository.save(
          SocialAccount.link(kakaoUser, OAuth2Provider.KAKAO, "1234", "kakao-user@kakao.com"));
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      Optional<SocialAccount> found = socialAccountRepository
          .findByProviderAndProviderId(OAuth2Provider.KAKAO, "1234");

      // then
      assertThat(found).isPresent();
      assertThat(found.get().getUser().getId()).isEqualTo(kakaoUser.getId());
    }
  }

  @Nested
  @DisplayName("저장 (save)")
  class Save {

    @Test
    @DisplayName("같은 provider와 providerId 조합을 다른 유저에 중복 저장하면 무결성 제약 예외가 발생한다")
    void 같은_provider와_providerId_조합을_다른_유저에_중복_저장하면_무결성_제약_예외가_발생한다() {
      // given
      User firstUser = persistUser("first@test.com");
      User secondUser = persistUser("second@test.com");
      socialAccountRepository.save(
          SocialAccount.link(firstUser, OAuth2Provider.GOOGLE, "dup-id", "first@gmail.com"));
      testEntityManager.flush();

      SocialAccount duplicate =
          SocialAccount.link(secondUser, OAuth2Provider.GOOGLE, "dup-id", "second@gmail.com");

      // when & then
      // saveAndFlush로 리포지토리 프록시를 거쳐야 Spring이 예외를 DataIntegrityViolationException으로 변환한다.
      // testEntityManager.flush()는 프록시를 안 거치므로 원본 ConstraintViolationException이 그대로 튀어나온다.
      assertThatThrownBy(() -> socialAccountRepository.saveAndFlush(duplicate))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }
}
