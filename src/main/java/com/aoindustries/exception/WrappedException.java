/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020  AO Industries, Inc.
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
package com.aoindustries.exception;

import java.util.concurrent.Callable;

/**
 * <p>
 * A wrapped exception may be used to rethrow checked exceptions in a context
 * where they are otherwise not allowed.
 * </p>
 * <p>
 * This could be accomplished by
 * rethrowing with {@link RuntimeException} directly, but having this distinct
 * class provides more meaning as well as the ability to catch wrapped
 * exceptions while letting all other runtime exceptions go through directly.
 * </p>
 * <p>
 * Catching wrapped exceptions may be used to unwrapped expected exception types.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
// TODO: Review uses of this class and convert to wrapChecked where cleaner
public class WrappedException extends RuntimeException {

	private static final long serialVersionUID = -987777760527780052L;

	public static <V> V wrapChecked(Callable<V> callable) {
		try {
			return callable.call();
		} catch(Error | RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new WrappedException(t);
		}
	}

	public static <V> V wrapChecked(Callable<V> callable, Object... extraInfo) {
		try {
			return callable.call();
		} catch(Error | RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new WrappedException(t, extraInfo);
		}
	}

	public static <V> V wrapChecked(Callable<V> callable, String message) {
		try {
			return callable.call();
		} catch(Error | RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new WrappedException(message, t);
		}
	}

	public static <V> V wrapChecked(Callable<V> callable, String message, Object... extraInfo) {
		try {
			return callable.call();
		} catch(Error | RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new WrappedException(message, t, extraInfo);
		}
	}

	private final Object[] extraInfo;

	/**
	 * @deprecated Please provide cause.
	 */
	@Deprecated
	public WrappedException() {
		super();
		this.extraInfo=null;
	}

	/**
	 * @deprecated Please provide cause.
	 */
	@Deprecated
	public WrappedException(String message) {
		super(message);
		this.extraInfo=null;
	}

	public WrappedException(Throwable cause) {
		super(cause);
		this.extraInfo=null;
	}

	public WrappedException(Throwable cause, Object... extraInfo) {
		super(cause);
		this.extraInfo=extraInfo;
	}

	public WrappedException(String message, Throwable cause) {
		super(message, cause);
		this.extraInfo=null;
	}

	public WrappedException(String message, Throwable cause, Object... extraInfo) {
		super(message, cause);
		this.extraInfo=extraInfo;
	}

	public Object[] getExtraInfo() {
		return extraInfo;
	}
}
