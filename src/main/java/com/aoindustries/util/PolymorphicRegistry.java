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
package com.aoindustries.util;

import com.aoindustries.lang.reflect.Classes;
import com.aoindustries.util.function.Predicate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry of objects by their class, along with all their parent classes
 * and interfaces, up to and including an upper bound.  The registry is highly
 * concurrent, and performs registry lookups in O(1).
 *
 * @author  AO Industries, Inc.
 */
// TODO: Should this spin-off to a microproject?
public class PolymorphicRegistry<U> {

	private final Class<U> upperBound;

	private final ConcurrentMap<Class<? extends U>,List<U>> instancesByClass = new ConcurrentHashMap<Class<? extends U>, List<U>>();

	public PolymorphicRegistry(Class<U> upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Adds a instance to the registry.
	 * The instance is registered under {@link Classes#getAllClasses(java.lang.Class, java.lang.Class) all classes and interfaces}
	 * it extends and implements, up to and including the upper bound {@link U}.
	 * <p>
	 * This implementation favors lookup speed at O(1), and pays the price during {@link #add(java.lang.Object)}.
	 * </p>
	 */
	public void add(U instance) {
		// Add the entry under all classes, up to U, that it implements
		Class<? extends U> instanceClass = instance.getClass().asSubclass(upperBound);
		for(Class<?> clazz : Classes.getAllClasses(instanceClass, upperBound)) {
			Class<? extends U> uClass = clazz.asSubclass(upperBound);
			boolean replaced;
			do {
				List<U> oldList = instancesByClass.get(uClass);
				List<U> newList = MinimalList.unmodifiable(
					MinimalList.add(
						oldList,
						instance
					)
				);
				replaced = instancesByClass.replace(uClass, oldList, newList);
			} while(!replaced);
		}
	}

	/**
	 * Gets all instances registered of the given class.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class, or an empty list when none registered
	 */
	@SuppressWarnings("unchecked")
	public <T extends U> List<T> get(Class<T> clazz) {
		List<? extends U> instances = instancesByClass.get(clazz);
		if(instances == null) {
			return Collections.emptyList();
		} else {
			return (List<T>)instances;
		}
	}

	/**
	 * Gets all instances registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends U> List<T> get(Class<T> clazz, Predicate<? super T> filter) {
		List<T> instances = get(clazz);
		List<T> matches = MinimalList.emptyList();
		for(T instance : instances) {
			if(filter.test(instance)) {
				matches = MinimalList.add(matches, instance);
			}
		}
		return (matches.size() == instances.size())
			? instances
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets the first instance registered of the given class.
	 *
	 * @return  the first instance registered or {@code null} for none registered
	 */
	public <T extends U> T getFirst(Class<T> clazz) {
		List<T> instances = get(clazz);
		return instances.isEmpty() ? null : instances.get(0);
	}

	/**
	 * Gets the first instance registered of the given class that match the given filter.
	 *
	 * @return  the first instance registered that matches the filter or {@code null} for none registered
	 */
	public <T extends U> T getFirst(Class<T> clazz, Predicate<? super T> filter) {
		for(T instance : get(clazz)) {
			if(filter.test(instance)) return instance;
		}
		return null;
	}

	/**
	 * Gets the last instance registered of the given class.
	 *
	 * @return  the last instance registered or {@code null} for none registered
	 */
	public <T extends U> T getLast(Class<T> clazz) {
		List<T> instances = get(clazz);
		int size = instances.size();
		return size == 0 ? null : instances.get(size - 1);
	}

	/**
	 * Gets the last instance registered of the given class that match the given filter.
	 *
	 * @return  the last instance registered that matches the filter or {@code null} for none registered
	 */
	public <T extends U> T getLast(Class<T> clazz, Predicate<? super T> filter) {
		List<T> instances = get(clazz);
		for(int i = instances.size() - 1; i >= 0; i--) {
			T instance = instances.get(i);
			if(filter.test(instance)) return instance;
		}
		return null;
	}

	// TODO: Remove methods when first needed

	// TODO: When first needed: getAny with round-robin distribution, with and without filtered.  Maintain level of concurrency.
	//       This could be simple underpinnings for when multiple instance of a service are registered, and requests can be
	//       distributed between them.
}
