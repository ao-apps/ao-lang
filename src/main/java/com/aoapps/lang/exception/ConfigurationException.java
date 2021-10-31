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
 * along with ao-lang.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoapps.lang.exception;

import com.aoapps.lang.Throwables;

/**
 * <p>
 * An invalid configuration has been detected.
 * This should be an abnormal condition, subject to corrective action by
 * the application administrator.
 * </p>
 * <p>
 * Once configured correctly, there should be a very low chance of this
 * exception occurring.  In other words, this exception should not generally
 * happen in response to external resources or conditions.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException() {
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	static {
		Throwables.registerSurrogateFactory(ConfigurationException.class, (template, cause) ->
			new ConfigurationException(template.getMessage(), cause)
		);
	}
}
