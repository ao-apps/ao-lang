/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2019, 2021, 2022  AO Industries, Inc.
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
package com.aoapps.lang.io;

import java.io.FilterReader;
import java.io.Reader;

/**
 * Overrides {@link #close()} to a no-op.
 */
public class NoCloseReader extends FilterReader implements NoClose {

	@SuppressWarnings("unchecked")
	public static <R extends Reader & NoClose> R wrap(Reader in) {
		if(in instanceof NoClose && ((NoClose)in).isNoClose()) return (R)in;
		return (R)new NoCloseReader(in);
	}

	/**
	 * @deprecated  Please use {@link #wrap(java.io.Reader)} to skip wrapping when possible.
	 */
	@Deprecated
	public NoCloseReader(Reader in) {
		super(in);
	}

	/**
	 * Does not close the wrapped reader.
	 */
	@Override
	public void close() {
		// Do nothing
	}
}
