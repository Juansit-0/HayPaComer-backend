package dev.haypacomer.persistence.relational;

import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.application.fridge.FridgeType;
import dev.haypacomer.application.fridge.Hinge;
import dev.haypacomer.application.port.FridgeShapeRepository;
import dev.haypacomer.domain.fridge.FridgeId;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresFridgeShapeRepository implements FridgeShapeRepository {

  private final JdbcClient jdbc;

  public PostgresFridgeShapeRepository(DataSource dataSource) {
    this.jdbc = JdbcClient.create(dataSource);
  }

  @Override
  public Optional<FridgeShape> find(FridgeId fridge) {
    return jdbc.sql(
            """
            SELECT type, shelves, door_racks, drawers, freezer_shelves, hinge, width_cm, height_cm,
                   depth_cm
            FROM fridge_shapes WHERE fridge_id = :fridge
            """)
        .param("fridge", fridge.value())
        .query(
            (row, index) ->
                new FridgeShape(
                    FridgeType.valueOf(row.getString("type")),
                    row.getInt("shelves"),
                    row.getInt("door_racks"),
                    row.getInt("drawers"),
                    row.getInt("freezer_shelves"),
                    Hinge.valueOf(row.getString("hinge")),
                    row.getInt("width_cm"),
                    row.getInt("height_cm"),
                    row.getInt("depth_cm")))
        .optional();
  }

  @Override
  public void save(FridgeId fridge, FridgeShape shape) {
    jdbc.sql(
            """
            INSERT INTO fridge_shapes (fridge_id, type, shelves, door_racks, drawers, freezer_shelves,
                                       hinge, width_cm, height_cm, depth_cm, updated_at)
            VALUES (:fridge, :type, :shelves, :racks, :drawers, :freezer, :hinge, :width, :height,
                    :depth, now())
            ON CONFLICT (fridge_id) DO UPDATE SET
                type = EXCLUDED.type, shelves = EXCLUDED.shelves, door_racks = EXCLUDED.door_racks,
                drawers = EXCLUDED.drawers, freezer_shelves = EXCLUDED.freezer_shelves,
                hinge = EXCLUDED.hinge, width_cm = EXCLUDED.width_cm,
                height_cm = EXCLUDED.height_cm, depth_cm = EXCLUDED.depth_cm, updated_at = now()
            """)
        .param("fridge", fridge.value())
        .param("type", shape.type().name())
        .param("shelves", shape.shelves())
        .param("racks", shape.doorRacks())
        .param("drawers", shape.drawers())
        .param("freezer", shape.freezerShelves())
        .param("hinge", shape.hinge().name())
        .param("width", shape.widthCm())
        .param("height", shape.heightCm())
        .param("depth", shape.depthCm())
        .update();
  }
}
