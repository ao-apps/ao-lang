/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2021  AO Industries, Inc.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Provides direct access to the internal <code>byte[]</code>
 *
 * @author  AO Industries, Inc.
 */
public class AoByteArrayOutputStream extends ByteArrayOutputStream {

	public AoByteArrayOutputStream() {
		super();
	}

	public AoByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * Provides direct access to the internal byte[] to avoid unnecessary
	 * copying of the array.
	 */
	public byte[] getInternalByteArray() {
		return this.buf;
	}

	/**
	 * Writes a portion of the contents of the buffer to another byte stream.
	 */
	public synchronized void writeTo(OutputStream out, int off, int len) throws IOException {
		out.write(buf, off, len);
	}

	public synchronized void writeTo(RandomAccessFile raf) throws IOException {
		raf.write(buf, 0, count);
	}

	public synchronized void writeTo(RandomAccessFile raf, int off, int len) throws IOException {
		raf.write(buf, off, len);
	}
}
