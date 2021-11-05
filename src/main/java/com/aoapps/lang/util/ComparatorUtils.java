/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2016, 2017, 2018, 2019, 2021  AO Industries, Inc.
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
package com.aoapps.lang.util;

import java.text.Collator;
import java.util.Locale;

/**
 * Utilities that help when working with comparators.
 *
 * @author  AO Industries, Inc.
 */
public abstract class ComparatorUtils {

	/** Make no instances. */
	private ComparatorUtils() {throw new AssertionError();}

	/**
	 * Compares two integers.
	 *
	 * @deprecated  use {@link java.lang.Integer#compare(int, int)} as of Java 1.7.
	 */
	@Deprecated
	public static int compare(int i1, int i2) {
		return Integer.compare(i1, i2);
	}

	/**
	 * Compares two shorts.
	 *
	 * @deprecated  use {@link Short#compare(short,short)} as of Java 1.7.
	 */
	@Deprecated
	public static int compare(short s1, short s2) {
		return Short.compare(s1, s2);
	}

	/**
	 * Compares two booleans.
	 *
	 * @deprecated  use {@link java.lang.Boolean#compare(boolean,boolean)} as of Java 1.7.
	 */
	@Deprecated
	public static int compare(boolean b1, boolean b2) {
		return Boolean.compare(b1, b2);
	}

	/**
	 * Compares two longs.
	 *
	 * @deprecated  use {@link java.lang.Long#compare(long,long)} as of Java 1.7.
	 */
	@Deprecated
	public static int compare(long l1, long l2) {
		return Long.compare(l1, l2);
	}

	private static final Collator collator = Collator.getInstance(Locale.ROOT);

	/**
	 * Compares two strings in a root-locale case-insensitive manner, while
	 * remaining strictly consistent with equals.
	 */
	public static int compareIgnoreCaseConsistentWithEquals(String s1, String s2) {
		if(s1 == s2) return 0;
		int diff = collator.compare(s1, s2);
		if(diff != 0) return diff;
		return s1.compareTo(s2);
	}
}
