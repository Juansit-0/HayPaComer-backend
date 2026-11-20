package dev.haypacomer.agent.kitchen;

import dev.haypacomer.agent.runtime.AgentTool;
import dev.haypacomer.agent.runtime.Observation;
import dev.haypacomer.agent.runtime.ToolInvocation;
import dev.haypacomer.agent.tools.ParameterSpec;
import dev.haypacomer.agent.tools.ParameterType;
import dev.haypacomer.agent.tools.ToolSpec;
import dev.haypacomer.application.fridge.ListFridges;
import dev.haypacomer.application.inventory.CommandOutcome;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.StockFoodCommand;
import dev.haypacomer.application.port.FoodCatalogRepository;
import dev.haypacomer.domain.food.FoodMetadata;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.fridge.ZoneKind;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.inventory.Visibility;
import dev.haypacomer.domain.quantity.Grams;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StockFoodTool implements AgentTool {

  public static final ToolSpec SPEC =
      ToolSpec.write(
          "stock_food",
          "Propose putting food the person brought home into the fridge after they confirm it;"
              + " the expiry is estimated from the shelf life rules.",
          Permission.EDIT_INVENTORY,
          List.of(
              ParameterSpec.required("food", ParameterType.TEXT, "Catalog food name"),
              ParameterSpec.required("grams", ParameterType.GRAMS, "Grams put away"),
              ParameterSpec.optional(
                  "place", ParameterType.TEXT, "Shelf, zone, or fridge name; default a shelf")));

  private static final Set<String> ANY_SHELF =
      Set.of("nevera", "fridge", "refri", "refrigerador", "shelf", "shelves", "estante", "repisa");
  private static final Map<String, ZoneKind> KINDS =
      Map.of(
          "door", ZoneKind.DOOR,
          "puerta", ZoneKind.DOOR,
          "freezer", ZoneKind.FREEZER,
          "congelador", ZoneKind.FREEZER,
          "drawer", ZoneKind.DRAWER,
          "cajon", ZoneKind.DRAWER,
          "verduras", ZoneKind.DRAWER);

  private record Place(Fridge fridge, Zone zone, Tray tray) {

    String label() {
      return fridge.name() + ", " + zone.name() + " " + tray.name();
    }
  }

  private final ExecuteInventoryCommand commands;
  private final FoodCatalogRepository catalog;
  private final ListFridges fridges;

  public StockFoodTool(
      ExecuteInventoryCommand commands, FoodCatalogRepository catalog, ListFridges fridges) {
    this.commands = commands;
    this.catalog = catalog;
    this.fridges = fridges;
  }

  @Override
  public ToolSpec spec() {
    return SPEC;
  }

  @Override
  public Optional<String> problem(ToolInvocation invocation) {
    String food = invocation.arguments().get("food");
    if (KitchenFoods.resolve(catalog, food).isEmpty()) {
      return Optional.of(KitchenFoods.unknown(food));
    }
    List<Fridge> all = fridges.list(invocation.user(), invocation.household());
    if (all.isEmpty()) {
      return Optional.of("The household has no fridge yet");
    }
    if (place(invocation).isPresent()) {
      return Optional.empty();
    }
    return Optional.of(
        "No place called "
            + invocation.arguments().get("place").strip()
            + "; places: "
            + places(all).stream().map(Place::label).collect(Collectors.joining("; ")));
  }

  @Override
  public String describe(ToolInvocation invocation) {
    return "Put "
        + grams(invocation)
        + " of "
        + food(invocation).name()
        + " in "
        + place(invocation).map(Place::label).orElse("the fridge");
  }

  @Override
  public Observation invoke(ToolInvocation invocation) {
    Place place = place(invocation).orElseThrow();
    FoodMetadata food = food(invocation);
    CommandOutcome outcome =
        commands.execute(
            invocation.user(),
            new StockFoodCommand(
                UUID.randomUUID(),
                invocation.household(),
                place.fridge().id(),
                place.tray().id(),
                food.name(),
                grams(invocation),
                Grams.ZERO,
                null,
                Visibility.SHARED,
                false,
                null));
    Optional<String> expiry =
        fridges.list(invocation.user(), invocation.household()).stream()
            .flatMap(fridge -> fridge.findItem(outcome.item()).stream())
            .findFirst()
            .flatMap(item -> item.expiresOn())
            .map(date -> "; expires " + date);
    return Observation.of(
        SPEC.name(),
        "Stocked "
            + outcome.remaining()
            + " of "
            + food.name()
            + " in "
            + place.label()
            + expiry.orElse(""));
  }

  private FoodMetadata food(ToolInvocation invocation) {
    return KitchenFoods.resolve(catalog, invocation.arguments().get("food")).orElseThrow();
  }

  private static Grams grams(ToolInvocation invocation) {
    return Grams.of(invocation.arguments().get("grams").strip());
  }

  private Optional<Place> place(ToolInvocation invocation) {
    List<Place> all = places(fridges.list(invocation.user(), invocation.household()));
    String wanted = normalize(invocation.arguments().getOrDefault("place", ""));
    if (wanted.isEmpty() || ANY_SHELF.contains(wanted)) {
      return all.stream()
          .filter(place -> place.zone().kind() == ZoneKind.SHELF)
          .findFirst()
          .or(() -> all.stream().findFirst());
    }
    ZoneKind kind = KINDS.get(wanted);
    if (kind != null) {
      return all.stream().filter(place -> place.zone().kind() == kind).findFirst();
    }
    return all.stream()
        .filter(place -> normalize(place.tray().name()).equals(wanted))
        .findFirst()
        .or(
            () ->
                all.stream()
                    .filter(place -> normalize(place.zone().name()).equals(wanted))
                    .findFirst())
        .or(
            () ->
                all.stream()
                    .filter(place -> normalize(place.fridge().name()).equals(wanted))
                    .findFirst())
        .or(
            () ->
                all.stream()
                    .filter(place -> normalize(place.label()).contains(wanted))
                    .findFirst());
  }

  private static List<Place> places(List<Fridge> fridges) {
    return fridges.stream()
        .flatMap(
            fridge ->
                fridge.children().stream()
                    .flatMap(
                        zone ->
                            zone.children().stream().map(tray -> new Place(fridge, zone, tray))))
        .toList();
  }

  private static String normalize(String text) {
    return Normalizer.normalize(text.strip(), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }
}
