/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2014, 2016, 2017  AO Industries, Inc.
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

import com.aoindustries.lang.ObjectUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MinimalMap provides a set of static methods to dynamically choose the most
 * efficient Map implementation.  The implementation of Map is changed as needed.
 * MinimalMap is most suited for building map-based data structures that use less
 * heap space than a pure HashMap-based solution.
 * <p>
 * Insertion order is maintained.
 * </p>
 * <p>
 * size=0: null<br/>
 * size=1: Collections.singletonMap<br/>
 * size=2: LinkedHashMap
 * </p>
 *
 * TODO: The size zero state should be represented by Collections.EMPTY_MAP
 *
 * @author  AO Industries, Inc.
 */
public class MinimalMap {

	private MinimalMap() {
	}

	/**
	 * Puts a new element in a map, returning the (possibly new) map.
	 */
	public static <K,V> Map<K,V> put(Map<K,V> map, K key, V value) {
		if(map == null) {
			// The first entry is always a singletonMap
			map = Collections.singletonMap(key, value);
		} else if(map.size()==1) {
			// Is a singleton map
			Map.Entry<K,V> entry = map.entrySet().iterator().next();
			K entryKey = entry.getKey();
			if(ObjectUtils.equals(key, entryKey)) {
				// If have the same key, replace entry
				map = Collections.singletonMap(entryKey, value);
			} else {
				// Is a second property
				map = new LinkedHashMap<K,V>(8);
				map.put(entryKey, entry.getValue());
				map.put(key, value);
			}
		} else {
			// Is a LinkedHashMap
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Removes an element from a map, returning the (possibly new) map.
	 */
	public static <K,V> Map<K,V> remove(final Map<K,V> map, final K key) {
		if(map == null) {
			// Empty map, nothing to remove
			return null;
		} else if(map.size()==1) {
			// Is a singleton map
			if(map.containsKey(key)) {
				// Map is now empty
				return null;
			} else {
				// Map unchanged
				return map;
			}
		} else {
			// Is a LinkedHashMap
			map.remove(key);
			if(map.size() == 1) {
				// Convert to singletonMap
				Map.Entry<K,V> entry = map.entrySet().iterator().next();
				return Collections.singletonMap(entry.getKey(), entry.getValue());
			} else {
				// Still more than one item, use same LinkedHashMap instance
				assert map.size() > 1;
				return map;
			}
		}
	}

	/**
	 * Gets an element from a map.
	 */
	public static <K,V> V get(Map<K,V> map, K key) {
		return map==null ? null : map.get(key);
	}

	/**
	 * Checks if a key is contained in the map.
	 */
	public static <K,V> boolean containsKey(Map<K,V> map, K key) {
		return map!=null && map.containsKey(key);
	}

	/**
	 * Gets the value collection.
	 */
	public static <K,V> Collection<V> values(Map<K,V> map) {
		if(map==null) {
			return Collections.emptyList();
		} else {
			return map.values();
		}
	}

	/**
	 * Performs a shallow copy of the value collection.
	 */
	public static <K,V> Collection<V> valuesCopy(Map<K,V> map) {
		if(map==null) {
			return Collections.emptyList();
		} if(map.size() == 1) {
			// singletonMap is unmodifiable: no wrapping required
			return map.values();
		} else {
			// Wrap in an ArrayList
			return new ArrayList<V>(map.values());
		}
	}

	/**
	 * Performs a shallow copy of a map.  The map is assumed to have been
	 * created by MinimalMap and to be used through MinimalMap.
	 */
	public static <K,V> Map<K,V> copy(Map<K,V> map) {
		if(map==null) {
			// Empty
			return null;
		}
		if(map.size()==1) {
			// Is a singletonMap (unmodifiable) - safe to share instance.
			return map;
		}
		// Create copy of map
		return new LinkedHashMap<K,V>(map);
	}

	/**
	 * Gets an unmodifiable wrapper around this list.
	 * May or may not wrap this list itself.
	 */
	public static <K,V> Map<K,V> unmodifiable(Map<K,V> map) {
		if(map==null) {
			// Empty
			return Collections.emptyMap();
		}
		return AoCollections.optimalUnmodifiableMap(map);
	}
}
