package dev.haypacomer.web.planning;

import dev.haypacomer.application.planning.ListRecipeTemplates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recipe templates", description = "Ready-made recipes to copy")
@RestController
public class RecipeTemplateController {

  private final ListRecipeTemplates listRecipeTemplates;

  public RecipeTemplateController(ListRecipeTemplates listRecipeTemplates) {
    this.listRecipeTemplates = listRecipeTemplates;
  }

  @Operation(summary = "List the ready-made recipe templates")
  @GetMapping("/api/v1/recipe-templates")
  List<PlanningController.RecipeResponse> templates() {
    return listRecipeTemplates.list().stream()
        .map(PlanningController.RecipeResponse::from)
        .toList();
  }
}
