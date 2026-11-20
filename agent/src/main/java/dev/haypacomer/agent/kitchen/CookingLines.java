package dev.haypacomer.agent.kitchen;

import dev.haypacomer.domain.session.CookingSession;
import dev.haypacomer.domain.session.SessionPhase;

final class CookingLines {

  private CookingLines() {}

  static String describe(CookingSession session) {
    String head =
        session.recipe().name() + " for " + session.recipe().servings() + ", " + session.phase();
    int steps = session.recipe().steps().size();
    if (session.phase() == SessionPhase.PREPARING) {
      return head + ", " + steps + " steps; next starts step 1";
    }
    return session
        .step()
        .map(
            step ->
                head
                    + ", step "
                    + step.position()
                    + " of "
                    + steps
                    + ": "
                    + step.instruction()
                    + step.timerDuration()
                        .map(timer -> " (timer " + timer.toMinutes() + " min)")
                        .orElse(""))
        .orElse(head);
  }
}
