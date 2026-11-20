package dev.haypacomer.ai.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haypacomer.ai.offline.OfflineFoodDensityEstimator;
import dev.haypacomer.application.ai.AdvisorSource;
import dev.haypacomer.application.ai.FoodCheckUnavailableException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LlmFoodDensityEstimatorTest {

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

  private static Optional<BigDecimal> estimate(String json) {
    return new LlmFoodDensityEstimator(answering(json)).gramsPerMilliliter("Milk");
  }

  @Test
  void readsAConfidentDensity() {
    assertEquals(
        Optional.of(new BigDecimal("1.03")),
        estimate("{\"gramsPerMilliliter\":1.03,\"confident\":true}"));
    assertEquals("gemini", new LlmFoodDensityEstimator(answering("{}")).provider());
  }

  @Test
  void saysNothingWhenNotConfidentOrNotANumber() {
    assertTrue(estimate("{\"gramsPerMilliliter\":1.03,\"confident\":false}").isEmpty());
    assertTrue(estimate("{\"gramsPerMilliliter\":\"heavy\",\"confident\":true}").isEmpty());
    assertTrue(estimate("{\"confident\":true}").isEmpty());
  }

  @Test
  void rejectsAnswersOutsideTheContract() {
    FoodCheckUnavailableException invalid =
        assertThrows(
            FoodCheckUnavailableException.class,
            () -> estimate("{\"gramsPerMilliliter\":1.03,\"confident\":\"yes\"}"));
    assertFalse(invalid.isProviderDown());
    assertThrows(FoodCheckUnavailableException.class, () -> estimate("not json"));
  }

  @Test
  void anOutageIsReportedAndOfflineModeSaysNothing() {
    assertTrue(
        assertThrows(FoodCheckUnavailableException.class, () -> estimate(null)).isProviderDown());
    OfflineFoodDensityEstimator offline = new OfflineFoodDensityEstimator();
    assertEquals("offline", offline.provider());
    assertTrue(offline.gramsPerMilliliter("Milk").isEmpty());
  }
}
