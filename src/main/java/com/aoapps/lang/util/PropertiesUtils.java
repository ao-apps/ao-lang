/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoapps.lang.util;

import com.aoapps.lang.i18n.Resources;
import com.aoapps.lang.io.LocalizedIOException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Property utilities.
 *
 * <p>See <a href="https://oss.aoapps.com/servlet-util/apidocs/com.aoapps.servlet.util/com/aoapps/servlet/PropertiesUtils.html">com.aoapps.servlet.PropertiesUtils</a> for use in servlet environment</p>
 */
public final class PropertiesUtils {

  /** Make no instances. */
  private PropertiesUtils() {
    throw new AssertionError();
  }

  public static final Resources RESOURCES = Resources.getResources(ResourceBundle::getBundle, PropertiesUtils.class);

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
   * Loads properties from a module or classpath resource.
   * <ol>
   * <li>Attempts to locate the resource with {@link Class#getResourceAsStream(java.lang.String)}</li>
   * <li>If resource name begins with a slash (/):
   *   <ol type="a">
   *   <li>Strip all beginning slashes (/) from resource name</li>
   *   <li>
   *     If {@link Thread#getContextClassLoader()} is non-null, attempts to locate the resource with
   *     {@link ClassLoader#getResourceAsStream(java.lang.String)}.
   *   </li>
   *   <li>
   *     Otherwise, attempts to locate the resource with
   *     {@link ClassLoader#getSystemResourceAsStream(java.lang.String)}.
   *   </li>
   *   </ol>
   * </ol>
   */
  public static Properties loadFromResource(Class<?> clazz, String resource) throws IOException {
    Properties props = new Properties();
    InputStream in = clazz.getResourceAsStream(resource);
    if (in == null && resource.startsWith("/")) {
      // Try ClassLoader for when modules enabled
      String name = resource;
      do {
        name = name.substring(1);
      } while (name.startsWith("/"));
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      in = (classloader != null)
          ? classloader.getResourceAsStream(name)
          : ClassLoader.getSystemResourceAsStream(name);
    }
    if (in == null) {
      throw new LocalizedIOException(RESOURCES, "readProperties.resourceNotFound", resource);
    }
    try {
      props.load(in);
    } finally {
      in.close();
    }
    return props;
  }
}
