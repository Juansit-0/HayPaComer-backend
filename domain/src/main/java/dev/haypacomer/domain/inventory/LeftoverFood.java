package dev.haypacomer.domain.inventory;

import java.util.Set;

public final class LeftoverFood extends StockedFoodDecorator {

  public static final String FOOD_KEY = "leftovers";

  private static final int RESCUE_BONUS = 1;

  public LeftoverFood(StockedFood wrapped) {
    super(wrapped);
  }

  public static StockedFood markIfLeftover(StockedFood food) {
    return FOOD_KEY.equals(food.item().food().key()) ? new LeftoverFood(food) : food;
  }

  @Override
  public Set<FoodStatus> statuses() {
    return withStatus(FoodStatus.LEFTOVER);
  }

  @Override
  public int rescuePriority() {
    return wrapped().rescuePriority() + RESCUE_BONUS;
  }
}
