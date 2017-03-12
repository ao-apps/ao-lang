/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017  AO Industries, Inc.
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

import com.aoindustries.math.SafeMath;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Provides direct access to the internal <code>char[]</code>
 *
 * @author  AO Industries, Inc.
 */
public class AoCharArrayWriter
	extends CharArrayWriter
	implements Writable
{

	public AoCharArrayWriter() {
		super();
	}

	public AoCharArrayWriter(int initialSize) {
		super(initialSize);
	}

	public char[] getInternalCharArray() {
		return this.buf;
	}

	@Override
	public long getLength() {
		return size();
	}

	@Override
	public boolean isFastToString() {
		return false;
	}

	/**
	 * Converts a portion of the input data to a string.
	 *
	 * @return the string.
	 */
	public String toString(int off, int len) {
		synchronized(lock) {
			return new String(buf, off, len);
		}
	}

	/**
	 * Writes a portion of the contents of the buffer to another character stream.
	 */
	@Override
	public void writeTo(Writer out, long off, long len) throws IOException {
		synchronized(lock) {
			if((off+len)>count) throw new IndexOutOfBoundsException();
			out.write(
				buf,
				SafeMath.castInt(off),
				SafeMath.castInt(len)
			);
		}
	}

	@Override
	public void writeTo(Encoder encoder, Writer out) throws IOException {
		synchronized(lock) {
			encoder.write(buf, 0, count, out);
		}
	}

	@Override
	public void writeTo(Encoder encoder, Writer out, long off, long len) throws IOException {
		synchronized(lock) {
			if((off+len)>count) throw new IndexOutOfBoundsException();
			encoder.write(
				buf,
				SafeMath.castInt(off),
				SafeMath.castInt(len),
				out
			);
		}
	}
}
