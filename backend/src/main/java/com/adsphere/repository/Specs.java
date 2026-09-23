package com.adsphere.repository;

import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Small helpers for composing optional filters into JPA specifications. */
public final class Specs {

  private Specs() {}

  public static <T> Specification<T> all() {
    return (root, query, cb) -> cb.conjunction();
  }

  public static <T> Specification<T> equalTo(String attribute, Object value) {
    if (value == null) {
      return all();
    }
    return (root, query, cb) -> cb.equal(root.get(attribute), value);
  }

  /** Case-insensitive "contains" match across one or more attributes. */
  public static <T> Specification<T> containsText(String text, String... attributes) {
    if (!StringUtils.hasText(text)) {
      return all();
    }
    String pattern = "%" + escape(text.trim().toLowerCase(Locale.ROOT)) + "%";
    return (root, query, cb) ->
        cb.or(
            java.util.Arrays.stream(attributes)
                .map(a -> cb.like(cb.lower(root.get(a)), pattern, '\\'))
                .toArray(javax.persistence.criteria.Predicate[]::new));
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
