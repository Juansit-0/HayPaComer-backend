package dev.haypacomer.application.planning;

public final class RecipeInPlanException extends IllegalStateException {

  public RecipeInPlanException() {
    super("This recipe is in a weekly plan; change those meals before deleting it");
  }
}
