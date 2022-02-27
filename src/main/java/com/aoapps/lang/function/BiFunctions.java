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
package com.aoapps.lang.function;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Utilities for working with {@link BiFunction} and {@link BiFunctionE}.
 *
 * @see BiFunction
 * @see BiFunctionE
 */
public final class BiFunctions {

	/** Make no instances. */
	private BiFunctions() {throw new AssertionError();}

	/**
	 * Lazily evaluates a set of bifunctions, returning the first non-null result or {@link Optional#EMPTY} when no result.
	 *
	 * @return  The first non-null result or {@link Optional#EMPTY} when all return {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T, U, R> Optional<R> coalesce(T t, U u, BiFunction<? super T, ? super U, ? extends R> ... functions) {
		R r = null;
		if(functions != null) {
			for(BiFunction<? super T, ? super U, ? extends R> function : functions) {
				r = function.apply(t, u);
				if(r != null) break;
			}
		}
		return Optional.ofNullable(r);
	}

	/**
	 * Lazily evaluates a set of bifunctions, returning the first non-null result or {@link Optional#EMPTY} when no result.
	 *
	 * @return  The first non-null result or {@link Optional#EMPTY} when all return {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T, U, R, Ex extends Throwable> Optional<R> coalesceE(T t, U u, BiFunctionE<? super T, ? super U, ? extends R, ? extends Ex> ... functions) throws Ex {
		R r = null;
		if(functions != null) {
			for(BiFunctionE<? super T, ? super U, ? extends R, ? extends Ex> function : functions) {
				r = function.apply(t, u);
				if(r != null) break;
			}
		}
		return Optional.ofNullable(r);
	}
}
