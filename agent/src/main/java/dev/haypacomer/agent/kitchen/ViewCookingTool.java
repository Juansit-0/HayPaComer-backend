package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.session.ResumeCookingSession;
import java.util.List;

public final class ViewCookingTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.read(
          "view_cooking",
          "The active cooking session: recipe, phase, and current step.",
          List.of());

  private final ResumeCookingSession sessions;

  public ViewCookingTool(ResumeCookingSession sessions) {
    this.sessions = sessions;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Read the active cooking session";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    return Observation.of(
        SPEC.name(),
        sessions
            .active(invocation.user(), invocation.household())
            .map(CookingLines::describe)
            .orElse("No cooking session is active"));
  }
}
