package dev.haypacomer.sensors.scale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.haypacomer.domain.device.DeviceId;
import dev.haypacomer.domain.quantity.Grams;
import dev.haypacomer.domain.scale.RawSample;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryScaleSampleStoreTest {

  private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

  private final InMemoryScaleSampleStore store =
      new InMemoryScaleSampleStore(Clock.fixed(NOW, ZoneOffset.UTC));
  private final DeviceId scale = new DeviceId(UUID.randomUUID());

  @Test
  void aSampleFromAWrongClockDoesNotHideRealOnes() {
    RawSample real = new RawSample(1000, NOW);

    store.record(scale, new RawSample(9_999_999, NOW.plus(Duration.ofDays(365))));
    store.record(scale, real);

    assertEquals(real, store.latest(scale).orElseThrow());
    assertFalse(store.stable(scale, Grams.of(1), NOW.plus(Duration.ofDays(365))));
  }
}
