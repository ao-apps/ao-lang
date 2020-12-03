/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2018, 2020  AO Industries, Inc.
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
package com.aoindustries.text;

import com.aoindustries.i18n.Resources;
import com.aoindustries.lang.EmptyArrays;
import com.aoindustries.lang.Throwables;
import java.io.Serializable;
import java.text.ParseException;

/**
 * Extends {@link ParseException} to provide exceptions in user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedParseException extends ParseException {

	private static final long serialVersionUID = 2L;

	/**
	 * @deprecated  Please use {@link #resources} directly.
	 */
	@Deprecated
	protected final com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor;
	protected final Resources resources;
	protected final String key;
	protected final Serializable[] args;

	public LocalizedParseException(int errorOffset, Resources resources, String key) {
		super(resources.getMessage(key), errorOffset);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedParseException(int, com.aoindustries.i18n.Resources, java.lang.String)} directly.
	 */
	@Deprecated
	public LocalizedParseException(int errorOffset, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		this(errorOffset, (Resources)accessor, key);
	}

	public LocalizedParseException(int errorOffset, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), errorOffset);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedParseException(int, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)} directly.
	 */
	@Deprecated
	public LocalizedParseException(int errorOffset, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this(errorOffset, (Resources)accessor, key, args);
	}

	@Override
	public String getLocalizedMessage() {
		return resources.getMessage(key, (Object[])args);
	}

	static {
		Throwables.registerSurrogateFactory(LocalizedParseException.class, (template, cause) -> {
			LocalizedParseException newEx = new LocalizedParseException(
				template.getErrorOffset(), template.resources, template.key, template.args
			);
			newEx.initCause(cause);
			return newEx;
		});
	}
}
