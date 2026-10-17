package dev.haypacomer.application.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.application.ai.AdvisorSource;
import dev.haypacomer.application.ai.AiRateLimitExceededException;
import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.ai.FoodVerdict;
import dev.haypacomer.application.port.FoodClassifier;
import dev.haypacomer.application.support.InMemoryHouseholdRepository;
import dev.haypacomer.application.support.InMemoryInventoryStores;
import dev.haypacomer.domain.food.FoodCategory;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.household.AccessDeniedException;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.UserId;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LearnFoodTest {

  private final InMemoryHouseholdRepository households = new InMemoryHouseholdRepository();
  private final InMemoryInventoryStores stores = new InMemoryInventoryStores();
  private final List<String> asked = new ArrayList<>();
  private final UserId juan = UserId.newId();
  private final UserId guest = UserId.newId();
  private boolean allowed = true;
  private Household household;

  private final FoodClassifier classifier =
      new FoodClassifier() {
        @Override
        public String provider() {
          return "fake";
        }

        @Override
        public FoodVerdict classify(String name) {
          asked.add(name);
          return switch (name) {
            case "guayaba" ->
                new FoodVerdict(
                    true,
                    "Guayaba",
                    FoodCategory.FRUIT,
                    true,
                    5,
                    "A tropical fruit",
                    AdvisorSource.GEMINI);
            case "zapatos" ->
                new FoodVerdict(
                    false, name, null, false, 0, "Shoes are not eaten", AdvisorSource.GEMINI);
            default -> throw new FoodCheckUnavailableException("down");
          };
        }
      };

  private final LearnFood learnFood =
      new LearnFood(households, stores.catalog, classifier, (subject, limit, window) -> allowed);

  @BeforeEach
  void createHousehold() {
    household =
        Household.create(
            "Apartment", Currency.getInstance("COP"), ZoneId.of("UTC"), juan, Instant.now());
    household.join(guest, Role.GUEST, Instant.now());
    households.save(household);
  }

  @Test
  void learnsAFoodTheAiRecognisesAndKeepsItInTheCatalog() {
    FoodMetadata guava = learnFood.learn(juan, household.id(), "guayaba");

    assertEquals("Guayaba", guava.name());
    assertEquals(FoodCategory.FRUIT, guava.category());
    assertEquals(5, guava.shelfDays());
    assertTrue(stores.catalog.findByName("Guayaba").isPresent());
    learnFood.learn(juan, household.id(), "Guayaba");
    assertEquals(List.of("guayaba"), asked);
  }

  @Test
  void refusesWhatIsNotFood() {
    NotFoodException refused =
        assertThrows(
            NotFoodException.class, () -> learnFood.learn(juan, household.id(), "zapatos"));

    assertTrue(refused.getMessage().contains("Shoes are not eaten"));
    assertTrue(stores.catalog.findByName("zapatos").isEmpty());
  }

  @Test
  void withoutTheAiOnlyCatalogFoodsAreAccepted() {
    stores.catalog.save(
        new FoodMetadata(
            "Tomato",
            FoodCategory.VEGETABLE,
            dev.haypacomer.domain.quantity.Unit.GRAM,
            dev.haypacomer.domain.quantity.ConversionFactors.MASS_ONLY,
            true,
            7,
            java.util.Set.of()));

    assertEquals("Tomato", learnFood.learn(juan, household.id(), "tomato").name());
    assertThrows(
        FoodCheckUnavailableException.class, () -> learnFood.learn(juan, household.id(), "lulo"));
  }

  @Test
  void guestsCannotTeachFoodsAndCallsAreRateLimited() {
    assertThrows(
        AccessDeniedException.class, () -> learnFood.learn(guest, household.id(), "guayaba"));
    assertThrows(IllegalArgumentException.class, () -> learnFood.learn(juan, household.id(), " "));
    allowed = false;
    assertThrows(
        AiRateLimitExceededException.class, () -> learnFood.learn(juan, household.id(), "guayaba"));
  }
}
