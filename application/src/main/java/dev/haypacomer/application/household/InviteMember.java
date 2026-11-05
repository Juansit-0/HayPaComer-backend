package dev.haypacomer.application.household;

import dev.haypacomer.application.auth.OpaqueTokens;
import dev.haypacomer.application.mail.EmailMessage;
import dev.haypacomer.application.mail.MailLinks;
import dev.haypacomer.application.port.EmailSender;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InvitationRepository;
import dev.haypacomer.application.port.UserRepository;
import dev.haypacomer.domain.household.Household;
import dev.haypacomer.domain.household.HouseholdId;
import dev.haypacomer.domain.household.Invitation;
import dev.haypacomer.domain.household.Permission;
import dev.haypacomer.domain.household.Role;
import dev.haypacomer.domain.identity.EmailAddress;
import dev.haypacomer.domain.identity.UserId;
import java.time.Clock;
import java.util.Objects;

public final class InviteMember {

  private final MemberHouseholds households;
  private final InvitationRepository invitations;
  private final UserRepository users;
  private final OpaqueTokens opaqueTokens;
  private final EmailSender email;
  private final MailLinks links;
  private final Clock clock;

  public InviteMember(
      HouseholdRepository households,
      InvitationRepository invitations,
      UserRepository users,
      OpaqueTokens opaqueTokens,
      EmailSender email,
      MailLinks links,
      Clock clock) {
    this.households = new MemberHouseholds(households);
    this.invitations = Objects.requireNonNull(invitations, "invitations");
    this.users = Objects.requireNonNull(users, "users");
    this.opaqueTokens = Objects.requireNonNull(opaqueTokens, "opaqueTokens");
    this.email = Objects.requireNonNull(email, "email");
    this.links = Objects.requireNonNull(links, "links");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  public SentInvitation invite(UserId actor, HouseholdId id, String emailAddress, Role role) {
    Household household = households.load(actor, id);
    household.requirePermission(actor, Permission.MANAGE_MEMBERS);
    boolean alreadyHere =
        users
            .findByEmail(new EmailAddress(emailAddress))
            .filter(user -> household.membershipOf(user.id()).isPresent())
            .isPresent();
    if (alreadyHere) {
      throw new IllegalStateException("That person is already in this household");
    }
    String raw = opaqueTokens.generate();
    Invitation invitation =
        Invitation.create(
            household.id(),
            new EmailAddress(emailAddress),
            role,
            opaqueTokens.hash(raw),
            actor,
            clock.instant());
    invitations.save(invitation);
    String link = links.to("join", raw);
    email.send(
        new EmailMessage(
            invitation.email(),
            "You are invited to " + household.name() + " on HayPaComer",
            "Join " + household.name() + " within 7 days: " + link,
            link));
    return new SentInvitation(invitation, link);
  }
}
