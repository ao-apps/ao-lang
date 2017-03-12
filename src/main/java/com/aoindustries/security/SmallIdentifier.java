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
package com.aoindustries.security;

import com.aoindustries.io.IoUtils;
import com.aoindustries.math.LongLong;
import static com.aoindustries.math.UnsignedLong.divide;
import static com.aoindustries.security.Identifier.BASE;
import java.io.Serializable;
import java.util.Random;

/**
 * A 64-bit random identifier internally stored as a long value.
 *
 * @author  AO Industries, Inc.
 */
public class SmallIdentifier implements Serializable, Comparable<SmallIdentifier> {

	private static final long serialVersionUID = 1L;

	/**
	 * @see  #toString()
	 */
	public static SmallIdentifier valueOf(String encoded) throws IllegalArgumentException {
		return new SmallIdentifier(encoded);
	}

	private final long value;

	/**
	 * Creates a new, random Identifier using the default SecureRandom instance.
	 */
	public SmallIdentifier() {
		this(Identifier.secureRandom);
	}

	/**
	 * Creates a new, random Identifier using the provided Random source.
	 */
	public SmallIdentifier(Random random) {
		byte[] bytes = new byte[8];
		random.nextBytes(bytes);
		value = IoUtils.bufferToLong(bytes);
	}

	public SmallIdentifier(long value) {
		this.value = value;
	}

	/**
	 * @see  #toString()
	 */
	public SmallIdentifier(String encoded) throws IllegalArgumentException {
		if(encoded.length()!=11) throw new IllegalArgumentException();
		this.value = Identifier.decode(encoded);
	}

	@Override
	public boolean equals(Object O) {
		if(!(O instanceof SmallIdentifier)) return false;
		return equals((SmallIdentifier)O);
	}

	public boolean equals(SmallIdentifier other) {
		return
			other!=null
			&& value==other.value
		;
	}

	@Override
	public int hashCode() {
		// The values should be well distributed, any set of 32 bits should be equally good.
		return (int)value;
	}

	/**
	 * The external representation is a string of characters encoded in base 57, with
	 * 11 characters for "value".
	 */
	@Override
	public String toString() {
		return new String(
			new char[] {
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE * BASE)),
				Identifier.getCharacter(divide(value, BASE)),
				Identifier.getCharacter(value)
			}
		);
	}

	/**
	 * Unsigned ordering.
	 */
	@Override
	public int compareTo(SmallIdentifier other) {
		return LongLong.compareUnsigned(value, other.value);
	}

	public long getValue() {
		return value;
	}
}