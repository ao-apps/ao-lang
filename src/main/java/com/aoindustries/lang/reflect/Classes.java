/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2018, 2019  AO Industries, Inc.
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

	private static <T> void addAllClasses(Set<Class<? extends T>> classes, Set<Class<?>> notAssignable, Class<?> current, Class<T> upperBound) {
		while(current != null) {
			if(upperBound.isAssignableFrom(current)) {
				if(classes.add(current.asSubclass(upperBound))) {
					for(Class<?> iface : current.getInterfaces()) {
						addAllClasses(classes, notAssignable, iface, upperBound);
					}
				} else {
					// This class has already been mapped
					break;
				}
			} else {
				if(notAssignable.add(current)) {
					for(Class<?> iface : current.getInterfaces()) {
						addAllClasses(classes, notAssignable, iface, upperBound);
					}
				} else {
					// This class has already been mapped
					break;
				}
			}
			current = current.getSuperclass();
		}
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
	public static <T> Set<Class<? extends T>> getAllClasses(Class<? extends T> clazz, Class<T> upperBound) {
		Set<Class<? extends T>> classes = new LinkedHashSet<>();
		Set<Class<?>> notAssignable = new LinkedHashSet<>();
		addAllClasses(classes, notAssignable, clazz, upperBound);
		return classes;
	}

	private static void addAllClasses(Set<Class<?>> classes, Class<?> current) {
		while(current != null) {
			if(classes.add(current)) {
				for(Class<?> iface : current.getInterfaces()) {
					addAllClasses(classes, iface);
				}
			} else {
				// This class has already been mapped
				break;
			}
			current = current.getSuperclass();
		}
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
		Set<Class<?>> classes = new LinkedHashSet<>();
		addAllClasses(classes, clazz);
		return classes;
	}
}
