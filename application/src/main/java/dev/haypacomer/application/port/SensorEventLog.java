package dev.haypacomer.application.port;

import dev.haypacomer.domain.device.DeviceId;
import dev.haypacomer.domain.sensor.SensorEvent;
import dev.haypacomer.domain.sensor.SensorEventId;
import java.time.Instant;
import java.util.Optional;

public interface SensorEventLog {

  boolean contains(DeviceId device, SensorEventId id);

  Optional<Instant> lastAccepted(DeviceId device, String type);

  boolean accept(SensorEvent event, Instant receivedAt);
}
