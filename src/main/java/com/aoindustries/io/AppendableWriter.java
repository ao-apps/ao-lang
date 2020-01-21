/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import javax.swing.text.Segment;

/**
 * Writer that writes to an underlying {@link Appendable}.
 * This is not necessarily the absolute fastest way to get things done (at least
 * when a {@code char[]} has to be wrapped in a {@link Segment}), but can help
 * bridge the gap between APIs based on {@link Writer} and {@link Appendable}.
 *
 * @author  AO Industries, Inc.
 */
public class AppendableWriter extends Writer {

	/**
	 * Wraps the given {@link Appendable} if it is not already
	 * a {@link Writer}.
	 *
	 * @return  The given out, if it is already a {@link Writer}, otherwise an
	 *          {@link AppendableWriter} wrapping out.
	 */
	public static Writer wrap(Appendable out) {
		if(out instanceof Writer) return (Writer)out;
		return new AppendableWriter(out);
	}

	private final Appendable out;

	public AppendableWriter(Appendable out) {
		this.out = out;
	}

	/**
	 * Gets the wrapped appendable.
	 */
	public Appendable getOut() {
		return out;
	}

	@Override
	public void write(int c) throws IOException {
		out.append((char)c);
	}

	@Override
	public void write(char cbuf[]) throws IOException {
		out.append(new Segment(cbuf, 0, cbuf.length));
	}

	@Override
	public void write(char cbuf[], int off, int len) throws IOException {
		out.append(new Segment(cbuf, off, len));
	}

	@Override
	public void write(String str) throws IOException {
		out.append(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		out.append(str, off, off + len);
	}

	@Override
	public AppendableWriter append(CharSequence csq) throws IOException {
		out.append(csq);
		return this;
	}

	@Override
	public AppendableWriter append(CharSequence csq, int start, int end) throws IOException {
		out.append(csq, start, end);
		return this;
	}

	@Override
	public AppendableWriter append(char c) throws IOException {
		out.append(c);
		return this;
	}

	@Override
	public void flush() throws IOException {
		if(out instanceof Flushable) {
			((Flushable)out).flush();
		}
	}

	@Override
	public void close() throws IOException {
		if(out instanceof Closeable) {
			((Closeable)out).close();
		}
	}
}
