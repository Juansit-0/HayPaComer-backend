package dev.haypacomer.agent.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class OfflineIntentsTest {

  static Stream<Arguments> phrases() {
    return Stream.of(
        Arguments.of("usé 200 g de arroz", "use_food", Map.of("food", "arroz", "grams", "200")),
        Arguments.of(
            "gasté medio kilo de pollo", "use_food", Map.of("food", "pollo", "grams", "500")),
        Arguments.of("I used 1,5 kg of rice", "use_food", Map.of("food", "rice", "grams", "1500")),
        Arguments.of("boté el tomate", "discard_food", Map.of("food", "tomate")),
        Arguments.of("threw away the yogurt", "discard_food", Map.of("food", "yogurt")),
        Arguments.of("abrí la leche", "mark_opened", Map.of("food", "leche")),
        Arguments.of(
            "guardé 1 kg de papa en la nevera",
            "stock_food",
            Map.of("food", "papa", "grams", "1000", "place", "nevera")),
        Arguments.of(
            "compré 2 kg de arroz", "stock_food", Map.of("food", "arroz", "grams", "2000")),
        Arguments.of(
            "agrega 500 g de arroz a la lista",
            "add_to_market",
            Map.of("food", "arroz", "grams", "500")),
        Arguments.of(
            "agrega tomate al mercado", "add_to_market", Map.of("food", "tomate", "grams", "500")),
        Arguments.of(
            "pon 1 kg de papa en la lista",
            "add_to_market",
            Map.of("food", "papa", "grams", "1000")),
        Arguments.of(
            "marca el arroz como comprado",
            "update_market",
            Map.of("food", "arroz", "action", "CHECK")),
        Arguments.of(
            "quita el tomate de la lista",
            "update_market",
            Map.of("food", "tomate", "action", "REMOVE")),
        Arguments.of(
            "desmarca la leche", "update_market", Map.of("food", "leche", "action", "UNCHECK")),
        Arguments.of("limpia lo comprado", "clear_checked_market", Map.of()),
        Arguments.of("presupuesto de 300000", "set_market_budget", Map.of("amount", "300000")),
        Arguments.of("presupuesto de 300.000", "set_market_budget", Map.of("amount", "300000")),
        Arguments.of("budget 1,5 million", "set_market_budget", Map.of("amount", "1500000")),
        Arguments.of(
            "¿qué me falta para arroz con pollo?",
            "missing_to_market",
            Map.of("recipe", "arroz con pollo")),
        Arguments.of(
            "qué necesito para la receta de arepas para 4",
            "missing_to_market",
            Map.of("recipe", "arepas", "servings", "4")),
        Arguments.of("pasa el plan al mercado", "plan_to_market", Map.of()),
        Arguments.of(
            "cocina arroz con pollo para 2",
            "start_cooking",
            Map.of("recipe", "arroz con pollo", "servings", "2")),
        Arguments.of("siguiente paso", "cooking_step", Map.of("action", "NEXT")),
        Arguments.of("Pausa", "cooking_step", Map.of("action", "PAUSE")),
        Arguments.of("sigue", "cooking_step", Map.of("action", "RESUME")),
        Arguments.of("cancela la receta", "cooking_step", Map.of("action", "ABANDON")),
        Arguments.of("deshaz lo último", "undo_last_change", Map.of()),
        Arguments.of("haz el plan de la semana", "generate_week_plan", Map.of()),
        Arguments.of(
            "arma el menú de la semana para 3", "generate_week_plan", Map.of("servings", "3")),
        Arguments.of("¿qué puedo cocinar?", "suggest_dishes", Map.of()),
        Arguments.of("what can I cook for 4?", "suggest_dishes", Map.of("servings", "4")),
        Arguments.of("¿qué recetas tengo?", "list_recipes", Map.of()),
        Arguments.of("¿en qué paso voy?", "view_cooking", Map.of()),
        Arguments.of("¿qué tengo en la nevera?", "query_inventory", Map.of()),
        Arguments.of("¿cuánto arroz queda?", "query_inventory", Map.of("food", "arroz")));
  }

  @ParameterizedTest
  @MethodSource("phrases")
  void commonPhrasesBecomeTheRightTool(String said, String tool, Map<String, String> arguments) {
    assertEquals(
        Optional.of(new OfflineIntents.Intent(tool, arguments)), OfflineIntents.match(said));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "hola",
        "¿qué se vence pronto?",
        "What do I buy to cook dinner?",
        "door open, temperature high, buy ice, cook, waste",
        "compré 2 tomates",
        "check the fridge temperature",
        "cook and buy",
        "cocina algo rico"
      })
  void questionsAndVagueCountsAreNotOrders(String said) {
    assertEquals(Optional.empty(), OfflineIntents.match(said));
  }
}
