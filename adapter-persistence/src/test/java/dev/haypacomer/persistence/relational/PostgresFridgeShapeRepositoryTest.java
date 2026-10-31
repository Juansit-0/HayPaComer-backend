package dev.haypacomer.persistence.relational;

import static dev.haypacomer.persistence.relational.PersistenceFixtures.NOW;
import static dev.haypacomer.persistence.relational.PersistenceFixtures.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.application.fridge.FridgeShape;
import dev.haypacomer.application.fridge.FridgeType;
import dev.haypacomer.application.fridge.Hinge;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.identity.User;
import java.time.ZoneId;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class PostgresFridgeShapeRepositoryTest extends PostgresTestSupport {

  @Test
  void savesAndReplacesTheShapeOfAFridge() {
    User juan = user("juan@haypacomer.dev", "Juan");
    new PostgresUserRepository(dataSource).save(juan);
    Household household =
        Household.create(
            "Apartment", Currency.getInstance("COP"), ZoneId.of("UTC"), juan.id(), NOW);
    new PostgresHouseholdRepository(dataSource).save(household);
    Fridge fridge = Fridge.named("Kitchen");
    new PostgresFridgeRepository(dataSource).save(household.id(), fridge);
    PostgresFridgeShapeRepository shapes = new PostgresFridgeShapeRepository(dataSource);

    FridgeShape minibar = FridgeShape.examples().getFirst().shape();
    shapes.save(fridge.id(), minibar);
    assertEquals(minibar, shapes.find(fridge.id()).orElseThrow());

    FridgeShape twoDoors =
        new FridgeShape(FridgeType.BOTTOM_FREEZER, 4, 3, 1, 2, Hinge.LEFT, 60, 185, 65);
    shapes.save(fridge.id(), twoDoors);
    assertEquals(twoDoors, shapes.find(fridge.id()).orElseThrow());
    assertTrue(shapes.find(FridgeId.newId()).isEmpty());
  }
}
