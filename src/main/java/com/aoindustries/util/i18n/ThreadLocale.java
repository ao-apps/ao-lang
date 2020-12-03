/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017, 2018, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.util.i18n;

import com.aoindustries.lang.RunnableE;
import com.aoindustries.util.concurrent.CallableE;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Keeps track of the user's locale on a per-thread basis.
 *
 * @author  AO Industries, Inc.
 */
// TODO: Move to com.aoindustries.i18n
final public class ThreadLocale {

	private ThreadLocale() {}

	static final ThreadLocal<Locale> locale = new ThreadLocal<Locale>() {
		@Override
		protected Locale initialValue() {
			return Locale.getDefault();
		}
	};

	/**
	 * Gets the current thread's locale or {@linkplain Locale#getDefault() the system default} if not yet set.
	 */
	public static Locale get() {
		return locale.get();
	}

	/**
	 * Sets the current thread's locale.  The locale is not automatically
	 * restored and should be restored in a try/finally or equivalent.
	 */
	public static void set(Locale locale) {
		if(locale==null) throw new IllegalArgumentException("locale==null");
		ThreadLocale.locale.set(locale);
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 */
	public static <V> V call(Locale locale, CallableE<? extends V,? extends RuntimeException> callable) {
		return call(locale, RuntimeException.class, callable);
	}

	/**
	 * Changes the current thread locale then calls the Callable.  The locale is
	 * automatically restored.
	 *
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoindustries.util.concurrent.CallableE)}
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
	 */
	public static <V,E extends Throwable> V call(Locale locale, Class<? extends E> eClass, CallableE<? extends V,? extends E> callable) throws E {
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
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoindustries.util.concurrent.CallableE)}
	 */
	@Deprecated
	public static <V,E extends Exception> V set(Locale locale, com.aoindustries.lang.CallableE<V,E> callable) throws E {
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
	 */
	public static <E extends Throwable> void run(Locale locale, Class<? extends E> eClass, RunnableE<? extends E> runnable) throws E {
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
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoindustries.util.concurrent.CallableE)} with
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
	 * @deprecated  Please use {@link #call(java.util.Locale, com.aoindustries.util.concurrent.CallableE)} with
	 *              {@link RuntimeException}
	 */
	@Deprecated
	public static <V> V set(Locale locale, Supplier<V> supplier) {
		return set(locale, (java.util.function.Supplier<V>)supplier);
	}
}
