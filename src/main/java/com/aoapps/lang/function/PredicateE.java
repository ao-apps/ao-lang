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

package com.aoapps.lang.function;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A predicate that is allowed to throw a checked exception.
 *
 * @param  <Ex>  An arbitrary exception type that may be thrown
 *
 * @see Predicate
 */
@FunctionalInterface
public interface PredicateE<T, Ex extends Throwable> {

  boolean test(T t) throws Ex;

  default PredicateE<T, Ex> and(PredicateE<? super T, ? extends Ex> other) throws Ex {
    Objects.requireNonNull(other);
    return t -> test(t) && other.test(t);
  }

  default PredicateE<T, Ex> negate() throws Ex {
    return t -> !test(t);
  }

  default PredicateE<T, Ex> or(PredicateE<? super T, ? extends Ex> other) throws Ex {
    Objects.requireNonNull(other);
    return t -> test(t) || other.test(t);
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   */
  static <T, Ex extends Throwable> PredicateE<T, Ex> isEqual(Object targetRef) {
    return (null == targetRef)
        ? Objects::isNull
        : targetRef::equals;
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   */
  @SuppressWarnings("unchecked")
  static <T, Ex extends Throwable> PredicateE<T, Ex> not(PredicateE<? super T, ? extends Ex> target) throws Ex {
    Objects.requireNonNull(target);
    return (PredicateE<T, Ex>) target.negate();
  }
}
