package dev.haypacomer.agent.tools;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

public enum ParameterType {
  TEXT {
    @Override
    Optional<String> problem(String value) {
      if (value.isBlank()) {
        return Optional.of("must not be blank");
      }
      return value.length() > MAX_TEXT
          ? Optional.of("must be at most " + MAX_TEXT + " characters")
          : Optional.empty();
    }
  },
  GRAMS {
    @Override
    Optional<String> problem(String value) {
      return integer(value)
          .filter(grams -> grams >= 1 && grams <= MAX_GRAMS)
          .map(grams -> Optional.<String>empty())
          .orElse(Optional.of("must be whole grams between 1 and " + MAX_GRAMS));
    }
  },
  COUNT {
    @Override
    Optional<String> problem(String value) {
      return integer(value)
          .filter(count -> count >= 1 && count <= MAX_COUNT)
          .map(count -> Optional.<String>empty())
          .orElse(Optional.of("must be a whole number between 1 and " + MAX_COUNT));
    }
  },
  MONEY {
    @Override
    Optional<String> problem(String value) {
      return integer(value)
          .filter(amount -> amount >= 1 && amount <= MAX_MONEY)
          .map(amount -> Optional.<String>empty())
          .orElse(Optional.of("must be a whole amount between 1 and " + MAX_MONEY));
    }
  },
  DATE {
    @Override
    Optional<String> problem(String value) {
      try {
        LocalDate.parse(value);
        return Optional.empty();
      } catch (DateTimeParseException invalid) {
        return Optional.of("must be an ISO date");
      }
    }
  },
  ID {
    @Override
    Optional<String> problem(String value) {
      try {
        UUID.fromString(value);
        return value.length() == 36 ? Optional.empty() : Optional.of("must be an id");
      } catch (IllegalArgumentException invalid) {
        return Optional.of("must be an id");
      }
    }
  };

  public static final int MAX_TEXT = 200;
  public static final long MAX_GRAMS = 100_000;
  public static final long MAX_COUNT = 50;
  public static final long MAX_MONEY = 10_000_000_000L;

  abstract Optional<String> problem(String value);

  private static Optional<Long> integer(String value) {
    try {
      return Optional.of(Long.parseLong(value));
    } catch (NumberFormatException invalid) {
      return Optional.empty();
    }
  }
}
