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
import java.util.Map;
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
// TODO: Should this fully implement the Map interface?
public class PolymorphicMultimap<K,V> {

	private final Class<K> upperBound;

	private final ConcurrentMap<Class<? extends K>, List<Map.Entry<K,V>>> entriesByClass = new ConcurrentHashMap<Class<? extends K>, List<Map.Entry<K,V>>>();

	public PolymorphicMultimap(Class<K> upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Puts an key to the registry along with an associated value.
	 * The key is registered under {@link Classes#getAllClasses(java.lang.Class, java.lang.Class) all classes and interfaces}
	 * it extends and implements, up to and including the upper bound {@link K}.
	 * <p>
	 * This implementation favors lookup speed at O(1), and pays the price during {@link #put(java.lang.Object, java.lang.Object)}.
	 * </p>
	 */
	public void put(final K key, final V value) {
		// Add the entry under all classes, up to K, that it implements
		Class<? extends K> keyClass = key.getClass().asSubclass(upperBound);
		for(Class<?> clazz : Classes.getAllClasses(keyClass, upperBound)) {
			Class<? extends K> uClass = clazz.asSubclass(upperBound);
			Map.Entry<K,V> newEntry = new Map.Entry<K,V>() {
				@Override
				public K getKey() {
					return key;
				}

				@Override
				public V getValue() {
					return value;
				}

				@Override
				public V setValue(V value) {
					throw new UnsupportedOperationException();
				}
			};
			boolean replaced;
			do {
				List<Map.Entry<K,V>> oldList = entriesByClass.get(uClass);
				List<Map.Entry<K,V>> newList = MinimalList.unmodifiable(
					MinimalList.add(
						oldList,
						newEntry
					)
				);
				replaced = entriesByClass.replace(uClass, oldList, newList);
			} while(!replaced);
		}
	}

	/**
	 * Gets all entries registered of the given class.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class, or an empty list when none registered
	 */
	@SuppressWarnings("unchecked")
	public <T extends K> List<Map.Entry<T,V>> get(Class<T> clazz) {
		List<Map.Entry<K,V>> entries = entriesByClass.get(clazz);
		if(entries == null) {
			return Collections.emptyList();
		} else {
			return (List)entries;
		}
	}

	/**
	 * Gets all entries registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<Map.Entry<T,V>> get(Class<T> clazz, Predicate<? super Map.Entry<? super T, ? super V>> filter) {
		List<Map.Entry<T,V>> entries = get(clazz);
		List<Map.Entry<T,V>> matches = MinimalList.emptyList();
		for(Map.Entry<T,V> entry : entries) {
			if(filter.test(entry)) {
				matches = MinimalList.add(matches, entry);
			}
		}
		return (matches.size() == entries.size())
			? entries
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets the first entry registered of the given class.
	 *
	 * @return  the first entry registered or {@code null} for none registered
	 */
	public <T extends K> Map.Entry<T,V> getFirst(Class<T> clazz) {
		List<Map.Entry<T,V>> entries = get(clazz);
		return entries.isEmpty() ? null : entries.get(0);
	}

	/**
	 * Gets the first entry registered of the given class that match the given filter.
	 *
	 * @return  the first entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Map.Entry<T,V> getFirst(Class<T> clazz, Predicate<? super Map.Entry<? super T, ? super V>> filter) {
		for(Map.Entry<T,V> entry : get(clazz)) {
			if(filter.test(entry)) return entry;
		}
		return null;
	}

	/**
	 * Gets the last entry registered of the given class.
	 *
	 * @return  the last entry registered or {@code null} for none registered
	 */
	public <T extends K> Map.Entry<T,V> getLast(Class<T> clazz) {
		List<Map.Entry<T,V>> entries = get(clazz);
		int size = entries.size();
		return size == 0 ? null : entries.get(size - 1);
	}

	/**
	 * Gets the last entry registered of the given class that match the given filter.
	 *
	 * @return  the last entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Map.Entry<T,V> getLast(Class<T> clazz, Predicate<? super Map.Entry<? super T, ? super V>> filter) {
		List<Map.Entry<T,V>> entries = get(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Map.Entry<T,V> entry = entries.get(i);
			if(filter.test(entry)) return entry;
		}
		return null;
	}

	// TODO: Remove methods when first needed

	// TODO: When first needed: getAny with round-robin distribution, with and without filtered.  Maintain level of concurrency.
	//       This could be simple underpinnings for when multiple entry of a service are registered, and requests can be
	//       distributed between them.
}
