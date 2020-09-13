/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2012, 2016, 2017, 2020  AO Industries, Inc.
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
package com.aoindustries.lang;

/**
 * A method has been invoked on a disposed object.
 *
 * @see Disposable
 *
 * @deprecated  Please use {@link IllegalStateException}
 */
@Deprecated
public class DisposedException extends IllegalStateException {

	private static final long serialVersionUID = 2L;

	public DisposedException() {
		super();
	}

	public DisposedException(String message) {
		super(message);
	}

	public DisposedException(Throwable cause) {
		super(cause);
	}

	public DisposedException(String message, Throwable cause) {
		super(message, cause);
	}
}
