/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.lang;

import com.aoindustries.exception.WrappedException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

/**
 * Utilities that help when working with {@link Package} and/or Maven projects.
 * <p>
 * See <a href="https://stackoverflow.com/questions/2712970/get-maven-artifact-version-at-runtime">java - Get Maven artifact version at runtime - Stack Overflow</a>.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public final class Projects {

	/**
	 * Make no instances.
	 */
	private Projects() {
	}

	/**
	 * Reads the <code>pom.properties</code> from the given source.
	 */
	public static String readVersion(Function<String,InputStream> getResourceAsStream, String groupId, String artifactId) throws IOException {
		String version = null;
		try (InputStream in = getResourceAsStream.apply("/META-INF/maven/" + groupId + '/' + artifactId + "/pom.properties")) {
			if(in != null) {
				Properties p = new Properties();
				p.load(in);
				version = p.getProperty("version");
			}
		}
		return version;
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
		} catch(IOException e) {
			throw new WrappedException(e);
		}
	}

	/**
	 * Gets the version from a Maven <code>pom.properties</code> file and the given classloader.
	 *
	 * @param  cl  The classloader to use.  When {@code null}, will use {@link ClassLoader#getSystemResourceAsStream(java.lang.String)}.
	 *
	 * @return  The version or {@code null} when not found.
	 */
	public static String getVersion(ClassLoader cl, String groupId, String artifactId, String def) {
		String version = getVersion(cl, groupId, artifactId);
		return (version != null) ? version : def;
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
	 * Gets the version from a Maven <code>pom.properties</code> file.
	 * <p>
	 * Searches the unnamed module via {@link ClassLoader#getResourceAsStream(java.lang.String)}.
	 * </p>
	 *
	 * @return  The version or {@code null} when not found.
	 */
	public static String getVersion(String groupId, String artifactId, String def) {
		String version = getVersion(groupId, artifactId);
		return (version != null) ? version : def;
	}

	/**
	 * Gets the version from {@link Package#getImplementationVersion()}, falling back
	 * to {@link Package#getSpecificationVersion()}.
	 *
	 * @return  The version or {@code null} when not found.
	 */
	public static String getVersion(Package pk) {
		if(pk == null) return null;
		String version = pk.getImplementationVersion();
		if(version == null) {
			version = pk.getSpecificationVersion();
		}
		return version;
	}

	/**
	 * Gets the version from {@link Package#getImplementationVersion()}, falling back
	 * to {@link Package#getSpecificationVersion()}.
	 *
	 * @return  The version or {@code null} when not found.
	 */
	public static String getVersion(Package pk, String def) {
		String version = getVersion(pk);
		return (version != null) ? version : def;
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
			if(version == null) {
				version = getVersion(clazz.getPackage());
			}
			return version;
		} catch(IOException e) {
			throw new WrappedException(e);
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
	public static String getVersion(Class<?> clazz, String groupId, String artifactId, String def) {
		String version = getVersion(clazz, groupId, artifactId);
		return (version != null) ? version : def;
	}
}
