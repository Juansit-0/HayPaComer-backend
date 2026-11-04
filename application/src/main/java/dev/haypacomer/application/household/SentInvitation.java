package dev.haypacomer.application.household;

import dev.haypacomer.domain.household.Invitation;
import java.util.Objects;

public record SentInvitation(Invitation invitation, String link) {

  public SentInvitation {
    Objects.requireNonNull(invitation, "invitation");
    Objects.requireNonNull(link, "link");
  }
}
