package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.port.FoodItemPieces;
import dev.haypacomer.domain.fridge.FoodItemId;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresFoodItemPieces implements FoodItemPieces {

  private final JdbcClient jdbc;

  public PostgresFoodItemPieces(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  @Override
  public void save(FoodItemId item, BigDecimal pieceGrams) {
    jdbc.sql(
            """
            INSERT INTO food_item_pieces (food_item_id, piece_grams) VALUES (:item, :grams)
            ON CONFLICT (food_item_id) DO UPDATE SET piece_grams = EXCLUDED.piece_grams
            """)
        .param("item", item.value())
        .param("grams", pieceGrams)
        .update();
  }

  @Override
  public Map<FoodItemId, BigDecimal> find(Collection<FoodItemId> items) {
    Map<FoodItemId, BigDecimal> found = new HashMap<>();
    if (items.isEmpty()) {
      return found;
    }
    jdbc.sql(
            "SELECT food_item_id, piece_grams FROM food_item_pieces WHERE food_item_id IN (:items)")
        .param("items", items.stream().map(FoodItemId::value).toList())
        .query(
            (row, index) ->
                found.put(
                    new FoodItemId(row.getObject("food_item_id", UUID.class)),
                    row.getBigDecimal("piece_grams")))
        .list();
    return found;
  }
}
