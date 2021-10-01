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
 * along with ao-lang.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utilities for working with various attribute scopes.
 * <p>
 * This API has three concepts:
 * </p>
 * <ol>
 *   <li>{@link Scope} - The most broad concept is scope.
 *                       Does not yet have a resolved context or attribute name.</li>
 *   <li>{@link Context} - A specifically resolved context.
 *                         Does not yet have an attribute name.</li>
 *   <li>{@link Attribute} - An attribute has both context and name and is used for value access.</li>
 *   <li>{@link Attribute.Name} - A name without any specific scope or context.</li>
 * </ol>
 * <p>
 * Ultimately, the goal is to get the an attribute, which means having both a fully resolved context and a name.  The
 * API supports arriving at an attribute in any order, such as <code>scope → context → name</code> or
 * <code>name → context</code>.
 * </p>
 * <p>
 * There is also a set of static utility methods, to which the rest of the API ends up calling.
 * For one-off attribute access, these static methods may be more succinct.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
package com.aoapps.lang.attribute;
