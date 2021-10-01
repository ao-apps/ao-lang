/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021  AO Industries, Inc.
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

import com.aoapps.lang.EmptyArrays;
import com.aoapps.lang.Throwables;
import com.aoapps.lang.exception.LocalizedException;
import com.aoapps.lang.i18n.Resources;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Extends {@link UnsupportedEncodingException} to provide exceptions with user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedUnsupportedEncodingException extends UnsupportedEncodingException implements LocalizedException {

	private static final long serialVersionUID = 3L;

	protected final Resources resources;
	protected final String key;
	protected final Serializable[] args;

	public LocalizedUnsupportedEncodingException(Resources resources, String key) {
		super(resources.getMessage(key));
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	public LocalizedUnsupportedEncodingException(Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args));
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public LocalizedUnsupportedEncodingException(Throwable cause, Resources resources, String key) {
		super(resources.getMessage(key));
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
		if(cause != null) initCause(cause);
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public LocalizedUnsupportedEncodingException(Throwable cause, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args));
		this.resources = resources;
		this.key = key;
		this.args = args;
		if(cause != null) initCause(cause);
	}

	@Override
	public String getLocalizedMessage() {
		return resources.getMessage(key, (Object[])args);
	}

	@Override
	final public Resources getResources() {
		return resources;
	}

	@Override
	final public String getKey() {
		return key;
	}

	/**
	 * @return  No defensive copy
	 */
	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	final public Serializable[] getArgs() {
		return args;
	}

	static {
		Throwables.registerSurrogateFactory(LocalizedUnsupportedEncodingException.class, (template, cause) ->
			new LocalizedUnsupportedEncodingException(cause, template.resources, template.key, template.args)
		);
	}
}
