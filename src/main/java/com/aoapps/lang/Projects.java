/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022  AO Industries, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities that help when working with {@link Package} and/or Maven projects.
 * <p>
 * See <a href="https://stackoverflow.com/questions/2712970/get-maven-artifact-version-at-runtime">java - Get Maven artifact version at runtime - Stack Overflow</a>.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public final class Projects {

  /** Make no instances. */
  private Projects() {
    throw new AssertionError();
  }

  private static final Logger logger = Logger.getLogger(Projects.class.getName());

  /**
   * Reads the <code>pom.properties</code> from the given input stream.
   */
  public static String readVersion(String resource, InputStream in, String groupId, String artifactId) throws IOException {
    String version = null;
    if (in != null) {
      Properties p = new Properties();
      p.load(in);
      assert groupId.equals(p.getProperty("groupId"));
      assert artifactId.equals(p.getProperty("artifactId"));
      version = p.getProperty("version");
      if (version != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Version \"" + version + "\" found from resource: " + resource);
        }
      } else {
        if (logger.isLoggable(Level.WARNING)) {
          logger.log(Level.WARNING, "Resource does not contain \"version\": " + resource);
        }
      }
    } else {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Resource not found: " + resource);
      }
    }
    return version;
  }

  /**
   * Reads the <code>pom.properties</code> from the given source.
   */
  public static String readVersion(Function<String, InputStream> getResourceAsStream, String groupId, String artifactId) throws IOException {
    String resource = "/META-INF/maven/" + groupId + '/' + artifactId + "/pom.properties";
    try (InputStream in = getResourceAsStream.apply(resource)) {
      return readVersion(resource, in, groupId, artifactId);
    }
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file and the given classloader.
   *
   * @param  cl  The classloader to use.  When {@code null}, will use {@link ClassLoader#getSystemResourceAsStream(java.lang.String)}.
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(ClassLoader cl, String groupId, String artifactId) {
    try {
      return readVersion(
          (cl == null) ? ClassLoader::getSystemResourceAsStream : cl::getResourceAsStream,
          groupId,
          artifactId
      );
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file and the given classloader,
   * falling back to the provided default.
   *
   * @param  cl  The classloader to use.  When {@code null}, will use {@link ClassLoader#getSystemResourceAsStream(java.lang.String)}.
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(ClassLoader cl, String groupId, String artifactId, String def) {
    String version = getVersion(cl, groupId, artifactId);
    if (version == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using provided default version for project \"" + groupId + ":" + artifactId + "\": " + def);
      }
      return def;
    } else {
      return version;
    }
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file.
   * <p>
   * Searches the unnamed module via {@link ClassLoader#getResourceAsStream(java.lang.String)}.
   * </p>
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(String groupId, String artifactId) {
    return getVersion(Projects.class.getClassLoader(), groupId, artifactId);
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file,
   * falling back to the provided default.
   * <p>
   * Searches the unnamed module via {@link ClassLoader#getResourceAsStream(java.lang.String)}.
   * </p>
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(String groupId, String artifactId, String def) {
    String version = getVersion(groupId, artifactId);
    if (version == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using provided default version for project \"" + groupId + ":" + artifactId + "\": " + def);
      }
      return def;
    } else {
      return version;
    }
  }

  /**
   * Gets the version from {@link Package#getImplementationVersion()}, falling back
   * to {@link Package#getSpecificationVersion()}.
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(Package pk) {
    if (pk == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "No package provided");
      }
      return null;
    }
    String version = pk.getImplementationVersion();
    if (version != null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using implementation version \"" + version + "\" for package: " + pk);
      }
      return version;
    }
    version = pk.getSpecificationVersion();
    if (version != null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using specification version \"" + version + "\" for package: " + pk);
      }
      return version;
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.log(Level.FINE, "Did not find any version information for package: " + pk);
    }
    return null;
  }

  /**
   * Gets the version from {@link Package#getImplementationVersion()}, falling back
   * to {@link Package#getSpecificationVersion()} then the provided default.
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(Package pk, String def) {
    String version = getVersion(pk);
    if (version == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using provided default version for package \"" + pk + "\": " + def);
      }
      return def;
    } else {
      return version;
    }
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file,
   * falling back to {@link Package#getImplementationVersion()} then
   * {@link Package#getSpecificationVersion()}.
   * <p>
   * Supports named modules via {@link Class#getResourceAsStream(java.lang.String)}.
   * </p>
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(Class<?> clazz, String groupId, String artifactId) {
    try {
      String version = readVersion(
          clazz::getResourceAsStream,
          groupId,
          artifactId
      );
      if (version == null) {
        version = getVersion(clazz.getPackage());
      }
      return version;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Gets the version from a Maven <code>pom.properties</code> file,
   * falling back to {@link Package#getImplementationVersion()} then
   * {@link Package#getSpecificationVersion()} then the provided default.
   * <p>
   * Supports named modules via {@link Class#getResourceAsStream(java.lang.String)}.
   * </p>
   *
   * @return  The version or {@code null} when not found.
   */
  public static String getVersion(Class<?> clazz, String groupId, String artifactId, String def) {
    String version = getVersion(clazz, groupId, artifactId);
    if (version == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "Using provided default version for project \"" + groupId + ":" + artifactId + "\" or class \"" + clazz + "\": " + def);
      }
      return def;
    } else {
      return version;
    }
  }

  private static final Pattern RELEASE = Pattern.compile("(.+)-([0-9]+)$");

  /**
   * Some projects are packaged with an additional release beyond their effective version number,
   * separated by a single hypen.
   * This release is optional, and expected to be numeric only.
   */
  public static String stripRelease(String version) {
    Matcher matcher = RELEASE.matcher(version);
    return matcher.matches() ? matcher.group(1) : version;
  }

  /**
   * Some projects are packaged with an additional release beyond their effective version number,
   * separated by a single hypen.
   * This release is optional, and expected to be numeric only.
   */
  public static String getRelease(String version) {
    Matcher matcher = RELEASE.matcher(version);
    return matcher.matches() ? matcher.group(2) : null;
  }
}
