/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017  AO Industries, Inc.
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
package com.aoindustries.util;

/**
 * @author  AO Industries, Inc.
 */
public class WrappedException extends RuntimeException {

	private static final long serialVersionUID = -987777760527780052L;

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

	public WrappedException(Throwable cause, Object[] extraInfo) {
		super(cause);
		this.extraInfo=extraInfo;
	}

	public WrappedException(String message, Throwable cause) {
		super(message, cause);
		this.extraInfo=null;
	}

	public WrappedException(String message, Throwable cause, Object[] extraInfo) {
		super(message, cause);
		this.extraInfo=extraInfo;
	}

	public Object[] getExtraInfo() {
		return extraInfo;
	}
}