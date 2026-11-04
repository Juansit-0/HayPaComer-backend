package dev.haypacomer.sensors.scale;

import dev.haypacomer.application.port.ScaleSampleStore;
import dev.haypacomer.domain.device.DeviceId;
import dev.haypacomer.domain.quantity.Grams;
import dev.haypacomer.domain.scale.RawSample;
import dev.haypacomer.domain.scale.StabilityDetector;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryScaleSampleStore implements ScaleSampleStore {

  private static final Duration CLOCK_SKEW = Duration.ofMinutes(2);

  private final Map<DeviceId, RawSample> latest = new ConcurrentHashMap<>();
  private final Map<DeviceId, StabilityDetector> detectors = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryScaleSampleStore() {
    this(Clock.systemUTC());
  }

  public InMemoryScaleSampleStore(Clock clock) {
    this.clock = clock;
  }

  // Newest wins below, so one sample from a wrong clock would hide every real one after it.
  private boolean fromTheFuture(Instant at) {
    return at.isAfter(clock.instant().plus(CLOCK_SKEW));
  }

  @Override
  public void record(DeviceId device, RawSample sample) {
    if (fromTheFuture(sample.at())) {
      return;
    }
    latest.merge(device, sample, (old, fresh) -> fresh.at().isBefore(old.at()) ? old : fresh);
  }

  @Override
  public Optional<RawSample> latest(DeviceId device) {
    return Optional.ofNullable(latest.get(device));
  }

  @Override
  public boolean stable(DeviceId device, Grams grams, Instant at) {
    if (fromTheFuture(at)) {
      return false;
    }
    return detectors.computeIfAbsent(device, id -> StabilityDetector.standard()).offer(grams, at);
  }
}
