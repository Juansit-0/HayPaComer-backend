package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.inventory.InventorySnapshot;
import dev.haypacomer.application.inventory.UndoLastChange;
import dev.haypacomer.domain.household.Permission;
import java.util.List;
import java.util.Locale;

public final class UndoLastChangeTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "undo_last_change",
          "Propose undoing the last inventory change (its author or the owner), after the person"
              + " confirms it.",
          Permission.MANAGE_OWN_ITEMS,
          List.of());

  private final UndoLastChange undo;

  public UndoLastChangeTool(UndoLastChange undo) {
    this.undo = undo;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Undo the last inventory change";
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    InventorySnapshot undone = undo.undo(invocation.user(), invocation.household());
    return Observation.of(
        SPEC.name(),
        "Undid the last change: "
            + undone.reason().toLowerCase(Locale.ROOT).replace('_', ' ')
            + " at "
            + undone.at());
  }
}
