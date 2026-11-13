package dev.haypacomer.domain.device;

import dev.haypacomer.domain.sensor.SensorEvent;
import dev.haypacomer.domain.sensor.WeightReading;

public enum DeviceKind {
  ESP32_DOOR_TEMP,
  ESP32_SCALE,
  SIMULATOR;

  public boolean emits(SensorEvent event) {
    return switch (this) {
      case ESP32_DOOR_TEMP -> !(event instanceof WeightReading);
      case ESP32_SCALE -> event instanceof WeightReading;
      case SIMULATOR -> true;
    };
  }
}
