/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.util.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that is allowed to throw a checked exception.
 *
 * @see Function
 */
@FunctionalInterface
public interface FunctionE<T, R, E extends Throwable> {

    R apply(T t) throws E;

	default <V> FunctionE<V, R, E> compose(FunctionE<? super V, ? extends T, ? extends E> before) throws E {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

	default <V> FunctionE<T, V, E> andThen(FunctionE<? super R, ? extends V, ? extends E> after) throws E {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

	static <T,E extends Throwable> FunctionE<T, T, E> identity() {
        return t -> t;
    }
}
