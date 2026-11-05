package dev.haypacomer.domain.quantity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuantityParser {

  private static final int MAX_LENGTH = 120;
  private static final Pattern TERM_SEPARATOR = Pattern.compile("\\s*\\+\\s*|\\s+(?:and|y)\\s+");
  private static final Pattern MIXED = Pattern.compile("^(\\d+)\\s+(\\d+)/(\\d+)(.*)$");
  private static final Pattern FRACTION = Pattern.compile("^(\\d+)/(\\d+)(.*)$");
  private static final Pattern DECIMAL =
      Pattern.compile("^([1-9]\\d{0,2}(?:[.,]\\d{3})+(?!\\d)|\\d+(?:[.,]\\d+)?)(.*)$");
  private static final Pattern THOUSANDS = Pattern.compile("[1-9]\\d{0,2}(?:[.,]\\d{3})+");

  private static final Set<String> FOOD_LINK = Set.of("de", "del", "of");

  private QuantityParser() {}

  public static QuantityExpression parse(String text) {
    if (text == null || text.isBlank()) {
      throw new InvalidQuantityException("Quantity is empty");
    }
    if (text.length() > MAX_LENGTH) {
      throw new InvalidQuantityException("Quantity is too long");
    }
    QuantityExpression expression = null;
    String typed =
        text.replace("\u00bd", " 1/2").replace("\u00bc", " 1/4").replace("\u00be", " 3/4");
    for (String term : TERM_SEPARATOR.split(typed.strip().toLowerCase(Locale.ROOT), -1)) {
      Amount amount = term(term);
      expression = expression == null ? amount : new Sum(expression, amount);
    }
    return expression;
  }

  private static Amount term(String term) {
    String text = term.strip();
    Matcher mixed = MIXED.matcher(text);
    if (mixed.matches()) {
      BigDecimal whole = new BigDecimal(mixed.group(1));
      return amount(whole.add(fraction(mixed.group(2), mixed.group(3), term)), mixed.group(4));
    }
    Matcher fraction = FRACTION.matcher(text);
    if (fraction.matches()) {
      return amount(fraction(fraction.group(1), fraction.group(2), term), fraction.group(3));
    }
    Matcher decimal = DECIMAL.matcher(text);
    if (decimal.matches()) {
      return amount(number(decimal.group(1)), decimal.group(2));
    }
    throw new InvalidQuantityException("Cannot read a quantity from \"" + term.strip() + "\"");
  }

  // Three digits after a separator are thousands, as Colombia writes 1.000 g; a mistaken 1.500 kg
  // then lands over the limit in Amount and is refused instead of stored a thousand times off.
  private static BigDecimal number(String text) {
    return THOUSANDS.matcher(text).matches()
        ? new BigDecimal(text.replaceAll("[.,]", ""))
        : new BigDecimal(text.replace(',', '.'));
  }

  private static BigDecimal fraction(String numerator, String denominator, String term) {
    BigDecimal divisor = new BigDecimal(denominator);
    if (divisor.signum() == 0) {
      throw new InvalidQuantityException("Division by zero in \"" + term.strip() + "\"");
    }
    return new BigDecimal(numerator).divide(divisor, MathContext.DECIMAL64);
  }

  private static Amount amount(BigDecimal value, String rest) {
    if (value.signum() == 0) {
      throw new InvalidQuantityException("A quantity is more than zero");
    }
    String words = rest.strip();
    String[] parts = words.isEmpty() ? new String[0] : words.split("\\s+");
    Optional<Unit> unit = parts.length == 0 ? Optional.empty() : UnitVocabulary.lookup(parts[0]);
    boolean clean =
        unit.isPresent()
            ? parts.length == 1 || FOOD_LINK.contains(parts[1])
            : !words.matches(".*[\\d+*/=-].*");
    if (!clean) {
      throw new InvalidQuantityException("Cannot read a quantity from \"" + words + "\"");
    }
    return new Amount(new Quantity(value, unit.orElse(Unit.PIECE)));
  }
}
