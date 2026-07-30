package com.sprint.mission.otboo.domain.authuser.user.repository;

import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.repository.querydsl.UserCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, UserCustomRepository {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);
}
