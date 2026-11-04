package dev.haypacomer.agent.runtime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RuleBasedPlanner implements Planner {

  private static final String ADD_TO_MARKET = "add_to_market";
  private static final String DEFAULT_GRAMS = "500";
  private static final Pattern ADD =
      Pattern.compile(
          "\\b(?:agrega|agregar|agregame|añade|añadir|anota|anotar|pon|compra|comprar|add|buy)\\s+"
              + "(?:(\\d+(?:[.,]\\d+)?)\\s*(kg|kilos?|g|gr|gramos?)\\s+(?:de\\s+)?)?"
              + "(.+?)(?:\\s+(?:al|a la|en el|en la|to the|to my|to)\\s+.*)?[.!?¿]*$",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
    Matcher asked = ADD.matcher(context.task().goal().strip());
    if (context.tools().contains(ADD_TO_MARKET)
        && !context.called(ADD_TO_MARKET)
        && context.stepsLeft() > 0
        && asked.find()) {
      return new Decision.CallTool(
          ADD_TO_MARKET,
          Map.of("food", asked.group(3).strip(), "grams", grams(asked)),
          "Offline rules propose what the person asked to add");
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
    return new Decision.FinalAnswer("Offline answer based on measured data:\n" + evidence);
  }

  // The person confirms the amount before anything is added, so a missing one can start from a
  // usual buy.
  private static String grams(Matcher asked) {
    if (asked.group(1) == null) {
      return DEFAULT_GRAMS;
    }
    BigDecimal amount = new BigDecimal(asked.group(1).replace(',', '.'));
    boolean kilos = asked.group(2).toLowerCase(Locale.ROOT).startsWith("k");
    return (kilos ? amount.movePointRight(3) : amount).toPlainString();
  }
}
