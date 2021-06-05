/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang.io;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;

/**
 * Provides direct access to the internal <code>byte[]</code>
 *
 * @author  AO Industries, Inc.
 */
public class AoByteArrayInputStream extends ByteArrayInputStream {

	public AoByteArrayInputStream(byte[] buf) {
		super(buf);
	}

	/**
	 * Provides direct access to the internal byte[] to avoid unnecessary
	 * copying of the array.
	 */
	public byte[] getInternalByteArray() {
		return this.buf;
	}

	public void fillFrom(DataInput in) throws IOException {
		synchronized(this) {
			in.readFully(buf);
			mark=0;
			pos=0;
			count=buf.length;
		}
	}

	public void fillFrom(DataInput in, int len) throws IOException {
		synchronized(this) {
			in.readFully(buf, 0, len);
			mark=0;
			pos=0;
			count=len;
		}
	}

	/* TODO: This requires code left back in ao-hodgepodge.  Is it used anywhere?
	public void fillFrom(com.aoapps.persistence.PersistentBuffer pbuffer, long position, int len) throws IOException {
		synchronized(this) {
			pbuffer.get(position, buf, 0, len);
			mark=0;
			pos=0;
			count=len;
		}
	}
	 */
}
