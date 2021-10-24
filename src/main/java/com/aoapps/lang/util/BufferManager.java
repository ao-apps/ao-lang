/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang.util;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * {@code BufferManager} manages a reusable pool of {@code byte[]} and {@code char[]} buffers.
 * This avoids the repetitive allocation of memory for an operation that only needs a temporary buffer.
 * The buffers are stored as {@code ThreadLocal} to maximize cache locality.
 * </p>
 * <p>
 * Do not use if intra-thread security is more important than performance.
 * </p>
 * <p>
 * The buffers are not necessarily cleared between invocations so the results of previous operations may be available
 * to additional callers.  On the scale of security versus performance, this is biased toward performance.
 * However, being thread local there remains some control over the visibility of the data.
 * </p>
 * <p>
 * Buffers should not be passed between threads.
 * Giving a thread a buffer you didn't get from it could result in a memory or information leak.
 * Soft references are used to avoid full-on memory leaks, but keeping buffers to a single thread
 * is optimal.
 * </p>
 * <p>
 * Under no circumstances should a buffer be released more than once.  This may result
 * in the buffer being allocated twice at the same time, with resulting data corruption.
 * </p>
 * <p>
 * The Java virtual machine has improved greatly over the years.  However, we still believe
 * this buffer management to be valuable to reduce garbage collection pressure.  If this ever
 * proves to not be the case, the implementation here can be simply changed to create new
 * arrays on each use.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public final class BufferManager {

	/**
	 * The size of buffers that are returned.
	 */
	public static final int BUFFER_SIZE = 4096;

	private static final ThreadLocal<Deque<SoftReference<byte[]>>> bytes = ThreadLocal.withInitial(ArrayDeque::new);

	private static final ThreadLocal<Deque<SoftReference<char[]>>> chars = ThreadLocal.withInitial(ArrayDeque::new);

	/**
	 * Make no instances.
	 */
	private BufferManager() {
	}

	/**
	 * Various statistics
	 */
	private static final AtomicLong
		bytesCreates = new AtomicLong(),
		bytesUses = new AtomicLong(),
		bytesZeroFills = new AtomicLong(),
		bytesCollected = new AtomicLong(),
		charsCreates = new AtomicLong(),
		charsUses = new AtomicLong(),
		charsZeroFills = new AtomicLong(),
		charsCollected = new AtomicLong()
	;

	/**
	 * Gets a {@code byte[]} of length {@code BUFFER_SIZE} that may
	 * be temporarily used for any purpose.  Once done with the buffer,
	 * {@code release} should be called, this is best accomplished
	 * in a {@code finally} block.
	 * The buffer is not necessarily zero-filled and may contain data from a previous use.
	 */
	public static byte[] getBytes() {
		bytesUses.getAndIncrement();
		Deque<SoftReference<byte[]>> myBytes = bytes.get();
		while(true) {
			SoftReference<byte[]> bufferRef = myBytes.poll();
			if(bufferRef != null) {
				byte[] buffer = bufferRef.get();
				if(buffer != null) return buffer;
				bytesCollected.getAndIncrement();
			} else {
				bytesCreates.getAndIncrement();
				return new byte[BUFFER_SIZE];
			}
		}
	}

	/**
	 * Gets a {@code char[]} of length {@code BUFFER_SIZE} that may
	 * be temporarily used for any purpose.  Once done with the buffer,
	 * {@code release} should be called, this is best accomplished
	 * in a {@code finally} block.
	 * The buffer is not necessarily zero-filled and may contain data from a previous use.
	 */
	public static char[] getChars() {
		charsUses.getAndIncrement();
		Deque<SoftReference<char[]>> myChars = chars.get();
		while(true) {
			SoftReference<char[]> bufferRef = myChars.poll();
			if(bufferRef != null) {
				char[] buffer = bufferRef.get();
				if(buffer != null) return buffer;
				charsCollected.getAndIncrement();
			} else {
				charsCreates.getAndIncrement();
				return new char[BUFFER_SIZE];
			}
		}
	}

	/**
	 * @deprecated  May obtain greater performance by avoiding zero fill on non-sensitive data.
	 */
	@Deprecated
	public static void release(byte[] buffer) {
		release(buffer, true);
	}

	/**
	 * Releases a {@code byte[]} that was obtained by a call to
	 * {@code getBytes}.  A buffer must not be released more than once.
	 *
	 * @param  buffer  the {@code byte[]} to release
	 * @param  zeroFill  if the data in the buffer may be sensitive, it is best to zero-fill the buffer on release.
	 */
	public static void release(byte[] buffer, boolean zeroFill) {
		Deque<SoftReference<byte[]>> myBytes = bytes.get();
		if(buffer.length != BUFFER_SIZE) throw new IllegalArgumentException();
		assert !inQueue(myBytes, buffer); // Error if already in the buffer list
		if(zeroFill) {
			bytesZeroFills.getAndIncrement();
			Arrays.fill(buffer, 0, BUFFER_SIZE, (byte)0);
		}
		myBytes.add(new SoftReference<>(buffer));
	}
	private static boolean inQueue(Iterable<SoftReference<byte[]>> myBytes, byte[] buffer) {
		for(SoftReference<byte[]> inQueue : myBytes) {
			if(inQueue.get() == buffer) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @deprecated  May obtain greater performance by avoiding zero fill on non-sensitive data.
	 */
	@Deprecated
	public static void release(char[] buffer) {
		release(buffer, true);
	}

	/**
	 * Releases a {@code char[]} that was obtained by a call to
	 * {@code getChars}.  A buffer must not be released more than once.
	 *
	 * @param  buffer  the {@code char[]} to release
	 * @param  zeroFill  if the data in the buffer may be sensitive, it is best to zero-fill the buffer on release.
	 */
	public static void release(char[] buffer, boolean zeroFill) {
		Deque<SoftReference<char[]>> myChars = chars.get();
		if(buffer.length != BUFFER_SIZE) throw new IllegalArgumentException();
		assert !inQueue(myChars, buffer); // Error if already in the buffer list
		if(zeroFill) {
			charsZeroFills.getAndIncrement();
			Arrays.fill(buffer, 0, BUFFER_SIZE, (char)0);
		}
		myChars.add(new SoftReference<>(buffer));
	}
	private static boolean inQueue(Iterable<SoftReference<char[]>> myChars, char[] buffer) {
		for(SoftReference<char[]> inQueue : myChars) {
			if(inQueue.get() == buffer) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the number of {@code byte[]} buffers instantiated.
	 */
	public static long getByteBufferCreates() {
		return bytesCreates.get();
	}

	/**
	 * Gets the number of time {@code byte[]} buffers have been used.
	 */
	public static long getByteBufferUses() {
		return bytesUses.get();
	}

	/**
	 * Gets the number of time {@code byte[]} buffers have been zero-filled on release.
	 */
	public static long getByteBufferZeroFills() {
		return bytesZeroFills.get();
	}

	/**
	 * Gets the number of {@code byte[]} buffers detected to have been garbage collected.
	 */
	public static long getByteBuffersCollected() {
		return bytesCollected.get();
	}

	/**
	 * Gets the number of {@code char[]} buffers instantiated.
	 */
	public static long getCharBufferCreates() {
		return charsCreates.get();
	}

	/**
	 * Gets the number of time {@code char[]} buffers have been used.
	 */
	public static long getCharBufferUses() {
		return charsUses.get();
	}

	/**
	 * Gets the number of time {@code char[]} buffers have been zero-filled on release.
	 */
	public static long getCharBufferZeroFills() {
		return charsZeroFills.get();
	}

	/**
	 * Gets the number of {@code char[]} buffers detected to have been garbage collected.
	 */
	public static long getCharBuffersCollected() {
		return charsCollected.get();
	}
}
