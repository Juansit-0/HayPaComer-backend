package dev.haypacomer.agent.runtime;

import dev.haypacomer.domain.quantity.ConversionFactors;
import dev.haypacomer.domain.quantity.InvalidQuantityException;
import dev.haypacomer.domain.quantity.QuantityParser;
import dev.haypacomer.domain.quantity.UnconvertibleQuantityException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OfflineIntents {

  public static final String DEFAULT_MARKET_GRAMS = "500";

  public record Intent(String tool, Map<String, String> arguments) {

    public Intent {
      Objects.requireNonNull(tool, "tool");
      arguments = Map.copyOf(arguments);
    }
  }

  private record Rule(Pattern pattern, Function<Matcher, Optional<Intent>> intent) {}

  private static final String UNIT =
      "(?:kilogramos?|kilograms?|kilos?|kgs?|gramos?|grams?|grs?|g|libras?|lbs?|pounds?"
          + "|litros?|liters?|litres?|lts?|l|mililitros?|milliliters?|ml|onzas?|oz)(?![a-z])";
  private static final String QTY =
      "(?:\\d+(?:[.,]\\d+)*(?:/\\d+)?\\s*"
          + UNIT
          + "?|(?:medio|media|un cuarto de|un|una|half a|half|a|one)\\s+"
          + UNIT
          + ")";
  private static final String LEAD = "^(?:(?:ya|yo|i|we|acabo de|acabamos de|hoy)\\s+)*";
  private static final String FOOD_END =
      "(?:\\s+(?:en|in|on|into|al|a la)\\s+(?:el |la |the |my |mi )?(.+))?$";

  private static final Pattern ARTICLE =
      Pattern.compile(
          "^(?:de|del|of|el|la|los|las|lo|un|una|unos|unas|the|a|an|some|mi|mis|my)\\s+");
  private static final Pattern QTY_PREFIX = Pattern.compile("^" + QTY + "\\s+(?:de\\s+|of\\s+)?");
  private static final Pattern FRIDGE_TAIL =
      Pattern.compile(
          "\\s+(?:de|del|from|en|in)\\s+(?:la |el |the |my |mi )?"
              + "(?:nevera|fridge|refri|refrigerador|congelador|freezer)$");
  private static final Pattern MARKET_PLACE =
      Pattern.compile("^(?:lista|mercado|list|market|shopping list|compras)\\b");
  private static final Pattern SERVINGS_TAIL =
      Pattern.compile(
          "\\s+(?:para|for)\\s+(\\d{1,2})(?:\\s+(?:personas?|people|persons?|porciones"
              + "|servings?|comensales))?$");
  private static final Pattern SERVINGS = Pattern.compile("\\b(?:para|for)\\s+(\\d{1,2})\\b");
  private static final Pattern NOT_A_RECIPE =
      Pattern.compile(
          "^(?:and|y|o|or|algo|something|anything|dinner|lunch|breakfast|cena|almuerzo|desayuno"
              + "|for|para|con|with|tonight|hoy)\\b");
  private static final Pattern RECIPE_LEAD =
      Pattern.compile(
          "^(?:(?:la|el|una|un|the|a|mi|my)\\s+)?(?:receta\\s+(?:de\\s+|del\\s+)?|recipe\\s+"
              + "(?:for\\s+|of\\s+)?)?");
  private static final ConversionFactors LIQUID_AS_WATER = ConversionFactors.withDensity("1");
  private static final BigDecimal MAX_GRAMS = BigDecimal.valueOf(100_000);
  private static final BigDecimal MIN_BARE_GRAMS = BigDecimal.valueOf(50);

  private static final List<Rule> RULES =
      List.of(
          rule(
              "^(?:deshaz|deshacer|deshaga|revierte|revertir|undo)\\b",
              m -> of("undo_last_change")),
          rule(
              "^(?:(?:listo|ok|ya)[, ]+)?(?:siguiente(?: paso)?|paso siguiente|next(?: step)?)$",
              m -> step("NEXT")),
          rule(
              "^(?:pausa|pausar|pause)(?: (?:la |the )?(?:receta|coccion|recipe|cooking))?$",
              m -> step("PAUSE")),
          rule(
              "^(?:sigue|seguir|continua|continuar|reanuda|reanudar|resume|continue)"
                  + "(?: (?:la |the )?(?:receta|coccion|recipe|cooking))?$",
              m -> step("RESUME")),
          rule(
              "^(?:cancela|cancelar|abandona|abandonar|cancel|abandon|stop)(?: la| the)?"
                  + " (?:receta|coccion|recipe|cooking)$",
              m -> step("ABANDON")),
          rule(
              "\\b(?:en (?:que|cual) paso (?:voy|estoy|vamos)|que paso (?:sigue|va|toca)"
                  + "|what step|which step)\\b",
              m -> of("view_cooking")),
          rule(
              "\\b(?:(?:que|cuales) recetas (?:tengo|tenemos|hay|guarde)|mis recetas"
                  + "|recetas guardadas|what recipes|my recipes|list (?:my |the )?recipes)\\b",
              m -> of("list_recipes")),
          rule(
              "\\b(?:(?:que|lo que) (?:me |nos )?faltan? para|que necesito (?:comprar )?para"
                  + "|what (?:do i|do we) (?:need|miss) (?:for|to make|to cook))"
                  + "\\s+(?:hacer\\s+|preparar\\s+|cocinar\\s+)?(.+)$",
              m -> recipe("missing_to_market", m.group(1))),
          rule(
              "\\bplan\\b.*\\b(?:mercado|lista|market|list|shopping|compras)\\b",
              m -> of("plan_to_market")),
          rule(
              "^(?:haz|hazme|hacer|genera|generar|arma|armar|armame|crea|crear|planea|planear"
                  + "|planifica|make|create|generate|plan)\\b.*\\b(?:plan|menu|semana|week)\\b.*$",
              m -> servings("generate_week_plan", m)),
          rule(
              "\\b(?:que (?:puedo|podemos) (?:cocinar|hacer|preparar|comer)"
                  + "|que (?:cocino|hago|preparo)(?: hoy| de (?:comer|almuerzo|cena|desayuno))?"
                  + "|what (?:can|should) (?:i|we) (?:cook|make|eat)|suggest|sugiereme|sugiere"
                  + "|recomiendame)\\b.*$",
              m -> servings("suggest_dishes", m)),
          rule(
              "^(?:vamos a )?(?:cocina|cocinar|cocinemos|prepara|preparar|preparemos|empieza"
                  + "|empezar|inicia|iniciar|cook|start cooking|lets cook|let's cook)\\s+(.+)$",
              m ->
                  NOT_A_RECIPE.matcher(m.group(1)).find()
                      ? Optional.empty()
                      : recipe("start_cooking", m.group(1))),
          rule(
              "\\b(?:limpia|limpiar|borra|borrar|quita|quitar|saca|sacar|elimina|eliminar|vacia"
                  + "|vaciar|clear|remove|empty)\\b.*\\b(?:comprados?|compradas?|carrito|checked"
                  + "|bought|cart|marcados?)\\b",
              m -> of("clear_checked_market")),
          rule(
              "\\b(?:presupuesto|budget)\\b\\D*?(\\d[\\d.,]*)\\s*"
                  + "(mil|k|millones|millon|millions|million)?(?![a-z])",
              OfflineIntents::budget),
          rule(
              "^(?:desmarca|desmarcar|uncheck|unmark)\\s+(.+?)"
                  + "(?:\\s+(?:de|from|on)\\s+(?:la |the |mi |my )?(?:lista|list|mercado))?$",
              m -> market("UNCHECK", m.group(1))),
          rule(
              "^(?:marca|marcar|mark)\\s+(.+?)\\s+(?:como|as)\\s+"
                  + "(?:comprad[oa]s?|bought|purchased|listos?|done)$",
              m -> market("CHECK", m.group(1))),
          rule(
              "^(?:tacha|tachar|chulea|chulear|check off)\\s+(.+?)"
                  + "(?:\\s+(?:de|from|on)\\s+(?:la |the |mi |my )?(?:lista|list|mercado))?$",
              m -> market("CHECK", m.group(1))),
          rule(
              "^(?:quita|quitar|quitame|saca|sacar|borra|borrar|elimina|eliminar|remove|delete"
                  + "|take)\\s+(.+?)\\s+(?:de|del|from|off)\\s+(?:la |el |the |mi |my )?"
                  + "(?:lista|mercado|list|market|shopping list)(?: de (?:compras|mercado))?$",
              m -> market("REMOVE", m.group(1))),
          rule(
              LEAD
                  + "(?:guarde|guardamos|guardo|guarda|meti|metimos|meto|mete|puse|pusimos|pon"
                  + "|compre|compramos|traje|trajimos|stocked|stock|bought|stored|store|put"
                  + "|added)\\s+("
                  + QTY
                  + ")\\s+(?:de\\s+|del\\s+|of\\s+)?(.+?)"
                  + FOOD_END,
              OfflineIntents::stock),
          rule(
              LEAD
                  + "(?:use|usamos|uso|gaste|gastamos|gasto|consumi|consumimos|consumed|comi"
                  + "|comimos|me comi|nos comimos|saque|sacamos|used|ate|took|spent)\\s+("
                  + QTY
                  + ")\\s+(?:de\\s+|del\\s+|of\\s+)?(.+)$",
              m ->
                  food(m.group(2))
                      .flatMap(food -> grams(m.group(1)).map(grams -> use(food, grams)))),
          rule(
              LEAD
                  + "(?:bote|botamos|tire|tiramos|deseche|desechamos|bota|tira|desecha|threw away"
                  + "|throw away|threw out|throw out|tossed|discarded|discard)\\s+(.+?)"
                  + "(?:\\s+(?:a la basura|in the trash|away|out))?$",
              m -> food(m.group(1)).map(food -> new Intent("discard_food", Map.of("food", food)))),
          rule(
              LEAD + "(?:abri|abrimos|destape|destapamos|opened)\\s+(.+)$",
              m -> food(m.group(1)).map(food -> new Intent("mark_opened", Map.of("food", food)))),
          rule(
              "^(?:(?:puedes|podrias|quiero|necesito|hay que|can you|could you|i need to|i want to"
                  + "|we need to)\\s+)?(?:agrega|agregar|agregame|anade|anadir|anota|anotar|apunta"
                  + "|pon|compra|comprar"
                  + "|add|buy)\\s+(?:("
                  + QTY
                  + ")\\s+(?:de\\s+|of\\s+)?)?(.+?)(?:\\s+(?:al|a la|en el|en la|to the|to my|to)"
                  + "\\s+.*)?$",
              OfflineIntents::addToMarket),
          rule(
              "\\bcuant[oa]s? (.+?) (?:tengo|tenemos|hay|queda|quedan)\\b"
                  + "|\\bhow much (.+?) (?:do i have|do we have|is left|left)\\b",
              m ->
                  food(m.group(1) != null ? m.group(1) : m.group(2))
                      .map(food -> new Intent("query_inventory", Map.of("food", food)))),
          rule(
              "\\b(?:que (?:hay|tengo|tenemos|queda|quedan) en (?:la |mi )?(?:nevera|refri)"
                  + "|what(?:s| is| do i have) in (?:the |my )?fridge)\\b",
              m -> of("query_inventory")));

  private OfflineIntents() {}

  public static Optional<Intent> match(String text) {
    String said = normalize(text);
    for (Rule rule : RULES) {
      Matcher matcher = rule.pattern().matcher(said);
      if (matcher.find()) {
        Optional<Intent> intent = rule.intent().apply(matcher);
        if (intent.isPresent()) {
          return intent;
        }
      }
    }
    return Optional.empty();
  }

  static String normalize(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replace('’', '\'')
        .replaceAll("[¿¡!?;:\"]", " ")
        .replaceAll("\\s+", " ")
        .strip()
        .replaceAll("^(?:por favor|please|oye|hey|chef)[, ]+", "")
        .replaceAll("[, ]+(?:por favor|please)$", "")
        .replaceAll("[.,]+$", "")
        .strip();
  }

  private static Rule rule(String regex, Function<Matcher, Optional<Intent>> intent) {
    return new Rule(Pattern.compile(regex), intent);
  }

  private static Optional<Intent> of(String tool) {
    return Optional.of(new Intent(tool, Map.of()));
  }

  private static Optional<Intent> step(String action) {
    return Optional.of(new Intent("cooking_step", Map.of("action", action)));
  }

  private static Intent use(String food, String grams) {
    return new Intent("use_food", Map.of("food", food, "grams", grams));
  }

  private static Optional<Intent> servings(String tool, Matcher matcher) {
    Matcher servings = SERVINGS.matcher(matcher.group());
    return Optional.of(
        new Intent(tool, servings.find() ? Map.of("servings", servings.group(1)) : Map.of()));
  }

  private static Optional<Intent> recipe(String tool, String rest) {
    Map<String, String> arguments = new HashMap<>();
    String name = rest.strip();
    Matcher servings = SERVINGS_TAIL.matcher(name);
    if (servings.find()) {
      arguments.put("servings", servings.group(1));
      name = name.substring(0, servings.start());
    }
    name = RECIPE_LEAD.matcher(name.strip()).replaceFirst("").strip();
    if (name.isEmpty() || name.contains(",")) {
      return Optional.empty();
    }
    arguments.put("recipe", name);
    return Optional.of(new Intent(tool, arguments));
  }

  private static Optional<Intent> market(String action, String text) {
    return food(text)
        .map(food -> new Intent("update_market", Map.of("food", food, "action", action)));
  }

  private static Optional<Intent> stock(Matcher matcher) {
    String place = matcher.group(3);
    if (place != null && MARKET_PLACE.matcher(place).find()) {
      return Optional.empty();
    }
    Optional<String> food = food(matcher.group(2));
    Optional<String> grams = grams(matcher.group(1));
    if (food.isEmpty() || grams.isEmpty()) {
      return Optional.empty();
    }
    Map<String, String> arguments = new HashMap<>();
    arguments.put("food", food.get());
    arguments.put("grams", grams.get());
    if (place != null) {
      arguments.put("place", place.strip());
    }
    return Optional.of(new Intent("stock_food", arguments));
  }

  private static Optional<Intent> addToMarket(Matcher matcher) {
    Optional<String> grams =
        matcher.group(1) == null ? Optional.of(DEFAULT_MARKET_GRAMS) : grams(matcher.group(1));
    return food(matcher.group(2))
        .flatMap(
            food ->
                grams.map(
                    value -> new Intent("add_to_market", Map.of("food", food, "grams", value))));
  }

  private static Optional<Intent> budget(Matcher matcher) {
    String number = matcher.group(1).replaceAll("[.,]+$", "");
    String scale = matcher.group(2);
    BigDecimal amount;
    if (scale == null) {
      amount = new BigDecimal(number.replaceAll("[.,]", ""));
    } else {
      String decimal = number.replace(',', '.');
      amount =
          new BigDecimal(decimal.matches("\\d+(?:\\.\\d{3})+") ? decimal.replace(".", "") : decimal)
              .multiply(
                  scale.startsWith("mill")
                      ? BigDecimal.valueOf(1_000_000)
                      : BigDecimal.valueOf(1_000));
    }
    BigDecimal whole = amount.setScale(0, RoundingMode.HALF_UP);
    return whole.signum() > 0
        ? Optional.of(new Intent("set_market_budget", Map.of("amount", whole.toPlainString())))
        : Optional.empty();
  }

  static Optional<String> food(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    String food = FRIDGE_TAIL.matcher(raw.strip()).replaceFirst("");
    food = QTY_PREFIX.matcher(food).replaceFirst("");
    String previous;
    do {
      previous = food;
      food = ARTICLE.matcher(food).replaceFirst("").strip();
    } while (!food.equals(previous));
    if (food.isEmpty() || food.contains(",") || food.split(" ").length > 5) {
      return Optional.empty();
    }
    return Optional.of(food);
  }

  static Optional<String> grams(String quantity) {
    String text =
        quantity
            .strip()
            .replaceFirst("^(?:medio|media|half a|half)\\s+", "1/2 ")
            .replaceFirst("^un cuarto de\\s+", "1/4 ")
            .replaceFirst("^(?:un|una|a|one)\\s+", "1 ");
    if (text.matches("[\\d.,/]+")) {
      if (new BigDecimal(text.replace(',', '.').replaceAll("/.*", "")).compareTo(MIN_BARE_GRAMS)
          < 0) {
        return Optional.empty();
      }
      text = text + " g";
    }
    try {
      BigDecimal grams =
          QuantityParser.parse(text)
              .interpret(LIQUID_AS_WATER)
              .value()
              .setScale(0, RoundingMode.HALF_UP);
      return grams.signum() > 0 && grams.compareTo(MAX_GRAMS) <= 0
          ? Optional.of(grams.toPlainString())
          : Optional.empty();
    } catch (InvalidQuantityException | UnconvertibleQuantityException unreadable) {
      return Optional.empty();
    }
  }
}
