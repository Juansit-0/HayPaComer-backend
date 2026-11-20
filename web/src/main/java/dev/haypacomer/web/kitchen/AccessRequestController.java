package dev.haypacomer.web.kitchen;

import dev.haypacomer.application.inventory.AnswerFoodRequest;
import dev.haypacomer.application.inventory.AskForFood;
import dev.haypacomer.application.inventory.FoodRequestView;
import dev.haypacomer.application.inventory.ListFoodRequests;
import dev.haypacomer.domain.fridge.FoodItemId;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.web.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessRequestController {

  private final AskForFood askForFood;
  private final ListFoodRequests listRequests;
  private final AnswerFoodRequest answerRequest;

  public AccessRequestController(
      AskForFood askForFood, ListFoodRequests listRequests, AnswerFoodRequest answerRequest) {
    this.askForFood = askForFood;
    this.listRequests = listRequests;
    this.answerRequest = answerRequest;
  }

  @GetMapping("/api/v1/households/{householdId}/access-requests")
  List<FoodRequestView> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId) {
    return listRequests.list(CurrentUser.of(jwt), new HouseholdId(householdId));
  }

  @PostMapping("/api/v1/households/{householdId}/items/{itemId}/access-requests")
  @ResponseStatus(HttpStatus.CREATED)
  FoodRequestView ask(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID householdId, @PathVariable UUID itemId) {
    return askForFood.ask(
        CurrentUser.of(jwt), new HouseholdId(householdId), new FoodItemId(itemId));
  }

  @PostMapping("/api/v1/households/{householdId}/access-requests/{requestId}/approve")
  FoodRequestView approve(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @PathVariable UUID requestId) {
    return answerRequest.answer(CurrentUser.of(jwt), new HouseholdId(householdId), requestId, true);
  }

  @PostMapping("/api/v1/households/{householdId}/access-requests/{requestId}/deny")
  FoodRequestView deny(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID householdId,
      @PathVariable UUID requestId) {
    return answerRequest.answer(
        CurrentUser.of(jwt), new HouseholdId(householdId), requestId, false);
  }
}
