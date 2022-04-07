/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2022  AO Industries, Inc.
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

import java.io.Closeable;
import java.io.IOException;

/**
 * Indicates that {@link #close()} is overridden to be a no-op.
 * This facilitates avoiding duplicate wrapping for close protection.
 *
 * @see  NoCloseInputStream#wrap(java.io.InputStream)
 * @see  NoCloseOutputStream#wrap(java.io.OutputStream)
 * @see  NoCloseReader#wrap(java.io.Reader)
 * @see  NoCloseWriter#wrap(java.io.Writer)
 * @see  NullOutputStream#getInstance()
 * @see  NullPrintWriter#getInstance()
 * @see  NullWriter#getInstance()
 */
public interface NoClose extends Closeable {

	/**
	 * Calls to close are ignored.
	 */
	@Override
	void close() throws IOException;
}
