/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2018  AO Industries, Inc.
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
package com.aoindustries.lang.reflect;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utilities for dealing with {@link Class classes}.
 *
 * @author  AO Industries, Inc.
 */
public final class Classes {

	/**
	 * Make no instances.
	 */
	private Classes() {
	}

	/**
	 * Gets all classes and interfaces for a class, up to and including the given upper bound.
	 * <p>
	 * More precisely: gets all the classes that the given class either extends
	 * or implements, including all its parent classes and interfaces implemented
	 * by parent classes, that are {@link Class#isAssignableFrom(java.lang.Class)
	 * assignable from} the given upper bound.
	 * </p>
	 */
	// TODO: Add all interface parents and break loop when already in set.  See recent UdtMap development.
	public static <T> Set<Class<? extends T>> getAllClasses(Class<? extends T> clazz, Class<T> upperBound) {
		Set<Class<? extends T>> classes = new LinkedHashSet<Class<? extends T>>();
		Class<?> current = clazz;
		do {
			if(upperBound.isAssignableFrom(current)) classes.add(current.asSubclass(upperBound));
			for(Class<?> iface : clazz.getInterfaces()) {
				do {
					if(upperBound.isAssignableFrom(iface)) classes.add(iface.asSubclass(upperBound));
				} while ((iface = iface.getSuperclass()) != null);
			}
		} while((current = current.getSuperclass()) != null);
		return classes;
	}

	/**
	 * Gets all classes and interfaces for a class.
	 * <p>
	 * More precisely: gets all the classes that the given class either extends
	 * or implements, including all its parent classes and interfaces implemented
	 * by parent classes.
	 * </p>
	 */
	public static Set<Class<?>> getAllClasses(Class<?> clazz) {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		Class<?> current = clazz;
		do {
			classes.add(current);
			classes.addAll(Arrays.asList(clazz.getInterfaces()));
		} while((current = current.getSuperclass()) != null);
		return classes;
	}
}
