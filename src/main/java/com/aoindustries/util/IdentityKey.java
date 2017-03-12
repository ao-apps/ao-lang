/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2016, 2017  AO Industries, Inc.
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
 * Allows any object to be used as a hash key, with identity used for
 * hashCode and equals.  They may be used, for example, to have IdentityHashMap
 * semantics with WeakHashMap references.
 *
 * Supports null value, which may allow null keys in maps that otherwise do not
 * support null keys.
 *
 * @author  AO Industries, Inc.
 */
public class IdentityKey<T> {

	private final T value;

	public IdentityKey(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value==null ? "null" : value.toString();
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	public T getValue() {
		return value;
	}
}
