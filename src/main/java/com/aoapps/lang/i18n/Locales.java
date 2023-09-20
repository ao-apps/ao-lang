/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016, 2017, 2019, 2020, 2021, 2022, 2023  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-lang.
 *
 * ao-lang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-lang is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-lang.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.lang.i18n;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Convenience static access to more locales than provided by the stock Locale class.
 *
 * @author  AO Industries, Inc.
 */
public final class Locales {

  /** Make no instances. */
  private Locales() {
    throw new AssertionError();
  }

  // Was getting NullPointerException on class init, trying cache in separate class.
  // It might have been due to memory exhausted in Tomcat, but this won't hurt.
  private static final class LocaleCache {

    /** Make no instances. */
    private LocaleCache() {
      throw new AssertionError();
    }

    private static class CacheKey {
      private final String language;
      private final String country;
      private final String variant;

      private CacheKey(String language, String country, String variant) {
        this.language = language;
        this.country = country;
        this.variant = variant;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof CacheKey)) {
          return false;
        }
        CacheKey other = (CacheKey) o;
        return
            language.equals(other.language)
                && country.equals(other.country)
                && variant.equals(other.variant);
      }

      @Override
      public int hashCode() {
        int hash = language.hashCode();
        hash = hash * 31 + country.hashCode();
        hash = hash * 31 + variant.hashCode();
        return hash;
      }
    }

    private static final ConcurrentMap<CacheKey, Locale> locales = new ConcurrentHashMap<>(16, 0.75f, 1);

    /**
     * @see  Locales#getCachedLocale(java.lang.String, java.lang.String, java.lang.String)
     */
    private static Locale getCachedLocale(String language, String country, String variant) {
      language = language.toLowerCase(Locale.ROOT);
      country = country.toUpperCase(Locale.ROOT);
      CacheKey key = new CacheKey(language, country, variant);
      Locale locale = locales.get(key);
      if (locale == null) {
        // Java 19: Deprecation of Locale Class Constructors, see https://bugs.openjdk.org/browse/JDK-8282819
        //          Entire caching mechanism should probably be removed
        locale = new Locale(
            language,
            country,
            variant
        );
        Locale existing = locales.putIfAbsent(key, locale);
        if (existing != null) {
          locale = existing;
        }
      }
      return locale;
    }

    // Preload all standard Java locales
    static {
      for (Locale locale : Locale.getAvailableLocales()) {
        // Ignore locales with script or extensions for preload, since the rest of this API is unaware of them
        if (
            locale.getScript().isEmpty()
                && locale.getExtensionKeys().isEmpty()
        ) {
          //System.out.println("preload: " + locale.toString());
          //System.out.println("preload.language     : " + locale.getLanguage());
          //System.out.println("preload.country      : " + locale.getCountry());
          //System.out.println("preload.variant      : " + locale.getVariant());
          //System.out.println("preload.script       : " + locale.getScript());
          //System.out.println("preload.extensionKeys: " + locale.getExtensionKeys());
          locales.put(
              new CacheKey(
                  locale.getLanguage(),
                  locale.getCountry(),
                  locale.getVariant()
              ),
              locale
          );
        }
      }
    }
  }

  /**
   * Gets a cached locale instance.
   */
  public static Locale getCachedLocale(String language, String country, String variant) {
    return LocaleCache.getCachedLocale(language, country, variant);
  }

  /**
   * Finds the first underscore (_) or dash(-).
   *
   * @return the position in the string or -1 if not found
   */
  private static int indexOfSeparator(String locale, int fromIndex) {
    int pos1 = locale.indexOf('_', fromIndex);
    int pos2 = locale.indexOf('-', fromIndex);
    if (pos1 == -1) {
      return pos2;
    } else {
      if (pos2 == -1) {
        return pos1;
      } else {
        return Math.min(pos1, pos2);
      }
    }
  }

  /**
   * Parses locales from their <code>toString</code> representation.
   * Language, country, and variant may be separated by underscore "_" or hyphen "-".
   * Language is converted to lowercase.
   * Country is converted to uppercase.
   * Caches locales so the same instance will be returned for each combination of language, country, and variant.
   * <p>
   *   Locales are currently cached forever.
   *   Malicious external sources of locales could fill the heap space, so protect against this if needed.
   * </p>
   */
  public static Locale parseLocale(String locale) {
    int pos = indexOfSeparator(locale, 0);
    if (pos == -1) {
      return getCachedLocale(locale, "", "");
    } else {
      int pos2 = indexOfSeparator(locale, pos + 1);
      if (pos2 == -1) {
        return getCachedLocale(
            locale.substring(0, pos).toLowerCase(Locale.ROOT),
            locale.substring(pos + 1).toUpperCase(Locale.ROOT),
            ""
        );
      } else {
        return getCachedLocale(
            locale.substring(0, pos).toLowerCase(Locale.ROOT),
            locale.substring(pos + 1, pos2).toUpperCase(Locale.ROOT),
            locale.substring(pos2 + 1)
        );
      }
    }
  }

  /**
   * Determines if the provided locale should be displayed from right to left.
   */
  public static boolean isRightToLeft(Locale locale) {
    String language = locale.getLanguage();
    return
        // arabic
        "ar".equals(language)
            // hebrew
            || "iw".equals(language) // Java <= 16
            || "he".equals(language) // Java >= 17
            // persian
            || "fa".equals(language);
  }

  /**
   * @deprecated  Please use {@link Locale#ROOT} directly.
   */
  @Deprecated
  public static final Locale ROOT = Locale.ROOT;

  /* Languages */

  public static final Locale ARABIC = parseLocale("ar");
  public static final Locale BULGARIAN = parseLocale("bg");
  public static final Locale CATALAN = parseLocale("ca");
  public static final Locale CZECH = parseLocale("cs");
  public static final Locale DANISH = parseLocale("da");

  /**
   * @deprecated  Please use {@link Locale#GERMAN} directly.
   */
  @Deprecated
  public static final Locale GERMAN = Locale.GERMAN;

  public static final Locale GREEK = parseLocale("el");

  /**
   * @deprecated  Please use {@link Locale#ENGLISH} directly.
   */
  @Deprecated
  public static final Locale ENGLISH = Locale.ENGLISH;

  public static final Locale SPANISH = parseLocale("es");
  public static final Locale ESTONIAN = parseLocale("et");
  public static final Locale PERSIAN = parseLocale("fa");
  public static final Locale FINNISH = parseLocale("fi");

  /**
   * @deprecated  Please use {@link Locale#FRENCH} directly.
   */
  @Deprecated
  public static final Locale FRENCH = Locale.FRENCH;

  public static final Locale HINDI = parseLocale("hi");
  public static final Locale CROATIAN = parseLocale("hr");
  public static final Locale HUNGARIAN = parseLocale("hu");

  /**
   * INDONESIAN is now "id" - this matches Java's backward compatibility "in".
   */
  // Java 17: Update ISO 639 language codes: https://bugs.openjdk.org/browse/JDK-8263202
  public static final Locale INDONESIAN = parseLocale("in");
  public static final Locale ICELANDIC = parseLocale("is");

  /**
   * @deprecated  Please use {@link Locale#ITALIAN} directly.
   */
  @Deprecated
  public static final Locale ITALIAN = Locale.ITALIAN;

  /**
   * @deprecated  Please use {@link Locale#JAPANESE} directly.
   */
  @Deprecated
  public static final Locale JAPANESE = Locale.JAPANESE;

  /**
   * @deprecated  Please use {@link Locale#KOREAN} directly.
   */
  @Deprecated
  public static final Locale KOREAN = Locale.KOREAN;

  /**
   * HEBREW is now "he" - this matches Java's backward compatibility "iw".
   */
  // Java 17: Update ISO 639 language codes: https://bugs.openjdk.org/browse/JDK-8263202
  public static final Locale HEBREW = parseLocale("iw");
  public static final Locale LITHUANIAN = parseLocale("lt");
  public static final Locale LATVIAN = parseLocale("lv");
  public static final Locale DUTCH = parseLocale("nl");
  public static final Locale NORWEGIAN = parseLocale("no");
  public static final Locale POLISH = parseLocale("pl");
  public static final Locale PORTUGUESE = parseLocale("pt");
  public static final Locale ROMANIAN = parseLocale("ro");
  public static final Locale RUSSIAN = parseLocale("ru");
  public static final Locale SLOVAK = parseLocale("sk");
  public static final Locale SLOVENIAN = parseLocale("sl");
  public static final Locale SERBIAN = parseLocale("sr");
  public static final Locale SWEDISH = parseLocale("sv");
  public static final Locale TURKISH = parseLocale("tr");

  /**
   * @deprecated  Please use {@link Locale#CHINESE} directly.
   */
  @Deprecated
  public static final Locale CHINESE = Locale.CHINESE;

  /**
   * @deprecated  Please use {@link Locale#SIMPLIFIED_CHINESE} directly.
   */
  @Deprecated
  public static final Locale SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;

  /**
   * @deprecated  Please use {@link Locale#TRADITIONAL_CHINESE} directly.
   */
  @Deprecated
  public static final Locale TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE;

  /* Countries */

  /**
   * @deprecated  Please use {@link Locale#FRANCE} directly.
   */
  @Deprecated
  public static final Locale FRANCE = Locale.FRANCE;

  /**
   * @deprecated  Please use {@link Locale#GERMANY} directly.
   */
  @Deprecated
  public static final Locale GERMANY = Locale.GERMANY;

  /**
   * @deprecated  Please use {@link Locale#ITALY} directly.
   */
  @Deprecated
  public static final Locale ITALY = Locale.ITALY;

  /**
   * @deprecated  Please use {@link Locale#JAPAN} directly.
   */
  @Deprecated
  public static final Locale JAPAN = Locale.JAPAN;

  /**
   * @deprecated  Please use {@link Locale#KOREA} directly.
   */
  @Deprecated
  public static final Locale KOREA = Locale.KOREA;

  /**
   * @deprecated  Please use {@link Locale#CHINA} directly.
   */
  @Deprecated
  public static final Locale CHINA = Locale.CHINA;

  /**
   * @deprecated  Please use {@link Locale#PRC} directly.
   */
  @Deprecated
  public static final Locale PRC = Locale.PRC;

  /**
   * @deprecated  Please use {@link Locale#TAIWAN} directly.
   */
  @Deprecated
  public static final Locale TAIWAN = Locale.TAIWAN;

  /**
   * @deprecated  Please use {@link Locale#UK} directly.
   */
  @Deprecated
  public static final Locale UK = Locale.UK;

  /**
   * @deprecated  Please use {@link Locale#US} directly.
   */
  @Deprecated
  public static final Locale US = Locale.US;

  /**
   * @deprecated  Please use {@link Locale#CANADA} directly.
   */
  @Deprecated
  public static final Locale CANADA = Locale.CANADA;

  /**
   * @deprecated  Please use {@link Locale#CANADA_FRENCH} directly.
   */
  @Deprecated
  public static final Locale CANADA_FRENCH = Locale.CANADA_FRENCH;
}
