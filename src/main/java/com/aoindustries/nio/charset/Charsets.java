/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2013, 2016, 2017  AO Industries, Inc.
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
package com.aoindustries.nio.charset;

import java.nio.charset.Charset;

/**
 * Defines some constants for handling standard character sets.
 *
 * @author  AO Industries, Inc.
 * 
 * Java 1.7: deprecated  Use java.nio.charset.StandardCharsets as of Java 1.7
 * @see java.nio.charset.StandardCharsets in Java 1.7
 */
// Java .1.7: @Deprecated
public class Charsets {

	/**
	 * Java 1.7: deprecated  Use java.nio.charset.StandardCharsets as of Java 1.7
	 * @see java.nio.charset.StandardCharsets in Java 1.7
	 */
	// Java 1.7: @Deprecated
	public static final Charset
		US_ASCII   = Charset.forName("US-ASCII"),
		ISO_8859_1 = Charset.forName("ISO-8859-1"),
		UTF_8      = Charset.forName("UTF-8"),
		UTF_16BE   = Charset.forName("UTF-16BE"),
		UTF_16LE   = Charset.forName("UTF-16LE"),
		UTF_16     = Charset.forName("UTF-16")
	;

	/**
	 * Make no instances.
	 */
	private Charsets() {
	}
}
