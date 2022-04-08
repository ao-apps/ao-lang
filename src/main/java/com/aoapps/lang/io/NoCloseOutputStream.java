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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Overrides {@link #close()} to a no-op.
 */
public class NoCloseOutputStream extends FilterOutputStream implements NoClose {

	@SuppressWarnings("unchecked")
	public static <O extends OutputStream & NoClose> O wrap(OutputStream out) {
		if(out instanceof NoClose && ((NoClose)out).isNoClose()) return (O)out;
		return (O)new NoCloseOutputStream(out);
	}

	/**
	 * @deprecated  Please use {@link #wrap(java.io.OutputStream)} to skip wrapping when possible.
	 */
	@Deprecated
	public NoCloseOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	/**
	 * Does not close the wrapped stream.
	 */
	@Override
	public void close() {
		// Do nothing
	}
}
