/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017, 2021  AO Industries, Inc.
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

import com.aoapps.lang.NullArgumentException;
import java.util.Locale;

/**
 * Associates a locale with a string.  This is useful to manipulate or represent
 * the string in a locale specific manner.
 *
 * @author  AO Industries, Inc.
 */
public class LocaleString {

	private final Locale locale;
	private final String value;

	public LocaleString(Locale locale, String value) {
		this.locale = NullArgumentException.checkNotNull(locale, "locale");
		this.value = NullArgumentException.checkNotNull(value, "value");
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof LocaleString)) return false;
		LocaleString other = (LocaleString)obj;
		return
			locale.equals(other.locale)
			&& value.equals(other.value)
		;
	}

	@Override
	public int hashCode() {
		int hash = locale.hashCode();
		hash = hash * 31 + value.hashCode();
		return hash;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getValue() {
		return value;
	}

	public LocaleString toLowerCase() {
		String newValue = value.toLowerCase(locale);
		return newValue == value ? this : new LocaleString(locale, newValue);
	}

	public LocaleString toUpperCase() {
		String newValue = value.toUpperCase(locale);
		return newValue == value ? this : new LocaleString(locale, newValue);
	}

	public LocaleString trim() {
		String newValue = value.trim();
		return newValue == value ? this : new LocaleString(locale, newValue);
	}

	public boolean isEmpty() {
		return value.isEmpty();
	}

	public boolean startsWith(String prefix) {
		return value.startsWith(prefix);
	}

	public boolean startsWith(String prefix, int offset) {
		return value.startsWith(prefix, offset);
	}

	public boolean endsWith(String prefix) {
		return value.endsWith(prefix);
	}
}
