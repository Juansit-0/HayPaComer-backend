package dev.haypacomer.application.household;

import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.identity.UserId;

final class JoinCodes {

  private JoinCodes() {}

  static void requireManager(HouseholdRepository households, UserId actor, HouseholdId id) {
    Household household = new MemberHouseholds(households).load(actor, id);
    household.requirePermission(actor, Permission.MANAGE_MEMBERS);
  }
}
