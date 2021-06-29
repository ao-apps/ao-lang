/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

import com.aoapps.lang.i18n.Resources;
import java.io.Serializable;

/**
 * Extends <code>IllegalArgumentException</code> to provide exceptions in user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedIllegalArgumentException extends IllegalArgumentException {

	private static final long serialVersionUID = 3L;

	protected final Resources resources;
	protected final String key;
	protected final Serializable[] args;

	public LocalizedIllegalArgumentException(Resources resources, String key) {
		super(resources.getMessage(key));
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	public LocalizedIllegalArgumentException(Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args));
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	public LocalizedIllegalArgumentException(Throwable cause, Resources resources, String key) {
		super(resources.getMessage(key), cause);
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	public LocalizedIllegalArgumentException(Throwable cause, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), cause);
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	@Override
	public String getLocalizedMessage() {
		return resources.getMessage(key, (Object[])args);
	}

	static {
		Throwables.registerSurrogateFactory(LocalizedIllegalArgumentException.class, (template, cause) ->
			new LocalizedIllegalArgumentException(cause, template.resources, template.key, template.args)
		);
	}
}
