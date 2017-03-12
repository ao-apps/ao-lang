/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2015, 2017  AO Industries, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * As data is read from the input stream it is also written to the given output
 * stream.  When the input is closed, the output stream is flushed but not
 * closed.
 */
public final class SniffInputStream extends InputStream {

	private final InputStream in;
	private final OutputStream out;

	public SniffInputStream(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public int read() throws IOException {
		int b = in.read();
		if(b != -1) out.write(b);
		return b;
	}

	@Override
	public int read(byte b[]) throws IOException {
		int numBytes = in.read(b);
		if(numBytes > 0) out.write(b, 0, numBytes);
		return numBytes;
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int numBytes = in.read(b, off, len);
		if(numBytes > 0) out.write(b, off, numBytes);
		return numBytes;
	}

	// skip uses the default InputStream implementation to ensure skipped bytes are still sniffed.

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
		out.flush();
	}

	// mark/reset not supported
}
