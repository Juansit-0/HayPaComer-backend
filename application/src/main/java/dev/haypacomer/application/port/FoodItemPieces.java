package dev.haypacomer.application.port;

import dev.haypacomer.domain.fridge.FoodItemId;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/** The weight of one unit for food added by units, so it is drawn as that many pieces. */
public interface FoodItemPieces {

  void save(FoodItemId item, BigDecimal pieceGrams);

  Map<FoodItemId, BigDecimal> find(Collection<FoodItemId> items);
}
