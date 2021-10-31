/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2016, 2017, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

import com.aoapps.lang.i18n.Resources;
import java.util.ResourceBundle;

/**
 * Indicates a null argument was passed where not allowed.
 *
 * @author  AO Industries, Inc.
 */
public class NullArgumentException extends IllegalArgumentException {

	private static final Resources RESOURCES = Resources.getResources(
		ResourceBundle::getBundle,
		NullArgumentException.class.getPackage(),
		"i18n_res.ApplicationResources",
		NullArgumentException.class.getSimpleName() + '.'
	);

	private static final long serialVersionUID = 1L;

	/**
	 * Checks an argument and throws an exception if null.
	 */
	public static <T> T checkNotNull(T argument) throws NullArgumentException {
		return checkNotNull(argument, null);
	}

	/**
	 * Checks an argument and throws an exception if null.
	 */
	public static <T> T checkNotNull(T argument, String argumentName) throws NullArgumentException {
		if(argument==null) throw new NullArgumentException(argumentName);
		return argument;
	}

	private final String argument;

	public NullArgumentException(String argument) {
		super(argument==null ? RESOURCES.getMessage("message.noName") : RESOURCES.getMessage("message", argument));
		this.argument = argument;
	}

	@Override
	public String getLocalizedMessage() {
		return argument==null ? RESOURCES.getMessage("message.noName") : RESOURCES.getMessage("message", argument);
	}

	/**
	 * Gets the name of the argument that was null.
	 */
	public String getArgument() {
		return argument;
	}

	static {
		Throwables.registerSurrogateFactory(NullArgumentException.class, (template, cause) -> {
			NullArgumentException newEx = new NullArgumentException(template.argument);
			newEx.initCause(cause);
			return newEx;
		});
	}
}
