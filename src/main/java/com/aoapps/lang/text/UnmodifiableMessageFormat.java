/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.text;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * A MessageFormat that may not be modified.  This allows the message format to be
 * cached while avoiding any accidental change of the cached object.
 *
 * @author  AO Industries, Inc.
 */
public class UnmodifiableMessageFormat extends MessageFormat {

	private static final long serialVersionUID = 1L;

	private boolean initCompleted = false;

	/* Unused 2013-09-27
	public UnmodifiableMessageFormat(String pattern) {
		super(pattern);
		initCompleted = true;
	} */

	public UnmodifiableMessageFormat(String pattern, Locale locale) {
		super(pattern, locale);
		initCompleted = true;
	}

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setLocale(Locale locale) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException except when called from constructor.
	 */
	@Override
	public void applyPattern(String pattern) throws UnsupportedOperationException {
		if(initCompleted) throw new UnsupportedOperationException();
		super.applyPattern(pattern);
	}

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setFormatsByArgumentIndex(Format[] newFormats) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setFormats(Format[] newFormats) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setFormat(int formatElementIndex, Format newFormat) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
