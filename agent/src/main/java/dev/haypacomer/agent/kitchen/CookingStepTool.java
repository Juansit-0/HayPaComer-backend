package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.session.AdvanceCookingSession;
import dev.haypacomer.application.session.ResumeCookingSession;
import dev.haypacomer.application.session.SessionAction;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.session.CookingSession;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CookingStepTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "cooking_step",
          "Propose moving the active cooking session: NEXT step, PAUSE, RESUME, or ABANDON,"
              + " after the person confirms it.",
          Permission.COOK,
          List.of(
              ParameterSpec.required(
                  "action", ParameterType.TEXT, "NEXT, PAUSE, RESUME, or ABANDON")));

  private final AdvanceCookingSession advance;
  private final ResumeCookingSession sessions;

  public CookingStepTool(AdvanceCookingSession advance, ResumeCookingSession sessions) {
    this.advance = advance;
    this.sessions = sessions;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    if (action(invocation).isEmpty()) {
      return Optional.of("action must be NEXT, PAUSE, RESUME, or ABANDON");
    }
    return active(invocation).isPresent()
        ? Optional.empty()
        : Optional.of("No cooking session is active; start a recipe first");
  }

  @Override
  public String describe(ToolInvocation invocation) {
    String recipe = active(invocation).map(session -> session.recipe().name()).orElse("the recipe");
    return switch (action(invocation).orElse(SessionAction.NEXT)) {
      case NEXT -> "Go to the next step of " + recipe;
      case PAUSE -> "Pause " + recipe;
      case RESUME -> "Resume " + recipe;
      case ABANDON -> "Cancel cooking " + recipe;
    };
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    CookingSession session =
        active(invocation)
            .orElseThrow(() -> new IllegalStateException("No cooking session is active"));
    CookingSession moved =
        advance.apply(
            invocation.user(),
            invocation.household(),
            session.id(),
            action(invocation).orElseThrow());
    return Observation.of(SPEC.name(), CookingLines.describe(moved));
  }

  private Optional<CookingSession> active(ToolInvocation invocation) {
    return sessions.active(invocation.user(), invocation.household());
  }

  private static Optional<SessionAction> action(ToolInvocation invocation) {
    try {
      return Optional.of(
          SessionAction.valueOf(
              invocation.arguments().get("action").strip().toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException unknown) {
      return Optional.empty();
    }
  }
}
