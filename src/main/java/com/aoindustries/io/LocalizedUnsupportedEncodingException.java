/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020  AO Industries, Inc.
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

import com.aoindustries.lang.EmptyArrays;
import com.aoindustries.util.i18n.ApplicationResourcesAccessor;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Extends {@link UnsupportedEncodingException} to provide exceptions with user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedUnsupportedEncodingException extends UnsupportedEncodingException {

	private static final long serialVersionUID = 1L;

	private final ApplicationResourcesAccessor accessor;
	private final String key;
	private final Serializable[] args;

	public LocalizedUnsupportedEncodingException(ApplicationResourcesAccessor accessor, String key) {
		super(accessor.getMessage(key));
		this.accessor = accessor;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	public LocalizedUnsupportedEncodingException(ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		super(accessor.getMessage(key, (Object[])args));
		this.accessor = accessor;
		this.key = key;
		this.args = args;
	}

	public LocalizedUnsupportedEncodingException(Throwable cause, ApplicationResourcesAccessor accessor, String key) {
		super(accessor.getMessage(key));
		this.accessor = accessor;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
		initCause(cause);
	}

	public LocalizedUnsupportedEncodingException(Throwable cause, ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		super(accessor.getMessage(key, (Object[])args));
		this.accessor = accessor;
		this.key = key;
		this.args = args;
		initCause(cause);
	}

	@Override
	public String getLocalizedMessage() {
		return accessor.getMessage(key, (Object[])args);
	}
}
