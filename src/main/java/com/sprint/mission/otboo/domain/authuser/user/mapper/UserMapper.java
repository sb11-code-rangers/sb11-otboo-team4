package com.sprint.mission.otboo.domain.authuser.user.mapper;

import com.sprint.mission.otboo.domain.authuser.user.dto.request.LocationValidationDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.LocationDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.ProfileDto;
import com.sprint.mission.otboo.domain.authuser.user.dto.response.UserDto;
import com.sprint.mission.otboo.domain.authuser.user.entity.Location;
import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.security.details.CustomUserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto userDtoFrom(User user) {
    return new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.isLocked()
    );
  }

  public UserDto userDtoFrom(CustomUserDetails principal) {
    return new UserDto(
        principal.getUserId(),
        principal.getCreatedAt(),
        principal.getEmail(),
        principal.getName(),
        principal.getRole(),
        principal.isLocked()
    );
  }

  public ProfileDto profileDtoFrom(Profile profile) {
    return new ProfileDto(
        profile.getId(),
        profile.getUser().getName(),
        profile.getGender(),
        profile.getBirthDate(),
        profile.getLocation() == null ? null : locationDtoFrom(profile.getLocation()),
        profile.getTemperatureSensitivity(),
        profile.getProfileImageUrl()
    );
  }

  public LocationDto locationDtoFrom(Location location) {
    return new LocationDto(
        location.getLatitude(),
        location.getLongitude(),
        location.getLocationX(),
        location.getLocationY(),
        location.getLocationNames());
  }

  public Location locationFrom(LocationValidationDto dto) {
    return dto == null ? null : Location.create(
        dto.latitude(), dto.longitude(), dto.x(), dto.y(), dto.locationNames()
    );
  }
}
