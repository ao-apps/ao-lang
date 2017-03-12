/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017  AO Industries, Inc.
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
package com.aoindustries.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the SmartComparator class.
 *
 * @author  AO Industries, Inc.
 */
public class SmartComparatorTest extends TestCase {

	public SmartComparatorTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SmartComparatorTest.class);
		return suite;
	}

	private SmartComparator comparator;

	@Override
	protected void setUp() throws Exception {
		comparator = new SmartComparator(Locale.ROOT);
	}

	@Override
	protected void tearDown() throws Exception {
		comparator = null;
	}

	private static void doTestNextToken(String value, int pos, SmartComparator.TokenType expectedType, int expectedBegin, int expectedEnd) {
		SmartComparator.Token token = SmartComparator.nextToken(value, pos);
		assertEquals(value, token.value);
		assertEquals(expectedType, token.tokenType);
		assertEquals(expectedBegin, token.begin);
		assertEquals(expectedEnd, token.end);
	}

	public void testNextToken() {
		// Numbers by self
		doTestNextToken(
			"1", 0,
			SmartComparator.TokenType.NUMERIC, 0, 1
		);
		doTestNextToken(
			"1.", 0,
			SmartComparator.TokenType.NUMERIC, 0, 2
		);
		doTestNextToken(
			"1.1", 0,
			SmartComparator.TokenType.NUMERIC, 0, 3
		);
		doTestNextToken(
			"-1.1", 0,
			SmartComparator.TokenType.NUMERIC, 0, 4
		);
		// Duplicate periods
		doTestNextToken(
			"1..", 0,
			SmartComparator.TokenType.NUMERIC, 0, 2
		);
		doTestNextToken(
			"1.1.", 0,
			SmartComparator.TokenType.NUMERIC, 0, 3
		);
		doTestNextToken(
			"-1.1.", 0,
			SmartComparator.TokenType.NUMERIC, 0, 4
		);
		// String by itself
		doTestNextToken(
			"Dan", 0,
			SmartComparator.TokenType.STRING, 0, 3
		);
		// Numbers before strings
		doTestNextToken(
			"1Dan", 0,
			SmartComparator.TokenType.NUMERIC, 0, 1
		);
		doTestNextToken(
			"1Dan", 1,
			SmartComparator.TokenType.STRING, 1, 4
		);
		doTestNextToken(
			".1Dan", 0,
			SmartComparator.TokenType.NUMERIC, 0, 2
		);
		doTestNextToken(
			".1Dan", 2,
			SmartComparator.TokenType.STRING, 2, 5
		);
		doTestNextToken(
			"-1Dan", 0,
			SmartComparator.TokenType.NUMERIC, 0, 2
		);
		doTestNextToken(
			"-1Dan", 2,
			SmartComparator.TokenType.STRING, 2, 5
		);
		doTestNextToken(
			"-1.Dan", 0,
			SmartComparator.TokenType.NUMERIC, 0, 3
		);
		doTestNextToken(
			"-1.Dan", 3,
			SmartComparator.TokenType.STRING, 3, 6
		);
		// Strings before numbers
		doTestNextToken(
			"Dan1", 0,
			SmartComparator.TokenType.STRING, 0, 3
		);
		doTestNextToken(
			"Dan1", 3,
			SmartComparator.TokenType.NUMERIC, 3, 4
		);
		doTestNextToken(
			"Dan.1", 0,
			SmartComparator.TokenType.STRING, 0, 3
		);
		doTestNextToken(
			"Dan.1", 3,
			SmartComparator.TokenType.NUMERIC, 3, 5
		);
		doTestNextToken(
			"Dan-1", 0,
			SmartComparator.TokenType.STRING, 0, 3
		);
		doTestNextToken(
			"Dan-1", 3,
			SmartComparator.TokenType.NUMERIC, 3, 5
		);
		doTestNextToken(
			"Dan-1.", 0,
			SmartComparator.TokenType.STRING, 0, 3
		);
		doTestNextToken(
			"Dan-1.", 3,
			SmartComparator.TokenType.NUMERIC, 3, 6
		);
		// Ending with partial numeric beginning
		doTestNextToken(
			"Dan-", 0,
			SmartComparator.TokenType.STRING, 0, 4
		);
		doTestNextToken(
			"Dan.", 0,
			SmartComparator.TokenType.STRING, 0, 4
		);
		doTestNextToken(
			"Dan-.", 0,
			SmartComparator.TokenType.STRING, 0, 5
		);
	}

	private static void doTestUnequal(SmartComparator comparator, String lower, String higher) {
		assertTrue(comparator.compare(lower, higher) < 0);
		assertTrue(comparator.compare(higher, lower) > 0);
	}

	private static void doTestEqual(SmartComparator comparator, String s1, String s2) {
		assertEquals(0, comparator.compare(s1, s2));
		assertEquals(0, comparator.compare(s2, s1));
	}

	public void testCompare() {
		doTestEqual(comparator, "", "");
		doTestUnequal(comparator, "10a", "10A");
		doTestUnequal(comparator, "10", "10A");
		doTestUnequal(comparator, "2", "10");
		doTestUnequal(comparator, "2a", "10");
		doTestUnequal(comparator, "2", "10a");
		doTestUnequal(comparator, "2a", "10a");
		doTestEqual(comparator, "2a", "2a");
		doTestUnequal(comparator, "2a2", "2a100");
		doTestUnequal(comparator, "2a2", "2a1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
		doTestUnequal(comparator, "Dan", "Dan 2");
		doTestUnequal(comparator, "Dan 1", "Dan 2");
		doTestUnequal(comparator, "Dan 2", "Dan 100");
		doTestUnequal(comparator, "Dan 100 A", "Dan 100 B");
		doTestUnequal(comparator, "dan", "Dan");
		doTestUnequal(comparator, "Dan 0 Test", "Dan 0.0 Test");
		doTestUnequal(comparator, "Dan 0.0 Test", "Dan 0.00000 Test");
		doTestUnequal(comparator, "Dan -0.0 Test", "Dan 0.00000 Test");
	}

	public void testSort() {
		String[] testValues = {
			"-10",
			"-10.",
			"-10.0",
			"-10.00",
			"-10.000",
			"-1",
			"-1.",
			"-1.0",
			"-1.00",
			"-1.000",
			"-.1",
			"-.10",
			"-.100",
			"-.1000",
			"-0.1",
			"-0.10",
			"-0.100",
			"-0.1000",
			"-.0",
			"-.00",
			"-.000",
			"-0",
			"-0.",
			"-0.0",
			"-0.00",
			"-0.000",
			".0",
			".00",
			".000",
			"0",
			"0.",
			"0.0",
			"0.00",
			"0.000",
			".1",
			".10",
			".100",
			".1000",
			"0.1",
			"0.10",
			"0.100",
			"0.1000",
			"1",
			"1.",
			"1.0",
			"1.00",
			"1.000",
			"10",
			"10.",
			"10.0",
			"10.00",
			"10.000"
		};
		String[] shuffled = Arrays.copyOf(testValues, testValues.length);
		Collections.shuffle(Arrays.asList(shuffled));
		Arrays.sort(shuffled, comparator);
		assertEquals(
			Arrays.asList(testValues),
			Arrays.asList(shuffled)
		);
	}
}
