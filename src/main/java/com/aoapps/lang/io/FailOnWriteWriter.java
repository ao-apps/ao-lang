/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.i18n.Resources;
import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;

public final class FailOnWriteWriter extends Writer implements NoClose {

	private static final Resources RESOURCES = Resources.getResources(ResourceBundle::getBundle, FailOnWriteWriter.class);

	private static final FailOnWriteWriter instance = new FailOnWriteWriter();

	public static FailOnWriteWriter getInstance() {
		return instance;
	}

	private FailOnWriteWriter() {
		// Do nothing
	}

	@Override
	public void write(int c) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public void write(String str) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public FailOnWriteWriter append(CharSequence csq) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public FailOnWriteWriter append(CharSequence csq, int start, int end) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public FailOnWriteWriter append(char c) throws IOException {
		throw new LocalizedIOException(RESOURCES, "noOutputAllowed");
	}

	@Override
	public void flush() {
		// Do nothing
	}

	@Override
	public void close() {
		// Do nothing
	}
}
