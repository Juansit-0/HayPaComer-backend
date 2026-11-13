package dev.haypacomer.web.agent;

import dev.haypacomer.application.agent.ClearHouseholdMemory;
import dev.haypacomer.application.agent.EditHouseholdMemory;
import dev.haypacomer.application.agent.RememberForHousehold;
import dev.haypacomer.application.agent.ViewHouseholdMemory;
import dev.haypacomer.application.port.HouseholdMemory;
import dev.haypacomer.application.port.HouseholdRepository;
import dev.haypacomer.application.port.UnitOfWork;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HouseholdMemoryConfiguration {

  @Bean
  ViewHouseholdMemory viewHouseholdMemory(HouseholdRepository households, HouseholdMemory memory) {
    return new ViewHouseholdMemory(households, memory);
  }

  @Bean
  RememberForHousehold rememberForHousehold(
      HouseholdRepository households, HouseholdMemory memory, UnitOfWork unitOfWork) {
    return new RememberForHousehold(households, memory, unitOfWork);
  }

  @Bean
  EditHouseholdMemory editHouseholdMemory(
      HouseholdRepository households, HouseholdMemory memory, UnitOfWork unitOfWork) {
    return new EditHouseholdMemory(households, memory, unitOfWork);
  }

  @Bean
  ClearHouseholdMemory clearHouseholdMemory(
      HouseholdRepository households, HouseholdMemory memory) {
    return new ClearHouseholdMemory(households, memory);
  }
}
