/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.attribute;

import java.util.Enumeration;

/**
 * A specifically resolved context.
 * <p>
 * {@link Attribute}: Has scope and context, still needs name.
 * </p>
 *
 * @see  Attribute
 * @see  Scope
 *
 * @author  AO Industries, Inc.
 */
public abstract class Context<C> {

	protected Context() {
		// Do nothing
	}

	/**
	 * Gets the scope for this context.
	 */
	public abstract Scope<C> getScope();

	/**
	 * {@link Attribute}: Uses the given name within this scope and context.
	 */
	public abstract <T> Attribute<C, T> attribute(String name);

	/**
	 * Gets the attribute names within this scope and context.
	 */
	public abstract Enumeration<String> getAttributeNames();
}
