package com.sprint.mission.otboo.domain.authuser.user.service;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.UserCreateRequest;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.exception.DuplicateEmailException;
import com.sprint.mission.otboo.domain.authuser.user.mapper.AuthUserMapper;
import com.sprint.mission.otboo.domain.authuser.user.repository.ProfileRepository;
import com.sprint.mission.otboo.domain.authuser.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto signUp(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        User newUser = User.createUser(request.name(), request.email(), passwordEncoder.encode(request.password()));
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException();
        }

        Profile newDefaultProfile = Profile.createDefaultProfile(savedUser);
        profileRepository.save(newDefaultProfile);

        return authUserMapper.userDtoFromUser(savedUser);
    }
}
