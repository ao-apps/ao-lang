/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2012, 2016, 2017  AO Industries, Inc.
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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates incrementing identifiers in a thread-safe manner using atomic
 * primitives.
 *
 * @author  AO Industries, Inc.
 */
public class AtomicSequence implements Sequence {

	final private AtomicLong counter;

	/**
	 * Starts at the value of 1.
	 */
	public AtomicSequence() {
		this(1);
	}

	public AtomicSequence(long initialValue) {
		counter = new AtomicLong(initialValue);
	}

	@Override
	public long getNextSequenceValue() {
		return counter.getAndIncrement();
	}

	@Override
	public void setNextSequenceValue(long nextValue) {
		counter.set(nextValue);
	}
}
