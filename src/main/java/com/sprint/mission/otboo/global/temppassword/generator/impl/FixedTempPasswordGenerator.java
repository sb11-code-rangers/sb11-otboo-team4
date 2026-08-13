package com.sprint.mission.otboo.global.temppassword.generator.impl;

import com.sprint.mission.otboo.global.temppassword.generator.TempPasswordGenerator;

public class FixedTempPasswordGenerator implements TempPasswordGenerator {

  @Override
  public String generate() {
    return "temporary1!!";
  }
}
