/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.exception.WrappedError;
import com.aoapps.lang.exception.WrappedException;

/**
 * Implements the creation of {@link Throwable} surrogates.
 *
 * @see  Throwables#newSurrogate(java.lang.Throwable)
 * @see  Throwables#registerSurrogateFactory(java.lang.Class, com.aoapps.lang.ThrowableSurrogateFactory)
 * @see  ThrowableSurrogateFactoryInitializer
 */
@FunctionalInterface
public interface ThrowableSurrogateFactory<Ex extends Throwable> {

  /**
   * Creates a new {@link Throwable} surrogate, when possible to create with a compatible state.
   *
   * @param  template  The created surrogate must be of the same type as the template, and must include all
   *                   template-type-specific state.  If unable to copy the state, then no surrogate should be
   *                   created.
   *
   * @param  cause  The cause that should be used for the newly created surrogate.  This may or may not be the same as
   *                the template.  This may or may not have the template in its change of causes or suppression lists.
   *                It is allowed to further wrap the cause if necessary to create the surrogate, such as with
   *                {@link WrappedException} or {@link WrappedError}.
   *
   * @return  A new throwable instance of the same type as the template, with compatible state, and the given cause
   *          either directly or wrapped.  If unable to create a compatible surrogate, returns {@code null}.
   */
  Ex newSurrogate(Ex template, Throwable cause);
}
