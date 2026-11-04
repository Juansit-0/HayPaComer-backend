package dev.haypacomer.agent.proactive;

import dev.haypacomer.agent.runtime.RuleBasedPlanner;
import dev.haypacomer.agent.supervisor.Supervisor;
import dev.haypacomer.application.notification.Notification;
import dev.haypacomer.application.notification.NotificationType;
import dev.haypacomer.application.notification.NotifyHousehold;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.HouseholdId;
import java.time.Clock;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class Briefer {

  public static final String OPEN_THE_APP =
      "Open HayPaComer to review the fridge and the food at risk.";

  static final int MAX_BODY = 1500;

  private final HouseholdRepository households;
  private final Supervisor supervisor;
  private final NotifyHousehold delivery;
  private final Clock clock;

  public Briefer(
      HouseholdRepository households,
      Supervisor supervisor,
      NotifyHousehold delivery,
      Clock clock) {
    this.households = households;
    this.supervisor = supervisor;
    this.delivery = delivery;
    this.clock = clock;
  }

  public Notification publish(HouseholdId household, String title, String body) {
    Notification digest =
        Notification.of(
            household,
            NotificationType.BRIEFING,
            title,
            body.length() > MAX_BODY ? body.substring(0, MAX_BODY) : body,
            clock.instant());
    delivery.publish(digest);
    return digest;
  }

  private static String shortened(String answer) {
    return answer.length() > MAX_BODY ? answer.substring(0, MAX_BODY) : answer;
  }

  // Without AI the agent answers with a raw list of readings, which must not bury the alert.
  public Optional<Notification> briefAlert(
      HouseholdId household, String specialist, String goal, String title) {
    return brief(
        household,
        specialist,
        goal,
        title,
        answer ->
            answer.startsWith(RuleBasedPlanner.RAW_ANSWER) ? OPEN_THE_APP : shortened(answer));
  }

  public Optional<Notification> brief(
      HouseholdId household, String specialist, String goal, String title) {
    return brief(household, specialist, goal, title, Briefer::shortened);
  }

  private Optional<Notification> brief(
      HouseholdId household,
      String specialist,
      String goal,
      String title,
      UnaryOperator<String> body) {
    return households
        .findById(household)
        .map(
            found -> {
              String answer =
                  supervisor
                      .handle(household, found.owner(), goal, Optional.of(specialist))
                      .answer();
              Notification briefing =
                  Notification.of(
                      household,
                      NotificationType.BRIEFING,
                      title,
                      body.apply(answer),
                      clock.instant());
              delivery.publish(briefing);
              return briefing;
            });
  }
}
