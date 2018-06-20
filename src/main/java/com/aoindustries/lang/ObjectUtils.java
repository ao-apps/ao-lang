/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2013, 2016, 2017, 2018  AO Industries, Inc.
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
package com.aoindustries.lang;

import java.util.Arrays;

/**
 * Utilities that help when working with objects.
 *
 * @author  AO Industries, Inc.
 */
public final class ObjectUtils {

	/**
	 * Make no instances.
	 */
	private ObjectUtils() {
	}

	/**
	 * Gets the hashCode for an object or <code>0</code> when <code>null</code>.
	 * <p>
	 * Java 1.7: deprecated  use {@link java.util.Objects#hashCode(java.lang.Object)} as of Java 1.7.
	 * </p>
	 *
	 * @see java.util.Objects#hashCode(java.lang.Object) as of Java 1.7
	 *
	 * @deprecated  Please use {@link org.apache.commons.lang3.ObjectUtils#hashCode(java.lang.Object)} from
	 *              <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>.
	 */
	@Deprecated
	public static int hashCode(Object obj) {
		return obj != null ? obj.hashCode() : 0;
	}

	/**
	 * Gets the hashCode for a set of objects or <code>0</code> when <code>null</code>.
	 * <p>
	 * Java 1.7: deprecated  use {@link java.util.Objects#hash(java.lang.Object...)} as of Java 1.7.
	 * </p>
	 *
	 * @see java.util.Objects#hash(java.lang.Object...) as of Java 1.7
	 *
	 * @deprecated  Please use {@link org.apache.commons.lang3.ObjectUtils#hashCodeMulti(java.lang.Object...)} from
	 *              <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>.
	 */
	@Deprecated
	public static int hash(Object... values) {
		return Arrays.hashCode(values);
	}

	/**
	 * Compares the equality of two objects, including their null states.
	 * <p>
	 * Java 1.7: deprecated  use java.util.Objects#equals(Object, Object) as of Java 1.7.
	 * </p>
	 * @see java.util.Objects#equals(java.lang.Object, java.lang.Object) as of Java 1.7
	 *
	 * @deprecated  Please use {@link org.apache.commons.lang3.ObjectUtils#equals(java.lang.Object, java.lang.Object)} from
	 *              <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>.
	 */
	@Deprecated
	public static boolean equals(Object obj1, Object obj2) {
		return (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
	}

	/**
	 * Calls toString if non-null, returns null when null.
	 * <p>
	 * Java 1.7: deprecated  use {@link java.util.Objects#toString(java.lang.Object, java.lang.String)} as of Java 1.7.
	 * </p>
	 *
	 * @see java.util.Objects#toString(java.lang.Object, java.lang.String)
	 *
	 * @deprecated  Please use {@link org.apache.commons.lang3.ObjectUtils#toString(java.lang.Object, java.lang.String)} from
	 *              <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>.
	 */
	@Deprecated
	public static String toString(Object obj) {
		return obj==null ? null : obj.toString();
	}
}
