/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * Sorts locales by language, country, then variant.
 *
 * @author  AO Industries, Inc.
 */
public final class LocaleComparator implements Comparator<Locale>, Serializable {

	private static final long serialVersionUID = 7238956708102131937L;

	private static final LocaleComparator instance = new LocaleComparator();

	public static LocaleComparator getInstance() {
		return instance;
	}

	private LocaleComparator() {
		// Do nothing
	}

	private Object readResolve() {
		return getInstance();
	}

	@Override
	public int compare(Locale l1, Locale l2) {
		int diff = l1.getLanguage().compareToIgnoreCase(l2.getLanguage());
		if(diff!=0) return diff;
		diff = l1.getCountry().compareToIgnoreCase(l2.getCountry());
		if(diff!=0) return diff;
		return l1.getVariant().compareToIgnoreCase(l2.getVariant());
	}
}
