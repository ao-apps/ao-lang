/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2013, 2016, 2017  AO Industries, Inc.
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
package com.aoindustries.text;

import com.aoindustries.util.i18n.ThreadLocale;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Creates MessageFormat objects and caches them.  This may be used as an alternative
 * to the MessageFormat constructor to increase performance.
 *
 * The following are the results of a single-threaded benchmark with a simple two-value substitution:
 * <pre>
 * Comparing constructor/factory and format together:
 *     Constructor: 1148.127153
 *     Factory....: 283.366235
 *     4 times improvement
 *
 * Comparing constructor/factory only:
 *     Constructor: 985.182660
 *     Factory....: 42.602040
 *     23 times improvement
 * </pre>
 * Longer or more complicated patterns should benefit even more (this assertion not tested).
 *
 * @author  AO Industries, Inc.
 */
public class MessageFormatFactory {

	private static final ConcurrentMap<Locale,ConcurrentMap<String,UnmodifiableMessageFormat>> cache = new ConcurrentHashMap<Locale,ConcurrentMap<String,UnmodifiableMessageFormat>>();

	/**
	 * Gets a message format for the provided format in the current thread locale.
	 *
	 * @return  a MessageFormat that may not be modified with any of its setters or other mutator methods.
	 * 
	 * @see ThreadLocale
	 */
	public static UnmodifiableMessageFormat getMessageFormat(String pattern) {
		return getMessageFormat(pattern, ThreadLocale.get());
	}

	/**
	 * Gets a message format for the provided format and locale.
	 *
	 * @return  a MessageFormat that may not be modified with any of its setters or other mutator methods.
	 */
	public static UnmodifiableMessageFormat getMessageFormat(String pattern, Locale locale) {
		ConcurrentMap<String,UnmodifiableMessageFormat> localeCache = cache.get(locale);
		if(localeCache==null) {
			localeCache = new ConcurrentHashMap<String,UnmodifiableMessageFormat>();
			ConcurrentMap<String,UnmodifiableMessageFormat> existing = cache.putIfAbsent(locale, localeCache);
			if(existing!=null) localeCache = existing;
		}
		UnmodifiableMessageFormat messageFormat = localeCache.get(pattern);
		if(messageFormat==null) {
			messageFormat = new UnmodifiableMessageFormat(pattern, locale);
			UnmodifiableMessageFormat existing = localeCache.putIfAbsent(pattern, messageFormat);
			if(existing!=null) messageFormat = existing;
		}
		return messageFormat;
	}

	private MessageFormatFactory() {
	}

	/*
	public static void benchmark() {
		Locale locale = Locale.getDefault();
		int iterations = 1000000;
		StringBuffer sb = new StringBuffer();
		Object[] args = new Object[] {
			"test", "message"
		};
		long startTime = System.nanoTime();
		for(int i=0; i<iterations; i++) {
			//sb.setLength(0);
			new MessageFormat("This is a {0} {1}", locale); //.format(args, sb, null);
		}
		long midTime = System.nanoTime();
		for(int i=0; i<iterations; i++) {
			//sb.setLength(0);
			MessageFormatFactory.getMessageFormat("This is a {0} {1}", locale); //.format(args, sb, null);
		}
		long endTime = System.nanoTime();
		System.out.println("Constructor: "+BigDecimal.valueOf(midTime - startTime, 6));
		System.out.println("Factory....: "+BigDecimal.valueOf(endTime - midTime, 6));
	}

	public static void main(String[] args) {
		for(int c=0;c<100;c++) benchmark();
	}
	 */
}
