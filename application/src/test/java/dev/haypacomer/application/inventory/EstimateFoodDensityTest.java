package dev.haypacomer.application.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.application.ai.AiRateLimitExceededException;
import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.household.HouseholdNotFoundException;
import dev.haypacomer.application.port.FoodDensityEstimator;
import dev.haypacomer.application.support.InMemoryHouseholdRepository;
import dev.haypacomer.application.support.InMemoryInventoryStores;
import dev.haypacomer.domain.food.FoodCategory;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.ConversionFactors;
import dev.haypacomer.domain.quantity.Unit;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstimateFoodDensityTest {

  private final InMemoryHouseholdRepository households = new InMemoryHouseholdRepository();
  private final InMemoryInventoryStores stores = new InMemoryInventoryStores();
  private final List<String> asked = new ArrayList<>();
  private final List<String> limited = new ArrayList<>();
  private final UserId juan = UserId.newId();
  private boolean allowed = true;
  private Optional<BigDecimal> answer = Optional.empty();
  private boolean down;
  private Household household;

  private final FoodDensityEstimator estimator =
      new FoodDensityEstimator() {
        @Override
        public String provider() {
          return "fake";
        }

        @Override
        public Optional<BigDecimal> gramsPerMilliliter(String foodName) {
          asked.add(foodName);
          if (down) {
            throw FoodCheckUnavailableException.providerDown("down");
          }
          return answer;
        }
      };

  private final EstimateFoodDensity estimate =
      new EstimateFoodDensity(
          households,
          stores.catalog,
          estimator,
          (subject, limit, window) -> {
            limited.add(subject);
            return allowed;
          });

  @BeforeEach
  void createHouseholdAndCatalog() {
    household =
        Household.create(
            "Apartment", Currency.getInstance("COP"), ZoneId.of("UTC"), juan, Instant.now());
    households.save(household);
    stores.catalog.save(food("Milk", ConversionFactors.withDensity("1.03")));
    stores.catalog.save(food("Kumis", ConversionFactors.withPieceWeight("200")));
  }

  @Test
  void aKnownDensityComesFromTheCatalogWithoutTheAi() {
    DensityEstimate milk = estimate.estimate(juan, household.id(), " milk ");

    assertEquals(new DensityEstimate("Milk", new BigDecimal("1.03"), DensitySource.CATALOG), milk);
    assertTrue(asked.isEmpty());
    assertTrue(limited.isEmpty());
  }

  @Test
  void anAiEstimateIsRoundedSavedAndKnownNextTime() {
    answer = Optional.of(new BigDecimal("1.04567"));

    DensityEstimate kumis = estimate.estimate(juan, household.id(), "kumis");

    assertEquals(new DensityEstimate("Kumis", new BigDecimal("1.046"), DensitySource.AI), kumis);
    ConversionFactors saved = stores.catalog.findByName("Kumis").orElseThrow().conversion();
    assertEquals(new BigDecimal("1.046"), saved.gramsPerMilliliter());
    assertEquals(0, new BigDecimal("200").compareTo(saved.gramsPerPiece()));
    assertEquals(List.of("density:" + juan.value()), limited);
    assertEquals(DensitySource.CATALOG, estimate.estimate(juan, household.id(), "Kumis").source());
    assertEquals(List.of("Kumis"), asked);
  }

  @Test
  void implausibleOrMissingEstimatesCountAsWaterAndAreNotSaved() {
    for (Optional<BigDecimal> value :
        List.of(
            Optional.of(new BigDecimal("3.1")),
            Optional.of(new BigDecimal("0.1")),
            Optional.<BigDecimal>empty())) {
      answer = value;
      assertEquals(
          new DensityEstimate("Kumis", BigDecimal.ONE, DensitySource.DEFAULT),
          estimate.estimate(juan, household.id(), "Kumis"));
    }
    assertTrue(stores.catalog.findByName("Kumis").orElseThrow().conversion().density().isEmpty());
  }

  @Test
  void anUnavailableProviderCountsAsWater() {
    down = true;

    assertEquals(DensitySource.DEFAULT, estimate.estimate(juan, household.id(), "Kumis").source());
    assertTrue(stores.catalog.findByName("Kumis").orElseThrow().conversion().density().isEmpty());
  }

  @Test
  void strangersBlankNamesUnknownFoodsAndTooManyCallsAreRefused() {
    assertThrows(
        HouseholdNotFoundException.class,
        () -> estimate.estimate(UserId.newId(), household.id(), "Milk"));
    assertThrows(
        IllegalArgumentException.class, () -> estimate.estimate(juan, household.id(), " "));
    assertThrows(
        FoodNotInCatalogException.class, () -> estimate.estimate(juan, household.id(), "Shoes"));
    allowed = false;
    assertThrows(
        AiRateLimitExceededException.class, () -> estimate.estimate(juan, household.id(), "Kumis"));
    assertTrue(asked.isEmpty());
  }

  private static FoodMetadata food(String name, ConversionFactors conversion) {
    return new FoodMetadata(
        name, FoodCategory.DAIRY, Unit.MILLILITER, conversion, true, 7, Set.of());
  }
}
