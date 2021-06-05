/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2021  AO Industries, Inc.
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

import java.io.IOException;
import java.io.Writer;

/**
 * Wraps a writer while passing-through all methods of the writer class.
 * This is used to hide the implementation of writer from calling classes.
 *
 * @author  AO Industries, Inc.
 */
final public class WriterFacade extends Writer {

	/**
	 * Gets an instance of the WriterFacade that wraps the given Writer.
	 * If the provided writer is already a WriterFacade, returns it without additional wrapping.
	 */
	public static WriterFacade getInstance(Writer out) {
		if(out instanceof WriterFacade) return (WriterFacade)out;
		return new WriterFacade(out);
	}

	private final Writer out;

	private WriterFacade(Writer out) {
		assert !(out instanceof WriterFacade);
		this.out = out;
	}

	@Override
	public void write(int c) throws IOException {
		out.write(c);
	}

	@Override
	public void write(char cbuf[]) throws IOException {
		out.write(cbuf);
	}

	@Override
	public void write(char cbuf[], int off, int len) throws IOException  {
		out.write(cbuf, off, len);
	}

	@Override
	public void write(String str) throws IOException {
		out.write(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		out.write(str, off, len);
	}

	@Override
	public WriterFacade append(CharSequence csq) throws IOException {
		out.append(csq);
		return this;
	}

	@Override
	public WriterFacade append(CharSequence csq, int start, int end) throws IOException {
		out.append(csq, start, end);
		return this;
	}

	@Override
	public WriterFacade append(char c) throws IOException {
		out.append(c);
		return this;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}
