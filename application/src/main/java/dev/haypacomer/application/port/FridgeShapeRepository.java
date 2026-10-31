package dev.haypacomer.application.port;

import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.domain.fridge.FridgeId;
import java.util.Optional;

public interface FridgeShapeRepository {

  Optional<FridgeShape> find(FridgeId fridge);

  void save(FridgeId fridge, FridgeShape shape);
}
