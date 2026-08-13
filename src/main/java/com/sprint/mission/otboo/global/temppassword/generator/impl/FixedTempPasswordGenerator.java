package com.sprint.mission.otboo.global.temppassword.generator.impl;

import com.sprint.mission.otboo.global.temppassword.generator.TempPasswordGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"demo"}) // TODO: 논의 후 결정
public class FixedTempPasswordGenerator implements TempPasswordGenerator {

  @Override
  public String generate() {
    return "temporary1!!";
  }
}
