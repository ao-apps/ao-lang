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

package com.aoapps.lang.i18n;

import com.aoapps.lang.EmptyArrays;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A localized consumer that is allowed to throw a checked exception.
 *
 * @param  <Ex>  An arbitrary exception type that may be thrown
 *
 * @see Consumer
 *
 * @author  AO Industries, Inc.
 */
@FunctionalInterface
public interface LocalizedConsumerE<Ex extends Throwable> {

  default void accept(Resources resources, String key) throws Ex {
    accept(resources, key, EmptyArrays.EMPTY_SERIALIZABLE_ARRAY);
  }

  void accept(Resources resources, String key, Serializable... args) throws Ex;

  default LocalizedConsumerE<Ex> andThen(LocalizedConsumerE<? extends Ex> after) {
    Objects.requireNonNull(after);
    return (Resources resources, String key, Serializable... args) -> { accept(resources, key, args); after.accept(resources, key, args); };
  }
}
