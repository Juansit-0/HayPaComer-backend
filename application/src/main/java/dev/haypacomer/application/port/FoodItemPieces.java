package dev.haypacomer.application.port;

import dev.haypacomer.domain.fridge.FoodItemId;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public interface FoodItemPieces {

  void save(FoodItemId item, BigDecimal pieceGrams);

  Map<FoodItemId, BigDecimal> find(Collection<FoodItemId> items);
}
