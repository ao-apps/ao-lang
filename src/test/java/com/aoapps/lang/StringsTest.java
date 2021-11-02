/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2015, 2016, 2020, 2021  AO Industries, Inc.
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
 * along with ao-lang.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aoapps.lang;

import com.aoapps.lang.io.IoUtils;
import java.security.SecureRandom;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  AO Industries, Inc.
 */
public class StringsTest extends TestCase {

	public StringsTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(StringsTest.class);
	}

	/**
	 * A fast pseudo-random number generator for non-cryptographic purposes.
	 */
	private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(Long.BYTES)));

	public void testConvertToFromHexInt() {
		for(int i=0; i<1000; i++) {
			int before = fastRandom.nextInt();
			@SuppressWarnings("deprecation")
			int after = Strings.convertIntArrayFromHex(Strings.convertToHex(before).toCharArray());
			assertEquals(before, after);
		}
	}

	public void testConvertToFromHexLong() {
		for(int i=0; i<1000; i++) {
			long before = fastRandom.nextLong();
			@SuppressWarnings("deprecation")
			long after = Strings.convertLongArrayFromHex(Strings.convertToHex(before).toCharArray());
			assertEquals(before, after);
		}
	}
}
