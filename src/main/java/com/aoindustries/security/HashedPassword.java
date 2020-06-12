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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * A salted, hashed and key stretched password.
 * <a href="https://crackstation.net/hashing-security.htm">https://crackstation.net/hashing-security.htm</a>
 *
 * @author  AO Industries, Inc.
 */
public class HashedPassword {

	/** From http://crackstation.net/hashing-security.htm */
	private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1"; // TODO: Change to PBKDF2WithHmacSHA512?

	/** The number of bytes in the random salt. */
	public static final int SALT_BYTES = 256 / 8;

	/** The number of bytes in the hash. */
	public static final int HASH_BYTES = 256 / 8;

	/**
	 * The recommended number of iterations for typical usage.
	 * <p>
	 * We may change this value between releases without notice.
	 * Only use this value for new password hashes.
	 * Always store the iterations with the salt and hash, and use the stored
	 * iterations when checking password matches.
	 * </p>
	 *
	 * @see  #hash(java.lang.String, byte[], int) 
	 */
	public static final int RECOMMENDED_ITERATIONS = 1000;

	private static final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Generates a random salt of <code>SALT_BYTES</code> bytes in length.
	 * 
	 * @see  #hash(java.lang.String, byte[], int) 
	 */
	public static byte[] generateSalt() {
		byte[] salt = new byte[SALT_BYTES];
		secureRandom.nextBytes(salt);
		return salt;
	}

	/**
	 * Hash the given password
	 * 
	 * @see  #generateSalt()
	 * @see  #RECOMMENDED_ITERATIONS
	 */
	public static byte[] hash(String password, byte[] salt, int iterations) {
		try {
			char[] chars = password.toCharArray();
			try {
				// See http://crackstation.net/hashing-security.htm
				PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, HASH_BYTES * 8);
				SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
				byte[] hash = skf.generateSecret(spec).getEncoded();
				assert hash.length == HASH_BYTES;
				return hash;
			} finally {
				Arrays.fill(chars, (char)0);
			}
		} catch(InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new WrappedException(e);
		}
	}

	private final byte[] salt;
	private final int iterations;
	private final byte[] hash;

	/**
	 * @param salt  The provided parameter is zeroed
	 * @param iterations  The number of has iterations
	 * @param hash  The provided parameter is zeroed 
	 */
	public HashedPassword(
		byte[] salt,
		int iterations,
		byte[] hash
	) {
		this.salt = Arrays.copyOf(salt, salt.length);
		Arrays.fill(salt, (byte)0);
		this.iterations = iterations;
		this.hash = Arrays.copyOf(hash, hash.length);
		Arrays.fill(hash, (byte)0);
	}

	@Override
	public String toString() {
		return "*";
		/* Do not leak hash
		return
			'('
			+ Strings.convertToHex(passwordSalt)
			+ ", "
			+ passwordIterations
			+ ", "
			+ Strings.convertToHex(passwordHash)
			+ ')'
		;
		 */
	}

	/**
	 * Checks if this matches the provided password.
	 * Performs comparisons in length-constant time.
	 * <a href="https://crackstation.net/hashing-security.htm">https://crackstation.net/hashing-security.htm</a>
	 */
	public boolean matches(String password) {
		// Hash again with the original salt and iterations
		byte[] newHash = hash(password, salt, iterations);
		try {
			return slowEquals(
				hash,
				newHash
			);
		} finally {
			Arrays.fill(newHash, (byte)0);
		}
	}

	/**
	 * Compares two byte arrays in length-constant time. This comparison method
	 * is used so that password hashes cannot be extracted from an on-line 
	 * system using a timing attack and then attacked off-line.
	 * <a href="https://crackstation.net/hashing-security.htm">https://crackstation.net/hashing-security.htm</a>
	 *
	 * @param   a       the first byte array
	 * @param   b       the second byte array 
	 * @return          true if both byte arrays are the same, false if not
	 */
	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		for(int i = 0; i < a.length && i < b.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}
}
