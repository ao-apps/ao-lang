/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2013, 2014, 2016, 2017  AO Industries, Inc.
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
import com.aoindustries.util.AoCollections.PeekIterator;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
// import org.checkthread.annotations.ThreadSafe;

/**
 * General-purpose array utilities and constants.
 *
 * @author  AO Industries, Inc.
 */
public class AoArrays {

	private AoArrays() {
	}

	public static final byte[] EMPTY_BYTE_ARRAY = {};
	public static final char[] EMPTY_CHAR_ARRAY = {};
	public static final int[] EMPTY_INT_ARRAY = {};
	public static final long[] EMPTY_LONG_ARRAY = {};
	public static final Class<?>[] EMPTY_CLASS_ARRAY = {};
	public static final Object[] EMPTY_OBJECT_ARRAY = {};
	public static final Serializable[] EMPTY_SERIALIZABLE_ARRAY = {};
	public static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * Checks if the subrange of two byte arrays is equal.
	 */
	// @ThreadSafe
	public static boolean equals(byte[] b1, byte[] b2, int off, int len) {
		for(int end=off+len; off<end; off++) {
			if(b1[off]!=b2[off]) return false;
		}
		return true;
	}

	/**
	 * Checks if the subrange of two byte arrays is equal.
	 */
	// @ThreadSafe
	public static boolean equals(byte[] b1, int off1, byte[] b2, int off2, int len) {
		for(int end=off1+len; off1<end; off1++, off2++) {
			if(b1[off1]!=b2[off2]) return false;
		}
		return true;
	}

	/**
	 * Checks if all the values in the provided range are equal to <code>value</code>.
	 */
	// @ThreadSafe
	public static boolean allEquals(byte[] b, int off, int len, byte value) {
		for(int end=off+len; off<end; off++) {
			if(b[off]!=value) return false;
		}
		return true;
	}

	/**
	 * Compares two byte[].  Shorter byte[] are ordered before longer when
	 * the shorter is a prefix of the longer.  The comparison considers each
	 * byte as a value from 0-255.
	 */
	// @ThreadSafe
	public static int compare(byte[] ba1, byte[] ba2) {
		int len = Math.min(ba1.length, ba2.length);
		for(int i=0; i<len; i++) {
			int b1 = ba1[i]&255;
			int b2 = ba2[i]&255;
			if(b1<b2) return -1;
			if(b1>b2) return 1;
		}
		if(ba1.length>len) return 1;
		if(ba2.length>len) return -1;
		return 0;
	}

	/**
	 * Merges multiple already-sorted collections into one big array.
	 *
	 * Worst-case Complexity:
	 * 
	 *     0 collections: constant
	 *
	 *     1 collection: O(n), where n is the number of elements in the collection
	 *
	 *     2 collection: O(n+m), where n is the number of elements in one collection, and m is the number of elements in the other collection
	 *
	 *     3+ collections: O(n*log(m)), where n is the total number of elements in all collections, and m is the number of collections
	 *
	 * @return Object[] of results.
	 */
	@SuppressWarnings("unchecked")
	public static <V> V[] merge(Class<V> clazz, Collection<? extends Collection<? extends V>> collections, final Comparator<? super V> comparator) {
		final int numCollections = collections.size();
		// Zero is easy
		if(numCollections==0) return (V[])Array.newInstance(clazz, 0);
		// One collection - just use toArray
		else if(numCollections == 1) {
			Collection<? extends V> collection = collections.iterator().next();
			return collection.toArray((V[])Array.newInstance(clazz, collection.size()));
		}
		// Two collections - use very simple merge
		else if(numCollections == 2) {
			Iterator<? extends Collection<? extends V>> collIter = collections.iterator();
			final Collection<? extends V> c1 = collIter.next();
			final Collection<? extends V> c2 = collIter.next();
			assert !collIter.hasNext();
			final Iterator<? extends V> i1 = c1.iterator();
			final Iterator<? extends V> i2 = c2.iterator();
			final int totalSize = c1.size() + c2.size();

			@SuppressWarnings("unchecked")
			final V[] results = (V[])Array.newInstance(clazz, totalSize);
			V next1 = i1.hasNext() ? i1.next() : null;
			V next2 = i2.hasNext() ? i2.next() : null;
			int pos = 0;
			while(true) {
				if(next1==null) {
					if(next2==null) {
						// Both done
						break;
					} else {
						// Get rest of i2
						results[pos++] = next2;
						while(i2.hasNext()) results[pos++] = i2.next();
						break;
					}
				} else {
					if(next2==null) {
						// Get rest of i1
						results[pos++] = next1;
						while(i1.hasNext()) results[pos++] = i1.next();
						break;
					} else {
						if(comparator.compare(next1, next2)<=0) {
							results[pos++] = next1;
							next1 = i1.hasNext() ? i1.next() : null;
						} else {
							results[pos++] = next2;
							next2 = i2.hasNext() ? i2.next() : null;
						}
					}
				}
			}
			if(pos!=totalSize) throw new ConcurrentModificationException();
			return results;
		} else {
			// 3+ collections, use priority queue
			PriorityQueue<AoCollections.PeekIterator<? extends V>> pq = new PriorityQueue<AoCollections.PeekIterator<? extends V>>(
				numCollections,
				new Comparator<AoCollections.PeekIterator<? extends V>>() {
					@Override
					public int compare(PeekIterator<? extends V> i1, PeekIterator<? extends V> i2) {
						return comparator.compare(i1.peek(), i2.peek());
					}
				}
			);
			int totalSize = 0;
			for(Collection<? extends V> collection : collections) {
				pq.add(AoCollections.peekIterator(collection.iterator()));
				totalSize += collection.size();
			}
			@SuppressWarnings("unchecked")
			final V[] results = (V[])Array.newInstance(clazz, totalSize);
			int pos = 0;
			PeekIterator<? extends V> pi;
			while((pi=pq.poll())!=null) {
				results[pos++] = pi.next();
				if(pi.hasNext()) pq.offer(pi);
			}
			if(pos!=totalSize) throw new ConcurrentModificationException();
			return results;
		}
	}

	static final class UnmodifiableArraySet<E> implements Set<E>, Serializable {

		private static final long serialVersionUID = 1L;

		private final E[] array;

		UnmodifiableArraySet(E[] array) {
			this.array = array;
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public boolean isEmpty() {
			return array.length==0;
		}

		@Override
		public boolean contains(Object o) {
			final E[] a = this.array;
			final int len = a.length;
			if(o==null) {
				for(int i=0; i<len; i++) {
					if(a[i]==null) return true;
				}
			} else {
				/* 278 ms * /
				int i=len;
				while(i>0) {
					if(o.equals(array[--i])) return true;
				}
				/* */
				/* 205 ms */
				int i=len-1;
				while(i>=0) {
					if(o.equals(a[i--])) return true;
				}
				/* */
				/* 275 ms * /
				for(int i=0; i<len; i++) {
					if(o.equals(array[i])) return true;
				}
				/* */
			}
			return false;
		}

		@Override
		public Iterator<E> iterator() {
			return new Iterator<E>() {
				int pos = 0;
				@Override
				public boolean hasNext() {
					return pos<array.length;
				}

				@Override
				public E next() throws NoSuchElementException {
					if(pos < array.length) return array[pos++];
					throw new NoSuchElementException();
				}

				@Override
				public void remove() throws UnsupportedOperationException {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public Object[] toArray() {
			int len = array.length;
			Object[] resultArray = new Object[len];
			System.arraycopy(array, 0, resultArray, 0, len);
			return resultArray;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] resultArray) {
			int len = array.length;
			resultArray =
				resultArray.length >= len
				? resultArray
				: (T[])java.lang.reflect.Array.newInstance(resultArray.getClass().getComponentType(), len)
			;
			System.arraycopy(array, 0, resultArray, 0, len);
			// Null terminate
			if(resultArray.length > len) resultArray[len] = null;
			return resultArray;
		}

		@Override
		public boolean add(E e) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for(Object e : c) {
				if(!contains(e)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Gets an unmodifiable set view with the contents of a backing array.
	 * <p>
	 * Contains is implemented sequentially and is thus O(n).  As a result, this
	 * is best used for very small sets.
	 * </p>
	 * <p>
	 * In order to have correct set semantics, the array must have unique values
	 * as determined by the element equals methods.  This is not checked, however,
	 * and passing in an array with duplicate values will result in duplicate
	 * values on iteration and a size that doesn't match the number of unique values.
	 * </p>
	 */
	// Java 1.7: @SafeVarargs
	public static <E> Set<E> asUnmodifiableSet(final E... array) {
		final int len = array.length;
		if(len==0) return Collections.emptySet();
		if(len==1) return Collections.singleton(array[0]);
		return new UnmodifiableArraySet<E>(array);
	}

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (Object[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static <E> int indexOf(E[] array, E element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static <E> int indexOf(E[] array, E element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(ObjectUtils.equals(array[i], element)) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static <E> int lastIndexOf(E[] array, E element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static <E> int lastIndexOf(E[] array, E element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(ObjectUtils.equals(array[i], element)) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (Enum[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static <E extends Enum<E>> int indexOf(E[] array, E element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static <E extends Enum<E>> int indexOf(E[] array, E element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static <E extends Enum<E>> int lastIndexOf(E[] array, E element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static <E extends Enum<E>> int lastIndexOf(E[] array, E element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (byte[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(byte[] array, byte element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(byte[] array, byte element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(byte[] array, byte element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(byte[] array, byte element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (short[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(short[] array, short element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(short[] array, short element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(short[] array, short element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(short[] array, short element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (int[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(int[] array, int element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(int[] array, int element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(int[] array, int element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(int[] array, int element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (long[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(long[] array, long element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(long[] array, long element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(long[] array, long element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(long[] array, long element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (float[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(float[] array, float element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(float[] array, float element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(float[] array, float element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(float[] array, float element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (double[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(double[] array, double element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(double[] array, double element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(double[] array, double element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(double[] array, double element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="indexOf and lastIndexOf (char[])">
	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(char[] array, char element) {
		return indexOf(array, element, 0);
	}

	/**
	 * Finds the first index of an element or <code>-1</code> if not found.
	 */
	public static int indexOf(char[] array, char element, int fromIndex) {
		for(int i=fromIndex, len=array.length; i<len; i++) {
			if(array[i] == element) return i;
		}
		return -1;
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(char[] array, char element) {
		return lastIndexOf(array, element, array.length-1);
	}

	/**
	 * Finds the last index of an element or <code>-1</code> if not found.
	 */
	public static int lastIndexOf(char[] array, char element, int fromIndex) {
		for(int i=fromIndex; i>=0; i--) {
			if(array[i] == element) return i;
		}
		return -1;
	}
	// </editor-fold>

	/**
	 * Computes hashCode compatible with Arrays.hashCode, but only across the
	 * given subset of the array.
	 *
	 * @see  Arrays#hashCode(byte[]) 
	 */
	public static int hashCode(byte a[], int off, int len) {
		if(a == null) return 0;

		int result = 1;
		// while(len-- > 0) result = 31 * result + a[off++];
		for(int end=off+len; off<end; off++) {
			result = 31 * result + a[off];
		}

		return result;
	}

	/**
	 * Gets the maximum non-null value, or {@code null} if no non-null value.
	 */
	public static <T extends Comparable<? super T>> T maxNonNull(T ... values) {
		T max = null;
		for(T value : values) {
			if(
				value != null
				&& (
					max == null
					|| value.compareTo(max) > 0
				)
			) max = value;
		}
		return max;
	}

	/**
	 * Sorts parallel arrays in-place.  Sorts by the first array and updates
	 * all other arrays to match.
	 * Uses the natural sorting of the objects.
	 * All arrays must be the same length.
	 * <p>
	 * The time complexity isn't too bad. Looks something like
	 * {@code O((M+1)*N*log(N))}, where {@code M} is the number of otherArrays
	 * and {@code N} is the number of keys. No crazy worst-case issues, at least.
	 * </p>
	 *
	 * @param  keys         the values used to sort, may be duplicate
	 *
	 * @param  otherArrays  the arrays to have reordered to match the sorting of
	 *                      the keys array.
	 *
	 * @exception  IllegalArgumentException  if any of otherArrays have a length
	 *                      different that the keys array.
	 */
	public static <E extends Comparable<? super E>> void sortParallelArrays(
		E[] keys,
		Object[] ... otherArrays
	) {
		int numKeys = keys.length;
		int numOtherArrays = otherArrays.length;
		for(Object[] otherArray : otherArrays) {
			if(otherArray.length != numKeys) {
				throw new IllegalArgumentException("Mismatched array lengths");
			}
		}
		// A list of all indexes per key
		// This also does the sorting within the TreeMap using natural ordering
		SortedMap<E, List<Integer>> originalIndexesByKey = new TreeMap<E, List<Integer>>();

		// Populate the map
		for(int i = 0; i < numKeys; i++) {
			E key = keys[i];
			List<Integer> originalIndexes = originalIndexesByKey.get(key);
			if(originalIndexes == null) {
				// Optimization for the non-duplicate keys
				originalIndexesByKey.put(key, Collections.singletonList(i));
			} else {
				if(originalIndexes.size() == 1) {
					// Upgrade to ArrayList now that know have duplicate keys
					originalIndexes = new ArrayList<Integer>(originalIndexes);
					originalIndexesByKey.put(key, originalIndexes);
				}
				originalIndexes.add(i);
			}
		}

		// Store back to keys and sort other arrays in a single traversal
		Object[][] sortedOtherArrays = new Object[numOtherArrays][numKeys];
		int pos = 0;
		for(Map.Entry<E, List<Integer>> entry : originalIndexesByKey.entrySet()) {
			E key = entry.getKey();
			for(int index : entry.getValue()) {
				keys[pos] = key;
				for(int ooIndex = 0; ooIndex < numOtherArrays; ooIndex++) {
					sortedOtherArrays[ooIndex][pos] = otherArrays[ooIndex][index];
				}
				pos++;
			}
		}
		assert pos == numKeys : "Arrays should be full";

		// Copy back to original arrays for in-place sort
		for(int ooIndex = 0; ooIndex < numOtherArrays; ooIndex++) {
			System.arraycopy(
				sortedOtherArrays[ooIndex], 0,
				otherArrays[ooIndex], 0,
				numKeys);
		}
	}
}
