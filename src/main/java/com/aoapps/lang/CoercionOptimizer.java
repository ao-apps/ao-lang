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

package com.aoapps.lang;

import com.aoapps.lang.io.Encoder;
import java.io.Writer;

/**
 * Implements the optimization of {@link Coercion}.
 *
 * @see  Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)
 * @see  Coercion#optimize(java.lang.Appendable, com.aoapps.lang.io.Encoder)
 * @see  Coercion#registerOptimizer(com.aoapps.lang.CoercionOptimizer)
 * @see  CoercionOptimizerInitializer
 */
@FunctionalInterface
public interface CoercionOptimizer {

	/**
	 * Unwraps a writer to expose any wrapped writer.  The wrapped writer is
	 * only returned when it is write-through, meaning the wrapper doesn't modify
	 * the data written, and writes to the wrapped writer immediately (no buffering).
	 *
	 * @param out      the writer to try unwrapping
	 * @param encoder  the encoder being used or {@code null} for none
	 *
	 * @return  The functionally equivalent, but more efficient/direct, writer
	 *          or {@code out} when nothing to unwrap
	 */
	Writer optimize(Writer out, Encoder encoder);
}
