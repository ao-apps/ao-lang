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
package com.aoindustries.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A predicate that is allowed to throw {@link IOException} and a checked exception.
 *
 * @see Predicate
 */
@FunctionalInterface
public interface IOPredicateE<T, E extends Throwable> {

	boolean test(T t) throws IOException, E;

	default IOPredicateE<T, E> and(IOPredicateE<? super T, ? extends E> other) throws IOException, E {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	default IOPredicateE<T, E> negate() throws IOException, E {
		return (t) -> !test(t);
	}

	default IOPredicateE<T, E> or(IOPredicateE<? super T, ? extends E> other) throws IOException, E {
		Objects.requireNonNull(other);
		return (t) -> test(t) || other.test(t);
	}

	static <T, E extends Throwable> IOPredicateE<T, E> isEqual(Object targetRef) {
		return (null == targetRef)
			? Objects::isNull
			: object -> targetRef.equals(object);
	}

	@SuppressWarnings("unchecked")
	static <T, E extends Throwable> IOPredicateE<T, E> not(IOPredicateE<? super T, ? extends E> target) throws IOException, E {
		Objects.requireNonNull(target);
		return (IOPredicateE<T, E>)target.negate();
	}
}
