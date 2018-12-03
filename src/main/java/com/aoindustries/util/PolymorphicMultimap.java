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
import java.util.Map.Entry;
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

	protected static class Lists<K,V> {
		private final List<K> keys;
		private final List<V> values;
		private final List<Entry<K,V>> entries;
		// TODO: AtomicLong for round-robin getAny
		private Lists(
			List<K> keys,
			List<V> values,
			List<Entry<K,V>> entries
		) {
			assert keys.size() == values.size();
			assert values.size() == entries.size();
			this.keys = keys;
			this.values = values;
			this.entries = entries;
		}
	}
	private final ConcurrentMap<Class<? extends K>,Lists<K,V>> listsByClass = new ConcurrentHashMap<Class<? extends K>,Lists<K,V>>();

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
			Entry<K,V> newEntry = new Entry<K,V>() {
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
				Lists<K,V> oldLists = listsByClass.get(uClass);
				Lists<K,V> newLists = new Lists<K,V>(
					MinimalList.unmodifiable(MinimalList.add(oldLists==null ? null : oldLists.keys, key)),
					MinimalList.unmodifiable(MinimalList.add(oldLists==null ? null : oldLists.values, value)),
					MinimalList.unmodifiable(MinimalList.add(oldLists==null ? null : oldLists.entries, newEntry))
				);
				replaced = listsByClass.replace(uClass, oldLists, newLists);
			} while(!replaced);
		}
	}

	/**
	 * Gets the lists registered for the given class.
	 *
	 * @return  the lists or {@code null} when none registered
	 */
	@SuppressWarnings("unchecked")
	protected <T extends K> Lists<T,V> getLists(Class<T> clazz) {
		Lists<K,V> lists = listsByClass.get(clazz);
		return (Lists)lists;
	}

	/**
	 * Gets all keys registered of the given class.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class, or an empty list when none registered
	 */
	public <T extends K> List<T> getKeys(Class<T> clazz) {
		Lists<T,?> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			return lists.keys;
		}
	}

	/**
	 * Gets all keys registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<T> getKeys(Class<T> clazz, Predicate<? super T> filter) {
		List<T> keys = getKeys(clazz);
		List<T> matches = MinimalList.emptyList();
		for(T key : keys) {
			if(filter.test(key)) {
				matches = MinimalList.add(matches, key);
			}
		}
		return (matches.size() == keys.size())
			? keys
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets all keys registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<T> getKeysFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		Lists<T,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			List<Entry<T,V>> entries = getEntries(clazz);
			List<T> matches = MinimalList.emptyList();
			for(Entry<T,V> entry : entries) {
				if(filter.test(entry.getValue())) {
					matches = MinimalList.add(matches, entry.getKey());
				}
			}
			return (matches.size() == entries.size())
				? lists.keys
				: MinimalList.unmodifiable(matches);
		}
	}

	/**
	 * Gets all keys registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<T> getKeysFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		Lists<T,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			List<Entry<T,V>> entries = getEntries(clazz);
			List<T> matches = MinimalList.emptyList();
			for(Entry<T,V> entry : entries) {
				if(filter.test(entry)) {
					matches = MinimalList.add(matches, entry.getKey());
				}
			}
			return (matches.size() == entries.size())
				? lists.keys
				: MinimalList.unmodifiable(matches);
		}
	}

	/**
	 * Gets all values registered of the given class.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class, or an empty list when none registered
	 */
	public List<V> getValues(Class<? extends K> clazz) {
		Lists<?,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			return lists.values;
		}
	}

	/**
	 * Gets all values registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public List<V> getValues(Class<? extends K> clazz, Predicate<? super V> filter) {
		List<V> values = getValues(clazz);
		List<V> matches = MinimalList.emptyList();
		for(V value : values) {
			if(filter.test(value)) {
				matches = MinimalList.add(matches, value);
			}
		}
		return (matches.size() == values.size())
			? values
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets all values registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<V> getValuesFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		Lists<T,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			List<Entry<T,V>> entries = getEntries(clazz);
			List<V> matches = MinimalList.emptyList();
			for(Entry<T,V> entry : entries) {
				if(filter.test(entry.getKey())) {
					matches = MinimalList.add(matches, entry.getValue());
				}
			}
			return (matches.size() == entries.size())
				? lists.values
				: MinimalList.unmodifiable(matches);
		}
	}

	/**
	 * Gets all values registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<V> getValuesFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		Lists<T,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			List<Entry<T,V>> entries = getEntries(clazz);
			List<V> matches = MinimalList.emptyList();
			for(Entry<T,V> entry : entries) {
				if(filter.test(entry)) {
					matches = MinimalList.add(matches, entry.getValue());
				}
			}
			return (matches.size() == entries.size())
				? lists.values
				: MinimalList.unmodifiable(matches);
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
	public <T extends K> List<Entry<T,V>> getEntries(Class<T> clazz) {
		Lists<T,V> lists = getLists(clazz);
		if(lists == null) {
			return Collections.emptyList();
		} else {
			return lists.entries;
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
	public <T extends K> List<Entry<T,V>> getEntries(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		List<Entry<T,V>> matches = MinimalList.emptyList();
		for(Entry<T,V> entry : entries) {
			if(filter.test(entry)) {
				matches = MinimalList.add(matches, entry);
			}
		}
		return (matches.size() == entries.size())
			? entries
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets all entries registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<Entry<T,V>> getEntriesFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		List<Entry<T,V>> matches = MinimalList.emptyList();
		for(Entry<T,V> entry : entries) {
			if(filter.test(entry.getKey())) {
				matches = MinimalList.add(matches, entry);
			}
		}
		return (matches.size() == entries.size())
			? entries
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets all entries registered of the given class that match the given filter.
	 * They are returned in the order registered.
	 * When an object is registered more than once, it will appear in the list multiple times.
	 * The list is a snapshot and will not change over time.
	 *
	 * @return  the unmodifiable list of all objects registered of the given class that match the filter, or an empty list when none registered
	 */
	public <T extends K> List<Entry<T,V>> getEntriesFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		List<Entry<T,V>> matches = MinimalList.emptyList();
		for(Entry<T,V> entry : entries) {
			if(filter.test(entry.getValue())) {
				matches = MinimalList.add(matches, entry);
			}
		}
		return (matches.size() == entries.size())
			? entries
			: MinimalList.unmodifiable(matches);
	}

	/**
	 * Gets the first key registered of the given class.
	 *
	 * @return  the first key registered or {@code null} for none registered
	 */
	public <T extends K> T getFirstKey(Class<T> clazz) {
		List<T> keys = getKeys(clazz);
		return keys.isEmpty() ? null : keys.get(0);
	}

	/**
	 * Gets the first key registered of the given class that match the given filter.
	 *
	 * @return  the first key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getFirstKey(Class<T> clazz, Predicate<? super T> filter) {
		for(T key : getKeys(clazz)) {
			if(filter.test(key)) return key;
		}
		return null;
	}

	/**
	 * Gets the first key registered of the given class that match the given filter.
	 *
	 * @return  the first key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getFirstKeyFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry.getValue())) return entry.getKey();
		}
		return null;
	}

	/**
	 * Gets the first key registered of the given class that match the given filter.
	 *
	 * @return  the first key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getFirstKeyFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Gets the first value registered of the given class.
	 *
	 * @return  the first value registered or {@code null} for none registered
	 */
	public V getFirstValue(Class<? extends K> clazz) {
		List<V> values = getValues(clazz);
		return values.isEmpty() ? null : values.get(0);
	}

	/**
	 * Gets the first value registered of the given class that match the given filter.
	 *
	 * @return  the first value registered that matches the filter or {@code null} for none registered
	 */
	public V getFirstValue(Class<? extends K> clazz, Predicate<? super V> filter) {
		for(V value : getValues(clazz)) {
			if(filter.test(value)) return value;
		}
		return null;
	}

	/**
	 * Gets the first value registered of the given class that match the given filter.
	 *
	 * @return  the first value registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> V getFirstValueFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry.getKey())) return entry.getValue();
		}
		return null;
	}

	/**
	 * Gets the first value registered of the given class that match the given filter.
	 *
	 * @return  the first value registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> V getFirstValueFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry)) return entry.getValue();
		}
		return null;
	}

	/**
	 * Gets the first entry registered of the given class.
	 *
	 * @return  the first entry registered or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getFirstEntry(Class<T> clazz) {
		List<Entry<T,V>> entries = getEntries(clazz);
		return entries.isEmpty() ? null : entries.get(0);
	}

	/**
	 * Gets the first entry registered of the given class that match the given filter.
	 *
	 * @return  the first entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getFirstEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry)) return entry;
		}
		return null;
	}

	/**
	 * Gets the first entry registered of the given class that match the given filter.
	 *
	 * @return  the first entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getFirstEntryFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry.getKey())) return entry;
		}
		return null;
	}

	/**
	 * Gets the first entry registered of the given class that match the given filter.
	 *
	 * @return  the first entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getFirstEntryFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		for(Entry<T,V> entry : getEntries(clazz)) {
			if(filter.test(entry.getValue())) return entry;
		}
		return null;
	}

	/**
	 * Gets the last key registered of the given class.
	 *
	 * @return  the last key registered or {@code null} for none registered
	 */
	public <T extends K> T getLastKey(Class<T> clazz) {
		List<T> keys = getKeys(clazz);
		int size = keys.size();
		return size == 0 ? null : keys.get(size - 1);
	}

	/**
	 * Gets the last key registered of the given class that match the given filter.
	 *
	 * @return  the last key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getLastKey(Class<T> clazz, Predicate<? super T> filter) {
		List<T> keys = getKeys(clazz);
		for(int i = keys.size() - 1; i >= 0; i--) {
			T key = keys.get(i);
			if(filter.test(key)) return key;
		}
		return null;
	}

	/**
	 * Gets the last key registered of the given class that match the given filter.
	 *
	 * @return  the last key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getLastKeyFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry.getValue())) return entry.getKey();
		}
		return null;
	}

	/**
	 * Gets the last key registered of the given class that match the given filter.
	 *
	 * @return  the last key registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> T getLastKeyFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Gets the last value registered of the given class.
	 *
	 * @return  the last value registered or {@code null} for none registered
	 */
	public V getLastValue(Class<? extends K> clazz) {
		List<V> values = getValues(clazz);
		int size = values.size();
		return size == 0 ? null : values.get(size - 1);
	}

	/**
	 * Gets the last value registered of the given class that match the given filter.
	 *
	 * @return  the last value registered that matches the filter or {@code null} for none registered
	 */
	public V getLastValue(Class<? extends K> clazz, Predicate<? super V> filter) {
		List<V> values = getValues(clazz);
		for(int i = values.size() - 1; i >= 0; i--) {
			V value = values.get(i);
			if(filter.test(value)) return value;
		}
		return null;
	}

	/**
	 * Gets the last value registered of the given class that match the given filter.
	 *
	 * @return  the last value registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> V getLastValueFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry.getKey())) return entry.getValue();
		}
		return null;
	}

	/**
	 * Gets the last value registered of the given class that match the given filter.
	 *
	 * @return  the last value registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> V getLastValueFilterEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry)) return entry.getValue();
		}
		return null;
	}

	/**
	 * Gets the last entry registered of the given class.
	 *
	 * @return  the last entry registered or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getLastEntry(Class<T> clazz) {
		List<Entry<T,V>> entries = getEntries(clazz);
		int size = entries.size();
		return size == 0 ? null : entries.get(size - 1);
	}

	/**
	 * Gets the last entry registered of the given class that match the given filter.
	 *
	 * @return  the last entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getLastEntry(Class<T> clazz, Predicate<? super Entry<? super T, ? super V>> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry)) return entry;
		}
		return null;
	}

	/**
	 * Gets the last entry registered of the given class that match the given filter.
	 *
	 * @return  the last entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getLastEntryFilterKey(Class<T> clazz, Predicate<? super T> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry.getKey())) return entry;
		}
		return null;
	}

	/**
	 * Gets the last entry registered of the given class that match the given filter.
	 *
	 * @return  the last entry registered that matches the filter or {@code null} for none registered
	 */
	public <T extends K> Entry<T,V> getLastEntryFilterValue(Class<T> clazz, Predicate<? super V> filter) {
		List<Entry<T,V>> entries = getEntries(clazz);
		for(int i = entries.size() - 1; i >= 0; i--) {
			Entry<T,V> entry = entries.get(i);
			if(filter.test(entry.getValue())) return entry;
		}
		return null;
	}

	// TODO: Remove methods when first needed

	// TODO: When first needed: getAny with round-robin distribution, with and without filtered.  Maintain level of concurrency.
	//       This could be simple underpinnings for when multiple entry of a service are registered, and requests can be
	//       distributed between them.
}
