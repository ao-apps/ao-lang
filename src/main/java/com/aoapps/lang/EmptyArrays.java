/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2013, 2014, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import java.io.Serializable;

/**
 * Various empty array constants.
 *
 * @author  AO Industries, Inc.
 */
public final class EmptyArrays {

	/** Make no instances. */
	private EmptyArrays() {throw new AssertionError();}

	public static final byte[] EMPTY_BYTE_ARRAY = {};
	public static final char[] EMPTY_CHAR_ARRAY = {};
	public static final int[] EMPTY_INT_ARRAY = {};
	public static final long[] EMPTY_LONG_ARRAY = {};
	public static final Class<?>[] EMPTY_CLASS_ARRAY = {};
	public static final Object[] EMPTY_OBJECT_ARRAY = {};
	public static final Serializable[] EMPTY_SERIALIZABLE_ARRAY = {};
	public static final String[] EMPTY_STRING_ARRAY = {};
}
