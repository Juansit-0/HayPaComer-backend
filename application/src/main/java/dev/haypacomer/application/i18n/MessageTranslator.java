package dev.haypacomer.application.i18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageTranslator {

  private static final String HOLE = "{}";

  private record Template(Pattern pattern, String target, int literal) {}

  private final Map<String, String> exact;
  private final List<Template> patterns;
  private final Map<String, String> texts;

  public MessageTranslator(Map<String, String> templates) {
    this(templates, Map.of());
  }

  public MessageTranslator(Map<String, String> templates, Map<String, String> texts) {
    this.texts = Map.copyOf(texts);
    this.exact = Map.copyOf(templates);
    List<Template> compiled = new ArrayList<>();
    templates.forEach(
        (source, target) -> {
          if (source.contains(HOLE)) {
            compiled.add(compile(source, target));
          }
        });
    compiled.sort(Comparator.comparingInt(Template::literal).reversed());
    this.patterns = List.copyOf(compiled);
  }

  public String translate(String message) {
    if (message == null || message.isBlank()) {
      return message;
    }
    String direct = exact.get(message);
    if (direct != null) {
      return direct;
    }
    for (Template template : patterns) {
      Matcher matcher = template.pattern().matcher(message);
      if (matcher.matches()) {
        return fill(template.target(), matcher);
      }
    }
    return message;
  }

  private static Template compile(String source, String target) {
    String[] parts = source.split(Pattern.quote(HOLE), -1);
    StringBuilder regex = new StringBuilder();
    int literal = 0;
    for (int index = 0; index < parts.length; index++) {
      if (index > 0) {
        regex.append("(.+?)");
      }
      regex.append(Pattern.quote(parts[index]));
      literal += parts[index].length();
    }
    return new Template(Pattern.compile(regex.toString(), Pattern.DOTALL), target, literal);
  }

  private String fill(String target, Matcher matcher) {
    StringBuilder result = new StringBuilder();
    int group = 1;
    int from = 0;
    int hole;
    while ((hole = target.indexOf(HOLE, from)) >= 0) {
      result.append(target, from, hole);
      result.append(
          group <= matcher.groupCount() ? named(matcher.group(group++), hole == 0) : HOLE);
      from = hole + HOLE.length();
    }
    return result.append(target.substring(from)).toString();
  }

  // A hole holding a food takes its name in this language, lowercase inside a sentence.
  private String named(String value, boolean starts) {
    String name = texts.get("food." + value.strip().toLowerCase(Locale.ROOT));
    if (name == null) {
      return value;
    }
    return starts ? name : name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
  }
}
