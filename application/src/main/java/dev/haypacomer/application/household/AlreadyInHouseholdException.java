package dev.haypacomer.application.household;

public final class AlreadyInHouseholdException extends RuntimeException {

  public AlreadyInHouseholdException() {
    super("You already belong to a household; leave it or delete it first");
  }
}
