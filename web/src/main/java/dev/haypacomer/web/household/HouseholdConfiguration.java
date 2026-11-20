package dev.haypacomer.web.household;

import dev.haypacomer.application.auth.OpaqueTokens;
import dev.haypacomer.application.household.AcceptInvitation;
import dev.haypacomer.application.household.CancelInvitation;
import dev.haypacomer.application.household.ChangeMemberRole;
import dev.haypacomer.application.household.CreateHousehold;
import dev.haypacomer.application.household.CreateJoinCode;
import dev.haypacomer.application.household.DeleteHousehold;
import dev.haypacomer.application.household.GetHousehold;
import dev.haypacomer.application.household.InviteMember;
import dev.haypacomer.application.household.JoinHouseholdWithCode;
import dev.haypacomer.application.household.ListHouseholds;
import dev.haypacomer.application.household.ListInvitations;
import dev.haypacomer.application.household.RemoveMember;
import dev.haypacomer.application.household.RevokeJoinCode;
import dev.haypacomer.application.household.TransferOwnership;
import dev.haypacomer.application.household.UpdateHousehold;
import dev.haypacomer.application.household.ViewJoinCode;
import dev.haypacomer.application.inventory.FoodAccessGuard;
import dev.haypacomer.application.mail.MailLinks;
import dev.haypacomer.application.moving.PackUpHouseholdFood;
import dev.haypacomer.application.moving.PackUpMemberFood;
import dev.haypacomer.application.port.CarriedFood;
import dev.haypacomer.application.port.EmailSender;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.InvitationRepository;
import dev.haypacomer.application.port.JoinCodeRepository;
import dev.haypacomer.application.port.UnitOfWork;
import dev.haypacomer.application.port.UserRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HouseholdConfiguration {

  @Bean
  CreateHousehold createHousehold(HouseholdRepository households, Clock clock) {
    return new CreateHousehold(households, clock);
  }

  @Bean
  DeleteHousehold deleteHousehold(HouseholdRepository households) {
    return new DeleteHousehold(households);
  }

  @Bean
  ListHouseholds listHouseholds(HouseholdRepository households) {
    return new ListHouseholds(households);
  }

  @Bean
  GetHousehold getHousehold(HouseholdRepository households) {
    return new GetHousehold(households);
  }

  @Bean
  UpdateHousehold updateHousehold(HouseholdRepository households) {
    return new UpdateHousehold(households);
  }

  @Bean
  ChangeMemberRole changeMemberRole(HouseholdRepository households, UnitOfWork unitOfWork) {
    return new ChangeMemberRole(households, unitOfWork);
  }

  @Bean
  RemoveMember removeMember(HouseholdRepository households, UnitOfWork unitOfWork) {
    return new RemoveMember(households, unitOfWork);
  }

  @Bean
  TransferOwnership transferOwnership(HouseholdRepository households, UnitOfWork unitOfWork) {
    return new TransferOwnership(households, unitOfWork);
  }

  @Bean
  InviteMember inviteMember(
      HouseholdRepository households,
      InvitationRepository invitations,
      UserRepository users,
      OpaqueTokens opaqueTokens,
      EmailSender email,
      MailLinks links,
      Clock clock) {
    return new InviteMember(households, invitations, users, opaqueTokens, email, links, clock);
  }

  @Bean
  ListInvitations listInvitations(
      HouseholdRepository households, InvitationRepository invitations, Clock clock) {
    return new ListInvitations(households, invitations, clock);
  }

  @Bean
  CancelInvitation cancelInvitation(
      HouseholdRepository households, InvitationRepository invitations) {
    return new CancelInvitation(households, invitations);
  }

  @Bean
  AcceptInvitation acceptInvitation(
      InvitationRepository invitations,
      HouseholdRepository households,
      UserRepository users,
      OpaqueTokens opaqueTokens,
      UnitOfWork unitOfWork,
      Clock clock) {
    return new AcceptInvitation(invitations, households, users, opaqueTokens, unitOfWork, clock);
  }

  @Bean
  CreateJoinCode createJoinCode(
      HouseholdRepository households, JoinCodeRepository codes, Clock clock) {
    return new CreateJoinCode(households, codes, clock);
  }

  @Bean
  ViewJoinCode viewJoinCode(HouseholdRepository households, JoinCodeRepository codes, Clock clock) {
    return new ViewJoinCode(households, codes, clock);
  }

  @Bean
  RevokeJoinCode revokeJoinCode(HouseholdRepository households, JoinCodeRepository codes) {
    return new RevokeJoinCode(households, codes);
  }

  @Bean
  JoinHouseholdWithCode joinHouseholdWithCode(
      JoinCodeRepository codes,
      HouseholdRepository households,
      UnitOfWork unitOfWork,
      Clock clock) {
    return new JoinHouseholdWithCode(codes, households, unitOfWork, clock);
  }

  @Bean
  PackUpMemberFood packUpMemberFood(HouseholdRepository households, CarriedFood carried) {
    return new PackUpMemberFood(households, carried);
  }

  @Bean
  PackUpHouseholdFood packUpHouseholdFood(HouseholdRepository households, CarriedFood carried) {
    return new PackUpHouseholdFood(households, carried);
  }

  @Bean
  FoodAccessGuard foodAccessGuard() {
    return new FoodAccessGuard();
  }
}
