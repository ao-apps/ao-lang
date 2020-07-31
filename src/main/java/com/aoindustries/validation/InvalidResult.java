/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011-2013, 2016, 2017, 2020  AO Industries, Inc.
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
package com.aoindustries.validation;

import com.aoindustries.lang.EmptyArrays;
import com.aoindustries.util.i18n.ApplicationResourcesAccessor;
import java.io.Serializable;

/**
 * An invalid result with a user-friendly message.
 *
 * @author  AO Industries, Inc.
 */
final public class InvalidResult implements ValidationResult {

	private static final long serialVersionUID = -105878200149461063L;

	private final ApplicationResourcesAccessor accessor;
	private final String key;
	private final Serializable[] args;

	public InvalidResult(ApplicationResourcesAccessor accessor, String key) {
		this.accessor = accessor;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	public InvalidResult(ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this.accessor = accessor;
		this.key = key;
		this.args = args;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public String toString() {
		return accessor.getMessage(key, (Object[])args);
	}

	public ApplicationResourcesAccessor getAccessor() {
		return accessor;
	}

	public String getKey() {
		return key;
	}

	public Serializable[] getArgs() {
		return args;
	}
}
