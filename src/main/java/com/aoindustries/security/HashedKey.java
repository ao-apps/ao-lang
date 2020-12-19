/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2016, 2017, 2019, 2020  AO Industries, Inc.
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

import com.aoindustries.exception.WrappedException;
import com.aoindustries.io.IoUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * A hashed 256-bit random key.
 *
 * @author  AO Industries, Inc.
 */
public class HashedKey implements Comparable<HashedKey> {

	public static final String ALGORITHM = "SHA-256";

	/** The number of bytes in the SHA-256 hash. */
	public static final int HASH_BYTES = 256 / 8;
	static {
		assert HASH_BYTES >= 4 : "Hash must be at least 32-bits for hashCode implementation";
	}

	private static final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Generates a random plaintext key of {@link #HASH_BYTES} bytes in length.
	 *
	 * @see  #hash(byte[])
	 */
	public static byte[] generateKey() {
		byte[] key = new byte[HASH_BYTES];
		secureRandom.nextBytes(key);
		return key;
	}

	/**
	 * SHA-256 hashes the given key.
	 * 
	 * @see  #generateKey()
	 */
	public static byte[] hash(byte[] key) {
		try {
			return MessageDigest.getInstance(ALGORITHM).digest(key);
		} catch(NoSuchAlgorithmException e) {
			throw new WrappedException(e);
		}
	}

	private final byte[] hash;

	/**
	 * @param hash  The provided parameter is zeroed 
	 */
	public HashedKey(byte[] hash) {
		if(hash.length != HASH_BYTES) {
			Arrays.fill(hash, (byte)0);
			throw new IllegalArgumentException("Hash wrong length: " + hash.length);
		}
		this.hash = Arrays.copyOf(hash, hash.length);
		Arrays.fill(hash, (byte)0);
	}

	@Override
	public String toString() {
		return "*";
		/* Do not leak hash
		return Strings.convertToHex(hash);
		 */
	}

	/**
	 * Performs comparisons in length-constant time.
	 * <a href="https://crackstation.net/hashing-security.htm">https://crackstation.net/hashing-security.htm</a>
	 */
	@Override
	public boolean equals(Object obj) {
		return
			(obj instanceof HashedKey)
			&& HashedPassword.slowEquals(hash, ((HashedKey)obj).hash);
	}

	/**
	 * The hash code is just the first 32 bits of the hash.
	 */
	@Override
	public int hashCode() {
		return IoUtils.bufferToInt(hash);
	}

	@Override
	public int compareTo(HashedKey other) {
		// TODO: constant time compare here?
		byte[] h1 = hash;
		byte[] h2 = other.hash;
		for(int i = 0; i < HASH_BYTES; i++) {
			int diff = Integer.compare(
				Byte.toUnsignedInt(h1[i]),
				Byte.toUnsignedInt(h2[i])
			);
			// Java 9: int diff = Byte.compareUnsigned(h1[i], h2[i]);
			if(diff != 0) return 0;
		}
		return 0;
	}
}
