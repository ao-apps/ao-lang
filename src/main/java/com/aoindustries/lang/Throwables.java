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
package com.aoindustries.lang;

import java.util.function.Function;

/**
 * Utilities for working with {@link Throwable}.
 */
// TODO: Automatically use cause from InvocationTargetException and WrappedException, when cause is Error, Runtime, or assignable to X?
final public class Throwables {

	/**
	 * Make no instances.
	 */
	private Throwables() {}

	/**
	 * Adds a suppressed exception, unless already in the list of suppressed exceptions.
	 * <p>
	 * When {@code suppressed} is a {@link ThreadDeath} and {@code t0} is not itself a {@link ThreadDeath},
	 * {@code suppressed} will be returned instead, with {@code t0} added to it as suppressed.
	 * This is to maintain the precedence of {@link ThreadDeath} for fail-fast behavior.
	 * </p>
	 *
	 * @param  t0  The throwable to add to.  When {@code null}, {@code suppressed} is returned instead.
	 *
	 * @param  suppressed  The suppressed throwable, skipped when {@code null}
	 *
	 * @return  {@code t0} when not null, otherwise {@code suppressed}.
	 *
	 * @see  #addSuppressedAndThrow(java.lang.Throwable, java.lang.Class, java.util.function.Function, java.lang.Throwable)
	 */
	// TODO: Rename "merge", since either one of them might be returned?
	public static Throwable addSuppressed(Throwable t0, Throwable suppressed) {
		if(suppressed != null) {
			if(t0 == null) {
				t0 = suppressed;
			} else {
				if(
					suppressed instanceof ThreadDeath
					&& !(t0 instanceof ThreadDeath)
				) {
					// Swap order to maintain fail-fast ThreadDeath
					Throwable t = t0;
					t0 = suppressed;
					suppressed = t;
				}
				boolean found = false;
				for(Throwable t : t0.getSuppressed()) {
					if(t == suppressed) {
						found = true;
						break;
					}
				}
				if(!found) {
					t0.addSuppressed(suppressed);
				}
			}
		}
		return t0;
	}

	/**
	 * Adds a suppressed exception, unless already in the list of suppressed exceptions,
	 * wrapping when needed, then throwing the result.
	 * <p>
	 * When {@code suppressed} is a {@link ThreadDeath} and {@code t0} is not itself a {@link ThreadDeath},
	 * {@code suppressed} will be returned instead, with {@code t0} added to it as suppressed.
	 * This is to maintain the precedence of {@link ThreadDeath} for fail-fast behavior.
	 * </p>
	 * <p>
	 * Only returns when both {@code t0} and {@code suppressed} are {@code null}.
	 * </p>
	 *
	 * @param  t0  The throwable to add to.  When {@code null}, {@code suppressed} is thrown instead.
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  suppressed  The suppressed throwable, skipped when {@code null}
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 *
	 * @see  #addSuppressed(java.lang.Throwable, java.lang.Throwable)
	 * @see  #wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)
	 */
	// TODO: Rename "mergeAndThrow", since either one of them might be returned?
	public static <X extends Throwable> void addSuppressedAndThrow(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		Throwable suppressed
	) throws Error, RuntimeException, X {
		t0 = addSuppressed(t0, suppressed);
		if(t0 != null) {
			if(t0 instanceof Error) throw (Error)t0;
			if(t0 instanceof RuntimeException) throw (RuntimeException)t0;
			if(xClass.isInstance(t0)) throw xClass.cast(t0);
			throw xSupplier.apply(t0);
		}
	}

	/**
	 * Wraps an exception, unless is an instance of {@code xClass}, {@link Error}, or {@link RuntimeException}.
	 * <ol>
	 * <li>When {@code null}, returns {@code null}.</li>
	 * <li>When is an instance of {@code xClass}, returns the exception.</li>
	 * <li>When is {@link Error} or {@link RuntimeException}, throws the exception directly.</li>
	 * <li>Otherwise, throws the exception wrapped via {@code xSupplier}.</li>
	 * </ol>
	 * <p>
	 * This is expected to typically used within a catch block, to throw a narrower scope:
	 * </p>
	 * <pre>try {
	 *   …
	 * } catch(Throwable t) {
	 *   throw Throwables.wrap(t, SQLException.class, SQLException::new);
	 * }</pre>
	 *
	 * @param  t  The throwable to return, throw, or wrap and return.
	 *
	 * @param  xClass  Throwables of this class are returned directly.
	 *
	 * @param  xSupplier  Throwables that a not returned directly, and are not {@link Error} or
	 *                    {@link RuntimeException}, are wrapped via this function, then returned.
	 *
	 * @return  {@code t} when is an instance of {@code xClass} or when {@code t} has been wrapped via {@code xSupplier}.
	 *
	 * @throws  Error             When {@code t} is an {@link Error}
	 * @throws  RuntimeException  When {@code t} is a {@link RuntimeException}
	 *
	 * @see  #addSuppressedAndThrow(java.lang.Throwable, java.lang.Class, java.util.function.Function, java.lang.Throwable)
	 */
	public static <X extends Throwable> X wrap(
		Throwable t,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier
	) throws Error, RuntimeException {
		if(t == null) return null;
		if(t instanceof Error) throw (Error)t;
		if(t instanceof RuntimeException) throw (RuntimeException)t;
		if(xClass.isInstance(t)) return xClass.cast(t);
		return xSupplier.apply(t);
	}
}
