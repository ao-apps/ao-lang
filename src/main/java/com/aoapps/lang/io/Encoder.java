/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2015, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

import java.io.IOException;
import java.io.Writer;

/**
 * Encodes data as it is written to the provided output.
 *
 * @author  AO Industries, Inc.
 */
public interface Encoder {

	/**
	 * Is this encoder buffered?  A buffered encoder may delay validation until {@link #writeSuffixTo(java.lang.Appendable, boolean)}.
	 * Furthermore, a buffered encoder should not be bypassed before any buffered data has been written via {@link #writeSuffixTo(java.lang.Appendable, boolean)}.
	 * An example of encoder bypassing is performing direct output on the writer from {@link EncoderWriter#getOut()}.
	 *
	 * @return  {@code false} by default
	 */
	default boolean isBuffered() {
		return false;
	}

	/**
	 * This is called before any data is written.
	 *
	 * @param  out  May optionally have already been optimized via {@link com.aoapps.lang.Coercion#optimize(java.lang.Appendable, com.aoapps.lang.io.Encoder)}.
	 */
	void writePrefixTo(Appendable out) throws IOException;

	void write(int c, Writer out) throws IOException;

	void write(char[] cbuf, Writer out) throws IOException;

	void write(char[] cbuf, int off, int len, Writer out) throws IOException;

	void write(String str, Writer out) throws IOException;

	void write(String str, int off, int len, Writer out) throws IOException;

	Encoder append(char c, Appendable out) throws IOException;

	Encoder append(CharSequence csq, Appendable out) throws IOException;

	Encoder append(CharSequence csq, int start, int end, Appendable out) throws IOException;

	/**
	 * This is called when no more data will be written.
	 * This should also flush any internal buffers to <code>out</code>.  It
	 * should not, however, call flush on <code>out</code> itself.  This is
	 * to not interfere with any output buffering of <code>out</code>.
	 * <p>
	 * The internal buffer is always clear for re-use, even when an exception is thrown.
	 * </p>
	 *
	 * @param  out  May optionally have already been optimized via {@link com.aoapps.lang.Coercion#optimize(java.lang.Appendable, com.aoapps.lang.io.Encoder)}.
	 *
	 * @deprecated  Please use {@link #writeSuffixTo(java.lang.Appendable, boolean)} while specifying desired trim.
	 */
	@Deprecated
	void writeSuffixTo(Appendable out) throws IOException;

	/**
	 * This is called when no more data will be written.
	 * This should also flush any internal buffers to <code>out</code>.  It
	 * should not, however, call flush on <code>out</code> itself.  This is
	 * to not interfere with any output buffering of <code>out</code>.
	 * <p>
	 * The internal buffer is always clear for re-use, even when an exception is thrown.
	 * </p>
	 *
	 * @param  out  May optionally have already been optimized via {@link com.aoapps.lang.Coercion#optimize(java.lang.Appendable, com.aoapps.lang.io.Encoder)}.
	 *
	 * @param  trim  Requests that the buffer be trimmed, if buffered and trim supported.
	 */
	default void writeSuffixTo(Appendable out, boolean trim) throws IOException {
		// Default to no trim supported implementation for API compatibility
		writeSuffixTo(out);
	}
}
