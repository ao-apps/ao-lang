/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

/**
 * Utilities for working with {@link Runnable}.
 */
final public class Runnables {

	/**
	 * Make no instances.
	 */
	private Runnables() {}

	/**
	 * Runs the given {@link Runnable}, catching all {@link Throwable}.
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  runnable  The runnable to be invoked
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public static Throwable runAndCatch(Throwable t0, Runnable runnable) {
		if(runnable != null) {
			try {
				runnable.run();
			} catch(Throwable t) {
				t0 = Throwables.addSuppressed(t0, t);
			}
		}
		return t0;
	}

	/**
	 * Runs the given {@link Runnable}, catching all {@link Throwable}.
	 *
	 * @param  runnable  The runnable to be invoked
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable runAndCatch(Runnable runnable) {
		return runAndCatch(null, runnable);
	}

	/**
	 * Runs all of the given {@link Runnable} in order, catching all {@link Throwable}.
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  runnable  The set of all runnables, which will be invoked in order
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	public static Throwable runAndCatch(Throwable t0, Runnable ... runnable) {
		if(runnable != null) {
			for(Runnable ac : runnable) {
				t0 = runAndCatch(t0, ac);
			}
		}
		return t0;
	}

	/**
	 * Runs all of the given {@link Runnable} in order, catching all {@link Throwable}.
	 *
	 * @param  runnable  The set of all runnables, which will be invoked in order
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable runAndCatch(Runnable ... runnable) {
		return runAndCatch(null, runnable);
	}

	/**
	 * Runs all of the given {@link Runnable} in order, catching all {@link Throwable}.
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  runnable  The set of all runnables, which will be invoked in order
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	public static Throwable runAndCatch(Throwable t0, Iterable<? extends Runnable> runnable) {
		if(runnable != null) {
			for(Runnable ac : runnable) {
				t0 = runAndCatch(t0, ac);
			}
		}
		return t0;
	}

	/**
	 * Runs all of the given {@link Runnable} in order, catching all {@link Throwable}.
	 *
	 * @param  runnable  The set of all runnables, which will be invoked in order
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable runAndCatch(Iterable<? extends Runnable> runnable) {
		return runAndCatch(null, runnable);
	}
}
