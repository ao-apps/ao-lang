/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.util.i18n;

import com.aoapps.lang.i18n.Resources;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @deprecated  Please use {@link Resources} directly.
 */
@Deprecated
public abstract class ApplicationResourcesAccessor implements Serializable {

	private static final long serialVersionUID = 2L;

	/**
	 * @deprecated  Please use {@link Resources#getResources(java.lang.String)} directly.
	 */
	@Deprecated
	public static ApplicationResourcesAccessor getInstance(String baseName) {
		return Resources.getResources(baseName);
	}

	/**
	 * @deprecated  Please use {@link Resources.Listener} directly.
	 */
	@Deprecated
	@FunctionalInterface
	public static interface Listener extends Resources.Listener {

		@Override
		default void onGetMessage(Resources resources, Locale locale, String key, Object[] args, String resource, String result) {
			onGetMessage((ApplicationResourcesAccessor)resources, locale, key, args, resource, result);
		}

		/**
		 * @deprecated  Please use {@link Resources.Listener#onGetMessage(com.aoapps.lang.i18n.Resources, java.util.Locale, java.lang.String, java.lang.Object[], java.lang.String, java.lang.String)} directly.
		 */
		@Deprecated
		void onGetMessage(ApplicationResourcesAccessor accessor, Locale locale, String key, Object[] args, String resource, String result);
	}

	/**
	 * @deprecated  Please use {@link Resources#addListener(com.aoapps.lang.i18n.Resources.Listener)} directly.
	 */
	@Deprecated
	public static void addListener(Listener listener) {
		Resources.addListener(listener);
	}

	/**
	 * @deprecated  Please use {@link Resources#removeListener(com.aoapps.lang.i18n.Resources.Listener)} directly.
	 */
	@Deprecated
	public static void removeListener(Listener listener) {
		Resources.removeListener(listener);
	}

	protected ApplicationResourcesAccessor() {
	}

	/**
	 * @deprecated  Please use {@link Resources#getBaseName()} directly.
	 */
	@Deprecated
	public abstract String getBaseName();

	/**
	 * @deprecated  Please use {@link Resources#getResourceBundle(java.util.Locale)} directly.
	 */
	@Deprecated
	public abstract ResourceBundle getResourceBundle(Locale locale);

	/**
	 * @deprecated  Please use {@link Resources#getMessage(java.lang.String)} directly.
	 */
	@Deprecated
	public abstract String getMessage(String key);

	/**
	 * @deprecated  Please use {@link Resources#getMessage(java.util.Locale, java.lang.String)} directly.
	 */
	@Deprecated
	public abstract String getMessage(Locale locale, String key);

	/**
	 * @deprecated  Please use {@link Resources#getMessage(java.lang.String, java.lang.Object...)} directly.
	 */
	@Deprecated
	public abstract String getMessage(String key, Object... args);

	/**
	 * @deprecated  Please use {@link Resources#getMessage(java.util.Locale, java.lang.String, java.lang.Object...)} directly.
	 */
	@Deprecated
	public abstract String getMessage(Locale locale, String key, Object... args);
}
