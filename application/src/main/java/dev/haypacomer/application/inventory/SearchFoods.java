package dev.haypacomer.application.inventory;

import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import java.util.List;
import java.util.Objects;

public final class SearchFoods {

  private static final int MAX_RESULTS = 20;
  // Browsing lists the whole catalog for the add panel and the drop bar; it grows with what the AI
  // learns, so the cap stays well above it instead of hiding foods like the tomato.
  private static final int BROWSE_RESULTS = 1000;

  private final FoodCatalogRepository catalog;

  public SearchFoods(FoodCatalogRepository catalog) {
    this.catalog = Objects.requireNonNull(catalog, "catalog");
  }

  public List<FoodMetadata> search(String text) {
    if (text == null || text.isBlank()) {
      return catalog.search("", BROWSE_RESULTS);
    }
    return catalog.search(text, MAX_RESULTS);
  }
}
