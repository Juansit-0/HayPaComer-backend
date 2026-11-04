package dev.haypacomer.web.kitchen;

import dev.haypacomer.application.HayPaComerFacade.KitchenSnapshot;
import dev.haypacomer.application.fridge.FridgeLayout;
import dev.haypacomer.application.inventory.InventoryEntry;
import dev.haypacomer.domain.expiry.ExpirySource;
import dev.haypacomer.domain.fridge.FoodItem;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.fridge.Fridge;
import dev.haypacomer.domain.fridge.FridgeNode;
import dev.haypacomer.domain.fridge.Tray;
import dev.haypacomer.domain.fridge.Zone;
import dev.haypacomer.domain.inventory.FoodStatus;
import dev.haypacomer.domain.inventory.PrivateFoodProxy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class KitchenDtos {

  private KitchenDtos() {}

  record SetUpFridgeRequest(@NotBlank @Size(max = 60) String name, FridgeLayout layout) {}

  record NodeResponse(
      UUID id,
      String type,
      String kind,
      String name,
      BigDecimal grams,
      int items,
      List<NodeResponse> children) {

    static NodeResponse from(FridgeNode node) {
      return from(node, Set.of());
    }

    static NodeResponse from(FridgeNode node, Set<FoodItemId> hidden) {
      String type = node.getClass().getSimpleName().toUpperCase();
      if (node instanceof FoodItem item) {
        return hidden.contains(item.id())
            ? new NodeResponse(
                id(node),
                type,
                null,
                PrivateFoodProxy.HIDDEN.name(),
                BigDecimal.ZERO,
                node.itemCount(),
                List.of())
            : new NodeResponse(
                id(node), type, null, node.name(), node.totalGrams().value(), 1, List.of());
      }
      List<NodeResponse> children =
          node.children().stream().map(child -> from(child, hidden)).toList();
      return new NodeResponse(
          id(node),
          type,
          node instanceof Zone zone ? zone.kind().name() : null,
          node.name(),
          children.stream().map(NodeResponse::grams).reduce(BigDecimal.ZERO, BigDecimal::add),
          node.itemCount(),
          children);
    }

    private static UUID id(FridgeNode node) {
      return switch (node) {
        case Fridge fridge -> fridge.id().value();
        case Zone zone -> zone.id().value();
        case Tray tray -> tray.id().value();
        case FoodItem item -> item.id().value();
      };
    }
  }

  record InventoryItemResponse(
      UUID id,
      UUID fridgeId,
      UUID trayId,
      String name,
      BigDecimal grams,
      LocalDate expiresOn,
      ExpirySource expirySource,
      Double expiryConfidence,
      LocalDate openedOn,
      Set<FoodStatus> statuses,
      boolean edible,
      boolean usable,
      int rescuePriority) {

    static InventoryItemResponse from(InventoryEntry entry) {
      FoodItem item = entry.food().item();
      return new InventoryItemResponse(
          item.id().value(),
          entry.fridge().value(),
          entry.tray().value(),
          item.name(),
          item.quantity().value(),
          item.expiresOn().orElse(null),
          item.expirySource().orElse(null),
          item.expirySource().map(ExpirySource::confidence).orElse(null),
          item.openedOn().orElse(null),
          entry.food().statuses(),
          entry.food().isEdible(),
          entry.usable(),
          entry.food().rescuePriority());
    }
  }

  record SnapshotResponse(int items, BigDecimal totalGrams, int atRisk, int expired) {

    static SnapshotResponse from(KitchenSnapshot snapshot) {
      return new SnapshotResponse(
          snapshot.items(), snapshot.totalGrams().value(), snapshot.atRisk(), snapshot.expired());
    }
  }
}
