package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.moving.CarriedItem;
import dev.haypacomer.application.port.CarriedFood;
import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.inventory.Visibility;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

@Repository
public class PostgresCarriedFood implements CarriedFood {

  private final JdbcClient jdbc;
  private final TransactionTemplate transaction;

  public PostgresCarriedFood(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
    this.transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
  }

  @Override
  public int packUp(HouseholdId household, UserId user) {
    Integer packed =
        transaction.execute(
            status -> {
              int copied =
                  jdbc.sql(
                          """
                          INSERT INTO carried_food (id, user_id, food_id, quantity_g, expires_on,
                                                    expiry_source, opened, visibility, piece_grams)
                          SELECT gen_random_uuid(), m.user_id, i.food_id, i.quantity_g, i.expires_on,
                                 i.expiry_source, i.opened_on IS NOT NULL, i.visibility, p.piece_grams
                          FROM food_items i
                          JOIN household_members m ON m.member_id = i.owner_member_id
                          LEFT JOIN food_item_pieces p ON p.food_item_id = i.id
                          WHERE i.household_id = :household AND m.user_id = :user
                            AND i.quantity_g > 0
                          """)
                      .param("household", household.value())
                      .param("user", user.value())
                      .update();
              jdbc.sql(
                      """
                      DELETE FROM food_items i USING household_members m
                      WHERE m.member_id = i.owner_member_id
                        AND i.household_id = :household AND m.user_id = :user
                      """)
                  .param("household", household.value())
                  .param("user", user.value())
                  .update();
              return copied;
            });
    return packed == null ? 0 : packed;
  }

  @Override
  public List<CarriedItem> waiting(UserId user) {
    return jdbc.sql(
            """
            SELECT c.id, f.name, c.quantity_g, c.expires_on, c.expiry_source, c.opened,
                   c.visibility, c.piece_grams
            FROM carried_food c JOIN food_catalog f ON f.id = c.food_id
            WHERE c.user_id = :user ORDER BY c.packed_at, c.id
            """)
        .param("user", user.value())
        .query(
            (row, index) -> {
              Date expires = row.getDate("expires_on");
              String source = row.getString("expiry_source");
              return new CarriedItem(
                  row.getObject("id", UUID.class),
                  row.getString("name"),
                  row.getBigDecimal("quantity_g"),
                  expires == null ? null : expires.toLocalDate(),
                  source == null ? null : ExpirySource.valueOf(source),
                  row.getBoolean("opened"),
                  Visibility.valueOf(row.getString("visibility")),
                  row.getBigDecimal("piece_grams"));
            })
        .list();
  }

  @Override
  public void unpacked(UUID carriedId) {
    jdbc.sql("DELETE FROM carried_food WHERE id = :id").param("id", carriedId).update();
  }
}
