/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017, 2019  AO Industries, Inc.
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
package com.aoindustries.util;

import com.aoindustries.io.LocalizedIOException;
import static com.aoindustries.util.ApplicationResourcesAccessor.accessor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Property utilities.
 *
 * @see  com.aoindustries.servlet.PropertiesUtils  for use in servlet environment
 */
final public class PropertiesUtils {

	/**
	 * Make no instances.
	 */
	private PropertiesUtils() {}

	/**
	 * Loads properties from a file.
	 */
	public static Properties loadFromFile(File file) throws IOException {
		Properties props = new Properties();
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			props.load(in);
		}
		return props;
	}

	/**
	 * Loads properties from a classpath resource.
	 */
	public static Properties loadFromResource(Class<?> clazz, String resource) throws IOException {
		Properties props = new Properties();
		InputStream in = clazz.getResourceAsStream(resource);
		if(in==null) throw new LocalizedIOException(accessor, "PropertiesUtils.readProperties.resourceNotFound", resource);
		try {
			props.load(in);
		} finally {
			in.close();
		}
		return props;
	}
}
