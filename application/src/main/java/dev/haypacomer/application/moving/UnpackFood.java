package dev.haypacomer.application.moving;

import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.inventory.CommandOutcome;
import dev.haypacomer.application.inventory.ExecuteInventoryCommand;
import dev.haypacomer.application.inventory.StockFoodCommand;
import dev.haypacomer.application.port.CarriedFood;
import dev.haypacomer.application.port.FoodItemPieces;
import dev.haypacomer.application.port.FridgeRepository;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.fridge.ZoneKind;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Membership;
import dev.haypacomer.domain.identity.UserId;
import dev.haypacomer.domain.quantity.Grams;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UnpackFood {

  private final GetHousehold households;
  private final FridgeRepository fridges;
  private final CarriedFood carried;
  private final ExecuteInventoryCommand commands;
  private final FoodItemPieces pieces;

  public UnpackFood(
      HouseholdRepository households,
      FridgeRepository fridges,
      CarriedFood carried,
      ExecuteInventoryCommand commands,
      FoodItemPieces pieces) {
    this.households = new GetHousehold(households);
    this.fridges = Objects.requireNonNull(fridges, "fridges");
    this.carried = Objects.requireNonNull(carried, "carried");
    this.commands = Objects.requireNonNull(commands, "commands");
    this.pieces = Objects.requireNonNull(pieces, "pieces");
  }

  public int unpack(UserId actor, HouseholdId id) {
    Household household = households.get(actor, id);
    List<Fridge> all = fridges.findByHousehold(id);
    if (all.isEmpty()) {
      return 0;
    }
    Fridge fridge = all.getFirst();
    Optional<Tray> tray = shelf(fridge);
    if (tray.isEmpty()) {
      return 0;
    }
    int unpacked = 0;
    for (Membership membership : household.memberships()) {
      for (CarriedItem item : carried.waiting(membership.user())) {
        try {
          CommandOutcome outcome;
          try {
            outcome = stock(membership.user(), id, fridge, tray.get(), item, true);
          } catch (RuntimeException dateNoLongerFits) {
            if (item.expiresOn() == null) {
              throw dateNoLongerFits;
            }
            outcome = stock(membership.user(), id, fridge, tray.get(), item, false);
          }
          if (item.pieceGrams() != null) {
            pieces.save(outcome.item(), item.pieceGrams());
          }
          carried.unpacked(item.id());
          unpacked++;
        } catch (RuntimeException notNow) {
        }
      }
    }
    return unpacked;
  }

  private CommandOutcome stock(
      UserId owner, HouseholdId id, Fridge fridge, Tray tray, CarriedItem item, boolean keepDate) {
    return commands.execute(
        owner,
        new StockFoodCommand(
            UUID.randomUUID(),
            id,
            fridge.id(),
            tray.id(),
            item.food(),
            Grams.of(item.grams()),
            Grams.ZERO,
            keepDate ? item.expiresOn() : null,
            item.visibility(),
            item.opened(),
            keepDate && item.expiresOn() != null ? item.expirySource() : null));
  }

  private static Optional<Tray> shelf(Fridge fridge) {
    return fridge.children().stream()
        .sorted(
            (left, right) ->
                Boolean.compare(left.kind() != ZoneKind.SHELF, right.kind() != ZoneKind.SHELF))
        .map(Zone::children)
        .flatMap(List::stream)
        .findFirst();
  }
}
