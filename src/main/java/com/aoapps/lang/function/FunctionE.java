/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022, 2023  AO Industries, Inc.
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

import java.util.Objects;
import java.util.function.Function;

/**
 * A function that is allowed to throw a checked exception.
 *
 * @param  <Ex>  An arbitrary exception type that may be thrown
 *
 * @see Function
 */
@FunctionalInterface
public interface FunctionE<T, R, Ex extends Throwable> {

  R apply(T t) throws Ex;

  default <V> FunctionE<V, R, Ex> compose(FunctionE<? super V, ? extends T, ? extends Ex> before) throws Ex {
    Objects.requireNonNull(before);
    return v -> apply(before.apply(v));
  }

  default <V> FunctionE<T, V, Ex> andThen(FunctionE<? super R, ? extends V, ? extends Ex> after) throws Ex {
    Objects.requireNonNull(after);
    return t -> after.apply(apply(t));
  }

  static <T> FunctionE<T, T, RuntimeException> identity() {
    return t -> t;
  }
}
