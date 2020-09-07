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

/**
 * Utilities for working with {@link AutoCloseable},
 */
// TODO: Use this many place where makes code cleaner
final public class AutoCloseables {

	/**
	 * Make no instances.
	 */
	private AutoCloseables() {}

	/**
	 * Closes the given {@link AutoCloseable}, catching all {@link Throwable} except {@link ThreadDeath}.
	 *
	 * @param  t1  If not {@code null}, any new throwables will be added via {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @return  The given throwable, a new throwable, or {@code null} when none given and none new
	 */
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public static Throwable close(Throwable t1, AutoCloseable closeable) {
		if(closeable != null) {
			try {
				closeable.close();
			} catch(ThreadDeath td) {
				throw td;
			} catch(Throwable t) {
				t1 = Throwables.addSuppressed(t1, t);
			}
		}
		return t1;
	}

	/**
	 * Closes the given {@link AutoCloseable}, catching all {@link Throwable} except {@link ThreadDeath}.
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable close(AutoCloseable closeable) {
		return close(null, closeable);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable} except {@link ThreadDeath}.
	 *
	 * @param  t1  If not {@code null}, any new throwables will be added via {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @return  The given throwable, a new throwable, or {@code null} when none given and none new
	 */
	public static Throwable close(Throwable t1, AutoCloseable ... closeable) {
		if(closeable != null) {
			for(AutoCloseable ac : closeable) {
				t1 = close(t1, ac);
			}
		}
		return t1;
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable} except {@link ThreadDeath}.
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable close(AutoCloseable ... closeable) {
		return close(null, closeable);
	}
}
