/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017, 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.RunnableE;
import com.aoapps.lang.concurrent.CallableE;
import com.aoapps.lang.i18n.impl.ThreadLocaleImpl;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Keeps track of the user's locale on a per-thread basis.
 *
 * @author  AO Industries, Inc.
 */
public final class ThreadLocale {

	/** Make no instances. */
	private ThreadLocale() {throw new AssertionError();}

	/**
	 * Gets the current thread's locale or {@linkplain Locale#getDefault() the system default} if not yet set.
	 */
	public static Locale get() {
		return ThreadLocaleImpl.locale.get();
	}

	/**
	 * Sets the current thread's locale.  The locale is not automatically
	 * restored and should be restored in a try/finally or equivalent.
	 */
	public static void set(Locale locale) {
		if(locale==null) throw new IllegalArgumentException("locale==null");
		ThreadLocaleImpl.locale.set(locale);
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 */
	public static <V> V call(Locale locale, CallableE<? extends V, ? extends RuntimeException> callable) {
		return call(locale, RuntimeException.class, callable);
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 *
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoapps.lang.concurrent.CallableE)}
	 */
	@Deprecated
	public static <V> V set(Locale locale, Callable<V> callable) throws Exception {
		Locale oldLocale = get();
		try {
			set(locale);
			return callable.call();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 *
	 * @param  <Ex>  An arbitrary exception type that may be thrown
	 */
	public static <V, Ex extends Throwable> V call(Locale locale, Class<? extends Ex> eClass, CallableE<? extends V, ? extends Ex> callable) throws Ex {
		Locale oldLocale = get();
		try {
			set(locale);
			return callable.call();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 *
	 * @param  <Ex>  An arbitrary exception type that may be thrown
	 *
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoapps.lang.concurrent.CallableE)}
	 */
	@Deprecated
	public static <V, Ex extends Exception> V set(Locale locale, com.aoapps.lang.CallableE<V, Ex> callable) throws Ex {
		Locale oldLocale = get();
		try {
			set(locale);
			return callable.call();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * Changes the current thread locale then runs the Runnable.  The locale is
	 * automatically restored.
	 */
	public static void run(Locale locale, Runnable runnable) {
		Locale oldLocale = get();
		try {
			set(locale);
			runnable.run();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * Changes the current thread locale then runs the RunnableE.  The locale is
	 * automatically restored.
	 *
	 * @param  <Ex>  An arbitrary exception type that may be thrown
	 */
	public static <Ex extends Throwable> void run(Locale locale, Class<? extends Ex> eClass, RunnableE<? extends Ex> runnable) throws Ex {
		Locale oldLocale = get();
		try {
			set(locale);
			runnable.run();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * Changes the current thread locale then gets the result from the Supplier.  The locale is
	 * automatically restored.
	 *
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoapps.lang.concurrent.CallableE)} with
	 *              {@link RuntimeException}
	 */
	@Deprecated
	public static <V> V set(Locale locale, java.util.function.Supplier<V> supplier) {
		Locale oldLocale = get();
		try {
			set(locale);
			return supplier.get();
		} finally {
			set(oldLocale);
		}
	}

	/**
	 * @deprecated  Please use {@link java.util.function.Supplier} directly.
	 */
	@Deprecated
	@FunctionalInterface
	public static interface Supplier<T> extends java.util.function.Supplier<T> {
		@Override
		T get();
	}

	/**
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoapps.lang.concurrent.CallableE)} with
	 *              {@link RuntimeException}
	 */
	@Deprecated
	public static <V> V set(Locale locale, Supplier<V> supplier) {
		return set(locale, (java.util.function.Supplier<V>)supplier);
	}
}
