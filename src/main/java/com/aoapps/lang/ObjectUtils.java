/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2013, 2016, 2017, 2018, 2019, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

import java.util.Arrays;

/**
 * Utilities that help when working with objects.
 *
 * @deprecated  use {@link java.util.Objects} as of Java 1.7.
 *
 * @author  AO Industries, Inc.
 */
@Deprecated
public abstract class ObjectUtils {

	/** Make no instances. */
	private ObjectUtils() {throw new AssertionError();}

	/**
	 * Gets {@linkplain Object#hashCode() the hashCode for an object}
	 * or {@code 0} when {@code null}.
	 *
	 * @deprecated  use {@link java.util.Objects#hashCode(java.lang.Object)} as of Java 1.7.
	 */
	@Deprecated
	public static int hashCode(Object obj) {
		return (obj != null) ? obj.hashCode() : 0;
	}

	/**
	 * Gets {@linkplain Arrays#hashCode(java.lang.Object[]) the hashCode for a set of objects}
	 * {@code 0} when {@code null}.
	 *
	 * @deprecated  use {@link java.util.Objects#hash(java.lang.Object...)} as of Java 1.7.
	 */
	@Deprecated
	public static int hash(Object... values) {
		return Arrays.hashCode(values);
	}

	/**
	 * Compares {@linkplain Object#equals(java.lang.Object) the equality of two objects},
	 * including their {@code null} states.
	 *
	 * @deprecated  use java.util.Objects#equals(Object, Object) as of Java 1.7.
	 */
	@Deprecated
	public static boolean equals(Object obj1, Object obj2) {
		return (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
	}

	/**
	 * Calls {@link Object#toString()} if non-{@code null},
	 * returns {@code null} when {@code null}.
	 *
	 * @deprecated  use {@link java.util.Objects#toString(java.lang.Object, java.lang.String)} as of Java 1.7.
	 */
	@Deprecated
	public static String toString(Object obj) {
		return (obj == null) ? null : obj.toString();
	}
}
