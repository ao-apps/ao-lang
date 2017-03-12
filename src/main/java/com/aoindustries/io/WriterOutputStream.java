/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017  AO Industries, Inc.
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

import com.aoindustries.util.BufferManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * A writer output stream makes a <code>Writer</code> behave like an
 * <code>OutputStream</code>.  No encoding/decoding is performed.
 *
 * @author  AO Industries, Inc.
 */
final public class WriterOutputStream extends OutputStream {

	private final Writer out;

	/**
	 * The conversions are done in this buffer for minimal memory allocation.
	 * Released on close.
	 */
	private char[] buff=BufferManager.getChars();

	/**
	 * Create a new PrintWriter, without automatic line flushing.
	 *
	 * @param  out        A character-output stream
	 */
	public WriterOutputStream(Writer out) {
		this.out=out;
	}

	@Override
	public void close() throws IOException {
		synchronized(this) {
			out.close();
			if(buff!=null) {
				BufferManager.release(buff, false);
				buff=null;
			}
		}
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		synchronized(this) {
			if (b == null) throw new NullPointerException();
			int pos=0;
			while(pos<len) {
				int blockSize=len-pos;
				if(blockSize>BufferManager.BUFFER_SIZE) blockSize=BufferManager.BUFFER_SIZE;
				for(int cpos=0;cpos<blockSize;cpos++) buff[cpos]=(char)b[off+(pos++)];
				out.write(buff, 0, blockSize);
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	/*
	 * It isn't important to release buff with newer implementation.
	 * Removing finalize to save garbage collector work.
	 *
	@Override
	protected void finalize() throws Throwable {
		try {
			if(buff!=null) {
				BufferManager.release(buff, false);
				buff=null;
			}
		} finally {
			super.finalize();
		}
	}
	*/
}