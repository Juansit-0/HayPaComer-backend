package dev.haypacomer.application.household;

/** Someone belongs to one home at a time: to make or join another, they leave or delete theirs. */
public final class AlreadyInHouseholdException extends RuntimeException {

  public AlreadyInHouseholdException() {
    super("You already belong to a household; leave it or delete it first");
  }
}
