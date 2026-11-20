package dev.haypacomer.agent.supervisor;

import dev.haypacomer.agent.runtime.AgentTask;
import dev.haypacomer.agent.runtime.OfflineIntents;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class KeywordRouter implements SpecialistRouter {

  public static final int MAX_SPECIALISTS = 2;

  private static final Map<Specialist, List<String>> KEYWORDS = new LinkedHashMap<>();

  static {
    KEYWORDS.put(
        Specialist.COLD,
        List.of(
            "temperature",
            "cold chain",
            "warm",
            "door open",
            "open door",
            "door was open",
            "door left",
            "fridge broke",
            "not cooling",
            "degrees",
            "power",
            "temperatura",
            "cadena de frio",
            "caliente",
            "calor",
            "grados",
            "puerta abierta",
            "abrieron la puerta",
            "dejaron la puerta",
            "dejaste la puerta",
            "no enfria",
            "se fue la luz",
            "apagon"));
    KEYWORDS.put(
        Specialist.MARKET,
        List.of(
            "buy",
            "market",
            "shopping",
            "groceries",
            "store",
            "compr",
            "mercado",
            "lista",
            "tienda",
            "surtir",
            "presupuesto",
            "budget",
            "carrito",
            "cart"));
    KEYWORDS.put(
        Specialist.COACH,
        List.of(
            "waste",
            "save",
            "habit",
            "tip",
            "throw away",
            "desperdici",
            "botar",
            "botando",
            "boto ",
            "botamos",
            "ahorrar",
            "habito",
            "consejo"));
    KEYWORDS.put(
        Specialist.CHEF,
        List.of(
            "cook",
            "recipe",
            "dinner",
            "lunch",
            "breakfast",
            "eat",
            "meal",
            "cocinar",
            "receta",
            "cena",
            "almuerzo",
            "desayuno",
            "comer",
            "comida",
            "cocin",
            "inventario",
            "inventory",
            "paso",
            "step"));
  }

  @Override
  public List<Specialist> route(String goal) {
    String asked = AgentTask.asked(goal);
    Optional<Specialist> owner =
        OfflineIntents.match(asked)
            .flatMap(
                intent ->
                    Specialist.ALL.stream()
                        .filter(specialist -> specialist.tools().contains(intent.tool()))
                        .findFirst());
    if (owner.isPresent()) {
      return List.of(owner.get());
    }
    String text = normalize(asked);
    List<Specialist> chosen = new ArrayList<>();
    KEYWORDS.forEach(
        (specialist, words) -> {
          if (chosen.size() < MAX_SPECIALISTS && words.stream().anyMatch(text::contains)) {
            chosen.add(specialist);
          }
        });
    return chosen.isEmpty() ? List.of(Specialist.CHEF) : List.copyOf(chosen);
  }

  private static String normalize(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }
}
