package com.sprint.mission.otboo.domain.authuser.user.service;

import com.sprint.mission.otboo.domain.authuser.auth.exception.AccessDeniedException;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ChangePasswordRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.ProfileUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserListParams;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserLockUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Location;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.entity.enums.LockReason;
import com.sprint.mission.otboo.domain.authuser.user.exception.DuplicateEmailException;
import com.sprint.mission.otboo.domain.authuser.user.exception.UserNotFoundException;
import com.sprint.mission.otboo.domain.authuser.user.mapper.UserMapper;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import com.sprint.mission.otboo.global.temppassword.registry.TempPasswordRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserSessionRegistry userSessionRegistry;
  private final TempPasswordRegistry tempPasswordRegistry;

  public CursorPageResponse<UserDto> getUsers(UserListParams condition) {
    return userRepository.search(condition);
  }

  @Transactional
  public UserDto signUp(UserCreateRequest request) {

    if (userRepository.existsByEmail(request.email())) {
      throw DuplicateEmailException.withEmail(request.email());
    }

    User newUser = User.create(request.name(), request.email(),
        passwordEncoder.encode(request.password()));
    User savedUser;
    try {
      savedUser = userRepository.saveAndFlush(newUser);
    } catch (DataIntegrityViolationException e) {
      throw DuplicateEmailException.withEmail(request.email());
    }

    Profile newDefaultProfile = Profile.createDefault(savedUser);
    profileRepository.save(newDefaultProfile);

    return userMapper.userDtoFrom(savedUser);
  }

  @Transactional
  public UserDto changeRole(UUID userId, UserRoleUpdateRequest request) {
    User foundUser = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::withNone);

    foundUser.changeRole(request.role());

    // 권한 변경 시 강제 로그아웃
    userSessionRegistry.revoke(userId);

    return userMapper.userDtoFrom(foundUser);
  }

  public ProfileDto getProfile(UUID userId, UUID requestUserId) {
    checkSelf(userId, requestUserId);
    Profile foundProfile = profileRepository.findByIdWithUser(userId)
        .orElseThrow(UserNotFoundException::withNone);
    return userMapper.profileDtoFrom(foundProfile);
  }

  @Transactional
  public ProfileDto changeProfile(UUID userId, UUID requestUserId, ProfileUpdateRequest request,
      MultipartFile image) {
    checkSelf(userId, requestUserId);

    Profile foundProfile = profileRepository.findByIdWithUser(userId)
        .orElseThrow(UserNotFoundException::withNone);

    Location newLocation = userMapper.locationFrom(request.location());
    foundProfile.changeFields(
        request.name(),
        request.gender(),
        request.birthDate(),
        newLocation,
        request.temperatureSensitivity()
    );

    // TODO: 이미지 저장 로직 반드시 필요 (팀원 간의 논의 후 Fix)

    return userMapper.profileDtoFrom(foundProfile);
  }

  @Transactional
  public UserDto changePassword(UUID userId, UUID requestUserId, ChangePasswordRequest request) {
    checkSelf(userId, requestUserId);

    User foundUser = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::withNone);

    foundUser.changePassword(passwordEncoder.encode(request.password()));

    // 비밀번호 변경 시 강제 로그아웃
    userSessionRegistry.revoke(userId);

    // 비밀번호 변경 시 tempPasswordRegistry 파기
    tempPasswordRegistry.revoke(userId);

    return userMapper.userDtoFrom(foundUser);
  }

  @Transactional
  public UserDto changeLocked(UUID userId, UserLockUpdateRequest request) {
    User foundUser = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::withNone);

    if (request.locked()) {
      foundUser.lock(LockReason.ADMIN_ACTION);
    } else {
      foundUser.unlock();
    }

    // 잠금 상태 변경 시 강제 로그아웃
    userSessionRegistry.revoke(userId);

    return userMapper.userDtoFrom(foundUser);
  }

  private void checkSelf(UUID userId, UUID requestUserId) {
    if (!userId.equals(requestUserId)) {
      throw AccessDeniedException.withNone();
    }
  }
}
