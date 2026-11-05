package dev.haypacomer.application.fridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.application.coldchain.FridgeNotFoundException;
import dev.haypacomer.application.household.HouseholdNotFoundException;
import dev.haypacomer.application.port.FridgeShapeRepository;
import dev.haypacomer.application.support.InMemoryFridgeRepository;
import dev.haypacomer.application.support.InMemoryHouseholdRepository;
import dev.haypacomer.application.support.InMemoryInventoryStores;
import dev.haypacomer.domain.food.FoodCategory;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeId;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.TrayNotEmptyException;
import dev.haypacomer.domain.household.AccessDeniedException;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.ConversionFactors;
import dev.haypacomer.domain.quantity.Grams;
import dev.haypacomer.domain.quantity.Unit;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangeFridgeShapeTest {

  private static final Instant NOW = Instant.parse("2026-10-31T15:00:00Z");
  private static final FridgeShape CR121 = FridgeShape.examples().getFirst().shape();

  private final InMemoryHouseholdRepository households = new InMemoryHouseholdRepository();
  private final InMemoryFridgeRepository fridges = new InMemoryFridgeRepository();
  private final InMemoryInventoryStores stores = new InMemoryInventoryStores();
  private final Map<FridgeId, FridgeShape> stored = new HashMap<>();
  private final FridgeShapeRepository shapes =
      new FridgeShapeRepository() {
        @Override
        public Optional<FridgeShape> find(FridgeId fridge) {
          return Optional.ofNullable(stored.get(fridge));
        }

        @Override
        public void save(FridgeId fridge, FridgeShape shape) {
          stored.put(fridge, shape);
        }
      };
  private final ChangeFridgeShape change =
      new ChangeFridgeShape(
          households,
          fridges,
          shapes,
          stores.audit,
          stores.unitOfWork,
          Clock.fixed(NOW, ZoneOffset.UTC));
  private final ViewFridgeShape view = new ViewFridgeShape(households, fridges, shapes);
  private final UserId juan = UserId.newId();
  private final UserId ana = UserId.newId();
  private Household household;
  private Fridge fridge;

  @BeforeEach
  void setUp() {
    household =
        Household.create("Apartment", Currency.getInstance("COP"), ZoneId.of("UTC"), juan, NOW);
    household.join(ana, Role.GUEST, NOW);
    households.save(household);
    fridge =
        new SetUpFridge(households, fridges)
            .setUp(juan, household.id(), "Kitchen", FridgeLayout.STANDARD);
  }

  @Test
  void aStandardFridgeBecomesTheChallengerMinibarAndIsReadBackSo() {
    assertEquals(FridgeType.TOP_FREEZER, view.view(ana, household.id(), fridge.id()).type());

    FridgeShape changed = change.change(juan, household.id(), fridge.id(), CR121);

    assertEquals(CR121, changed);
    assertEquals(CR121, view.view(ana, household.id(), fridge.id()));
    assertEquals("CHANGE_FRIDGE_SHAPE", stores.auditEntries.getLast().action());
    assertEquals("MINIBAR", stores.auditEntries.getLast().detail().get("type"));
  }

  @Test
  void aTrayWithFoodIsNeverRemoved() {
    Tray bottom = fridge.children().getFirst().children().get(2);
    bottom.add(
        FoodItem.weighed(
            new FoodMetadata(
                "Milk",
                FoodCategory.DAIRY,
                Unit.MILLILITER,
                ConversionFactors.MASS_ONLY,
                true,
                7,
                Set.of()),
            Grams.of(842),
            Grams.ZERO,
            LocalDate.of(2026, 11, 5)));

    change.change(juan, household.id(), fridge.id(), CR121);
    assertTrue(fridge.trays().anyMatch(tray -> tray.id().equals(bottom.id())));

    FridgeShape noShelves = new FridgeShape(FridgeType.MINIBAR, 0, 3, 0, 1, Hinge.LEFT, 49, 85, 60);
    TrayNotEmptyException full =
        assertThrows(
            TrayNotEmptyException.class,
            () -> change.change(juan, household.id(), fridge.id(), noShelves));

    assertEquals(bottom.id(), full.tray());
  }

  @Test
  void onlyWhoManagesFridgesAndOnlyFridgesOfTheHousehold() {
    assertThrows(
        AccessDeniedException.class, () -> change.change(ana, household.id(), fridge.id(), CR121));
    assertThrows(
        FridgeNotFoundException.class,
        () -> change.change(juan, household.id(), FridgeId.newId(), CR121));
    assertThrows(
        FridgeNotFoundException.class, () -> view.view(juan, household.id(), FridgeId.newId()));
    assertThrows(
        HouseholdNotFoundException.class,
        () -> view.view(UserId.newId(), household.id(), fridge.id()));
  }
}
