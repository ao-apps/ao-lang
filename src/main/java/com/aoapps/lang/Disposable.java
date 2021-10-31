/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2012, 2016, 2017, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

/**
 * Any object that should be disposed programatically.
 * <p>
 * To aid in debugging, any object that is disposable should throw {@link DisposedException}
 * when any of its methods (besides dispose itself) are accessed after being disposed.
 * </p>
 *
 * @deprecated  Please use {@link AutoCloseable}.
 */
@Deprecated
public interface Disposable {

	/**
	 * Disposes of this object instance.
	 *
	 * If already disposed, no action will be taken and no exception thrown.
	 */
	void dispose();
}
