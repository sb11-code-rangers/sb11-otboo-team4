package com.sprint.mission.otboo.domain.authuser.user.entity;

import com.sprint.mission.otboo.domain.authuser.user.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

  private static final int DEFAULT_TEMPERATURE_SENSITIVITY = 3;

  @Id
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_profiles_user_id"))
  private User user;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private LocalDate birthDate;

  @Embedded
  private Location location;

  @Column(nullable = false)
  private int temperatureSensitivity;

  private String profileImageUrl;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  private Profile(User user, int temperatureSensitivity) {
    this.user = user;
    this.temperatureSensitivity = temperatureSensitivity;
  }

  public static Profile createDefault(User user) {
    return new Profile(user, DEFAULT_TEMPERATURE_SENSITIVITY);
  }

  public void changeFields(String newName, Gender newGender, LocalDate newBirthDate,
      Location newLocation, int newTemperatureSensitivity) {
    changeName(newName);
    changeGender(newGender);
    changeBirthDate(newBirthDate);
    if (newLocation != null) {
      changeLocation(newLocation);
    }
    changeTemperatureSensitivity(newTemperatureSensitivity);
  }

  public void changeName(String newName) {
    this.user.changeName(newName);
  }

  public void changeGender(Gender newGender) {
    this.gender = newGender;
  }

  public void changeBirthDate(LocalDate newBirthDate) {
    this.birthDate = newBirthDate;
  }

  public void changeLocation(Location newLocation) {
    this.location = newLocation;
  }

  public void changeTemperatureSensitivity(int newTemperatureSensitivity) {
    this.temperatureSensitivity = newTemperatureSensitivity;
  }
}
