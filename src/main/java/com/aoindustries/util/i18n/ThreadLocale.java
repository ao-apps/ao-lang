/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017  AO Industries, Inc.
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

import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Keeps track of the user's locale on a per-thread basis.
 *
 * @author  AO Industries, Inc.
 */
final public class ThreadLocale {

	private ThreadLocale() {}

	static final ThreadLocal<Locale> locale = new ThreadLocal<Locale>() {
		@Override
		protected Locale initialValue() {
			return Locale.getDefault();
		}
	};

	/**
	 * Gets the current thread's locale or the system default if not yet set.
	 */
	public static Locale get() {
		return locale.get();
	}

	/**
	 * Sets the current thread's locale.  The locale is not automatically
	 * restored.
	 */
	public static void set(Locale locale) {
		if(locale==null) throw new IllegalArgumentException("locale==null");
		ThreadLocale.locale.set(locale);
	}

	/**
	 * Changes the current thread locale and calls the Callable.  The locale is
	 * automatically restored.
	 */
	public static <V> V set(Locale locale, Callable<V> callable) throws Exception {
		Locale oldLocale = get();
		try {
			set(locale);
			return callable.call();
		} finally {
			set(oldLocale);
		}
	}
}
