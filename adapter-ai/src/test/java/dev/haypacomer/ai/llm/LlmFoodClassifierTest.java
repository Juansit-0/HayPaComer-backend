package dev.haypacomer.ai.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.ai.offline.OfflineFoodClassifier;
import dev.haypacomer.application.ai.AdvisorSource;
import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import dev.haypacomer.application.ai.FoodVerdict;
import dev.haypacomer.domain.food.FoodCategory;
import org.junit.jupiter.api.Test;

class LlmFoodClassifierTest {

  private static LlmClient answering(String json) {
    return new LlmClient() {
      @Override
      public AdvisorSource source() {
        return AdvisorSource.GEMINI;
      }

      @Override
      public String completeJson(LlmPrompt prompt) {
        if (json == null) {
          throw new AiUnavailableException("down");
        }
        return json;
      }
    };
  }

  @Test
  void acceptsAFoodWithItsCategoryAndShelfLife() {
    FoodVerdict guava =
        new LlmFoodClassifier(
                answering(
                    "{\"food\":true,\"name\":\"Guayaba\",\"category\":\"FRUIT\",\"perishable\":true,\"shelfDays\":5,\"reason\":\"Tropical fruit\"}"))
            .classify("guayaba");

    assertTrue(guava.food());
    assertEquals("Guayaba", guava.name());
    assertEquals(FoodCategory.FRUIT, guava.category());
    assertEquals(5, guava.shelfDays());
  }

  @Test
  void refusesThingsThatAreNotFood() {
    FoodVerdict shoes =
        new LlmFoodClassifier(answering("{\"food\":false,\"reason\":\"Shoes are not eaten\"}"))
            .classify("zapatos");

    assertFalse(shoes.food());
    assertEquals("Shoes are not eaten", shoes.reason());
  }

  @Test
  void rejectsAnswersOutsideTheContract() {
    assertThrows(
        FoodCheckUnavailableException.class,
        () ->
            new LlmFoodClassifier(
                    answering(
                        "{\"food\":true,\"name\":\"X\",\"category\":\"SHOES\",\"perishable\":true,\"shelfDays\":5,\"reason\":\"r\"}"))
                .classify("x"));
    assertThrows(
        FoodCheckUnavailableException.class,
        () ->
            new LlmFoodClassifier(
                    answering(
                        "{\"food\":true,\"name\":\"X\",\"category\":\"FRUIT\",\"perishable\":true,\"shelfDays\":9000,\"reason\":\"r\"}"))
                .classify("x"));
    assertThrows(
        FoodCheckUnavailableException.class,
        () -> new LlmFoodClassifier(answering("{\"food\":\"yes\"}")).classify("x"));
  }

  @Test
  void anOutageOrOfflineModeNeverLetsUnknownThingsIn() {
    FoodCheckUnavailableException down =
        assertThrows(
            FoodCheckUnavailableException.class,
            () -> new LlmFoodClassifier(answering(null)).classify("lulo"));
    assertTrue(down.isProviderDown());
    assertThrows(
        FoodCheckUnavailableException.class, () -> new OfflineFoodClassifier().classify("lulo"));
  }
}
