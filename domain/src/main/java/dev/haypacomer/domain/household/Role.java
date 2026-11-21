package dev.haypacomer.domain.household;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
  OWNER(EnumSet.allOf(Permission.class)),
  MEMBER(EnumSet.complementOf(EnumSet.of(Permission.MANAGE_MEMBERS, Permission.DELETE_HOUSEHOLD))),
  GUEST(EnumSet.of(Permission.VIEW_HOUSEHOLD, Permission.MANAGE_OWN_ITEMS));

  private final Set<Permission> permissions;

  Role(Set<Permission> permissions) {
    this.permissions = Set.copyOf(permissions);
  }

  public Set<Permission> permissions() {
    return permissions;
  }

  public boolean grants(Permission permission) {
    return permissions.contains(permission);
  }
}
