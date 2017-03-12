/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2013, 2014, 2016, 2017  AO Industries, Inc.
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
 * along with ao-lang.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.util.i18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Convenience static access to more locales than provided by the stock Locale class.
 *
 * @author  AO Industries, Inc.
 */
public class Locales {

	private Locales() {}

	/**
	 * Gets the script if Java 1.8 or "" if not Java 1.8.
	 *
	 * Java 1.8: Get rid of this and just call directly
	 *
	 * @see  Locale#getScript()
	 */
	static String getScript(Locale locale) {
		try {
			Method method = Locale.class.getMethod("getScript");
			return (String)method.invoke(locale);
		} catch(NoSuchMethodException e) {
			return "";
		} catch(IllegalAccessException e) {
			AssertionError ae = new AssertionError("getScript() should be accessible");
			ae.initCause(e);
			throw ae;
		} catch(InvocationTargetException e) {
			AssertionError ae = new AssertionError("getScript() should not throw any exceptions");
			ae.initCause(e);
			throw ae;
		}
	}

	/**
	 * Gets the extension keys if Java 1.8 or "" if not Java 1.8.
	 *
	 * Java 1.8: Get rid of this and just call directly
	 *
	 * @see  Locale#getExtensionKeys()
	 */
	@SuppressWarnings("unchecked")
	static Set<Character> getExtensionKeys(Locale locale) {
		try {
			Method method = Locale.class.getMethod("getExtensionKeys");
			return (Set<Character>)method.invoke(locale);
		} catch(NoSuchMethodException e) {
			return Collections.emptySet();
		} catch(IllegalAccessException e) {
			AssertionError ae = new AssertionError("getExtensionKeys() should be accessible");
			ae.initCause(e);
			throw ae;
		} catch(InvocationTargetException e) {
			AssertionError ae = new AssertionError("getExtensionKeys() should not throw any exceptions");
			ae.initCause(e);
			throw ae;
		}
	}

	// Was getting NullPointerException on class init, trying cache in separate class.
	// It might have been due to memory exhausted in Tomcat, but this won't hurt.
	private static class LocaleCache {

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
				if(!(o instanceof CacheKey)) return false;
				CacheKey other = (CacheKey)o;
				return
					language.equals(other.language)
					&& country.equals(other.country)
					&& variant.equals(other.variant)
				;
			}

			@Override
			public int hashCode() {
				int hash = language.hashCode();
				hash = hash * 31 + country.hashCode();
				hash = hash * 31 + variant.hashCode();
				return hash;
			}
		}

		private static final ConcurrentMap<CacheKey,Locale> locales = new ConcurrentHashMap<CacheKey,Locale>(16, 0.75f, 1);

		/**
		 * @see  Locales#getCachedLocale(java.lang.String, java.lang.String, java.lang.String)
		 */
		private static Locale getCachedLocale(String language, String country, String variant) {
			language = language.toLowerCase(Locale.ROOT);
			country = country.toUpperCase(Locale.ROOT);
			CacheKey key = new CacheKey(language, country, variant);
			Locale locale = locales.get(key);
			if(locale == null) {
				locale = new Locale(
					language,
					country,
					variant
				);
				Locale existing = locales.putIfAbsent(key, locale);
				if(existing != null) locale = existing;
			}
			return locale;
		}

		// Preload all standard Java locales
		static {
			for(Locale locale : Locale.getAvailableLocales()) {
				// Ignore locales with script or extensions for preload, since the rest of this API is unaware of them
				if(
					getScript(locale).isEmpty()
					&& getExtensionKeys(locale).isEmpty()
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
		private LocaleCache() {
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
		if(pos1 == -1) {
			return pos2;
		} else {
			if(pos2 == -1) {
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
		if(pos == -1) {
			return getCachedLocale(locale, "", "");
		} else {
			int pos2 = indexOfSeparator(locale, pos+1);
			if(pos2 == -1) {
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
			"ar".equals(language)    // arabic
			|| "iw".equals(language) // hebrew
			|| "fa".equals(language) // persian
		;
	}

	/**
	 * Some locale constants not provided directly by Java, along with those provided by Java.
	 */
	public static final Locale
		/** Root locale */
		ROOT = Locale.ROOT,
		/** Languages */
		ARABIC = parseLocale("ar"),
		BULGARIAN = parseLocale("bg"),
		CATALAN = parseLocale("ca"),
		CZECH = parseLocale("cs"),
		DANISH = parseLocale("da"),
		GERMAN = Locale.GERMAN,
		GREEK = parseLocale("el"),
		ENGLISH = Locale.ENGLISH,
		SPANISH = parseLocale("es"),
		ESTONIAN = parseLocale("et"),
		PERSIAN = parseLocale("fa"),
		FINNISH = parseLocale("fi"),
		FRENCH = Locale.FRENCH,
		HINDI = parseLocale("hi"),
		CROATIAN = parseLocale("hr"),
		HUNGARIAN = parseLocale("hu"),
		// INDONESIAN is now "id" - this matches Java's backward compatibility
		INDONESIAN = parseLocale("in"),
		ICELANDIC = parseLocale("is"),
		ITALIAN = Locale.ITALIAN,
		JAPANESE = Locale.JAPANESE,
		KOREAN = Locale.KOREAN,
		// HEBREW is now "he" - this matches Java's backward compatibility
		HEBREW = parseLocale("iw"),
		LITHUANIAN = parseLocale("lt"),
		LATVIAN = parseLocale("lv"),
		DUTCH = parseLocale("nl"),
		NORWEGIAN = parseLocale("no"),
		POLISH = parseLocale("pl"),
		PORTUGUESE = parseLocale("pt"),
		ROMANIAN = parseLocale("ro"),
		RUSSIAN = parseLocale("ru"),
		SLOVAK = parseLocale("sk"),
		SLOVENIAN = parseLocale("sl"),
		SERBIAN = parseLocale("sr"),
		SWEDISH = parseLocale("sv"),
		TURKISH = parseLocale("tr"),
		CHINESE = Locale.CHINESE,
		SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE,
		TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE,
		/** Countries */
		FRANCE = Locale.FRANCE,
		GERMANY = Locale.GERMANY,
		ITALY = Locale.ITALY,
		JAPAN = Locale.JAPAN,
		KOREA = Locale.KOREA,
		CHINA = Locale.SIMPLIFIED_CHINESE,
		PRC = Locale.SIMPLIFIED_CHINESE,
		TAIWAN = Locale.TRADITIONAL_CHINESE,
		UK = Locale.UK,
		US = Locale.US,
		CANADA = Locale.CANADA,
		CANADA_FRENCH = Locale.CANADA_FRENCH
	;
}
