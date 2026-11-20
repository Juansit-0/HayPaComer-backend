package dev.haypacomer.agent.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class RuleBasedPlanner implements Planner {

  public static final String RAW_ANSWER = "Offline answer based on measured data";
  public static final String NOTHING_CHANGED = "Nothing was changed: ";

  private final List<String> reads;

  public RuleBasedPlanner(List<String> reads) {
    this.reads = List.copyOf(reads);
  }

  @Override
  public String name() {
    return "offline";
  }

  @Override
  public Decision next(AgentContext context) {
    Optional<OfflineIntents.Intent> intent =
        OfflineIntents.match(context.task().asked())
            .filter(found -> context.tools().contains(found.tool()));
    if (intent.isPresent()) {
      String tool = intent.get().tool();
      Optional<Observation> seen =
          context.history().stream()
              .filter(exchange -> exchange.call().tool().equals(tool))
              .map(Exchange::observation)
              .findFirst();
      if (seen.isPresent()) {
        Observation observation = seen.get();
        return new Decision.FinalAnswer(
            observation.failed() ? NOTHING_CHANGED + observation.content() : observation.content());
      }
      if (context.stepsLeft() > 0) {
        return new Decision.CallTool(
            tool, intent.get().arguments(), "Offline rules match what the person asked");
      }
    }
    for (String tool : reads) {
      if (context.tools().contains(tool) && !context.called(tool) && context.stepsLeft() > 0) {
        return new Decision.CallTool(tool, Map.of(), "Offline rules read " + tool);
      }
    }
    String evidence =
        context.history().stream()
            .map(Exchange::observation)
            .filter(observation -> !observation.failed())
            .map(observation -> observation.tool() + ": " + observation.content())
            .collect(Collectors.joining("\n"));
    if (evidence.isEmpty()) {
      return new Decision.FinalAnswer("No evidence available right now; nothing was changed.");
    }
    return new Decision.FinalAnswer(RAW_ANSWER + ":\n" + evidence);
  }
}
