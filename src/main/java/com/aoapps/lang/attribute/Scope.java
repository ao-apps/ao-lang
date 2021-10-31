/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2021  AO Industries, Inc.
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

import java.io.Serializable;

/**
 * The most broad concept is scope.
 * <p>
 * {@link com.aoapps.lang.attribute.Attribute}: Has scope, still needs context and name.
 * </p>
 *
 * @see  com.aoapps.lang.attribute.Attribute
 * @see  Context
 *
 * @author  AO Industries, Inc.
 */
public abstract class Scope<C> implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Scope() {}

	/**
	 * {@link com.aoapps.lang.attribute.Attribute}: Uses the given context within this scope, still needs name.
	 */
	public abstract Context<C> context(C context);

	/**
	 * {@link com.aoapps.lang.attribute.Attribute}: Has scope and name, still needs context.
	 */
	public abstract static class Attribute<C, T> implements Serializable {

		private static final long serialVersionUID = 1L;

		protected final String name;

		protected Attribute(String name) {
			this.name = name;
		}

		/**
		 * Gets the attribute name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * {@link com.aoapps.lang.attribute.Attribute}: Uses the given context within this scope and name.
		 */
		public abstract com.aoapps.lang.attribute.Attribute<C, T> context(C context);
	}

	/**
	 * {@link com.aoapps.lang.attribute.Attribute}: Uses the given name within this scope, still needs context.
	 */
	public abstract <T> Attribute<C, T> attribute(String name);
}
