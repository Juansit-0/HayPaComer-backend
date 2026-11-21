package dev.haypacomer.web.resilience;

import dev.haypacomer.application.port.ServiceHealth;
import dev.haypacomer.application.resilience.DegradedComponent;
import dev.haypacomer.application.resilience.ServiceStatus;
import dev.haypacomer.application.resilience.ViewServiceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Status", description = "Health of the database and AI providers")
@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

  static final String CHEF_MEMORY = "chef-memory";

  private final ViewServiceStatus viewStatus;
  private final ServiceHealth health;
  private final RedisConnectionFactory redis;
  private final Clock clock;

  public StatusController(
      ViewServiceStatus viewStatus,
      ServiceHealth health,
      RedisConnectionFactory redis,
      Clock clock) {
    this.viewStatus = viewStatus;
    this.health = health;
    this.redis = redis;
    this.clock = clock;
  }

  @Operation(summary = "Show whether the database and AI providers are available")
  @GetMapping
  StatusResponse status() {
    checkChefMemory();
    ServiceStatus status = viewStatus.view();
    return new StatusResponse(
        status.healthy() ? "OK" : "DEGRADED",
        status.degraded().stream().map(ComponentResponse::from).toList());
  }

  private void checkChefMemory() {
    try (RedisConnection connection = redis.getConnection()) {
      connection.ping();
      health.recovered(CHEF_MEMORY);
    } catch (RuntimeException unreachable) {
      health.degraded(CHEF_MEMORY, "The Chef cannot reach its memory", clock.instant());
    }
  }

  record ComponentResponse(String component, String reason, Instant since) {

    static ComponentResponse from(DegradedComponent component) {
      return new ComponentResponse(component.component(), component.reason(), component.since());
    }
  }

  record StatusResponse(String state, List<ComponentResponse> degraded) {}
}
