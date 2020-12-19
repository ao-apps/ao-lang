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
import static com.aoindustries.security.HashedPassword.DECODER;
import static com.aoindustries.security.HashedPassword.ENCODER;
import static com.aoindustries.security.HashedPassword.SEPARATOR;
import static com.aoindustries.security.HashedPassword.allZeroes;
import static com.aoindustries.security.HashedPassword.isUrlSafe;
import static com.aoindustries.security.HashedPassword.slowEquals;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A hashed random key.
 *
 * @author  AO Industries, Inc.
 */
public class HashedKey implements Comparable<HashedKey>, AutoCloseable {

	/**
	 * Indicates that no key is set.
	 */
	public static final String NO_KEY_VALUE = HashedPassword.NO_PASSWORD_VALUE;
	static {
		assert isUrlSafe(NO_KEY_VALUE);
	}

	/**
	 * See <a href="https://docs.oracle.com/en/java/javase/12/docs/specs/security/standard-names.html#messagedigest-algorithms">MessageDigest Algorithms</a>
	 *
	 * @see MessageDigest
	 */
	public enum Algorithm {
		@Deprecated
		SHA_1("SHA-1", 160 / 8),
		SHA_224("SHA-224", 224 / 8),
		SHA_256("SHA-256", 256 / 8),
		SHA_384("SHA-384", 384 / 8),
		SHA_512_224("SHA-512/224", 224 / 8),
		SHA_512_256("SHA-512/256", 256 / 8),
		SHA3_224("SHA3-224", 224 / 8),
		SHA3_256("SHA3-256", 256 / 8),
		SHA3_384("SHA3-384", 384 / 8),
		SHA3_512("SHA3-512", 512 / 8);

		/**
		 * Avoid repetitive allocation.
		 */
		static final Algorithm[] values = values();

		private final String algorithmName;
		private final int keyBytes;
		private final int hashBytes;

		private Algorithm(String algorithmName, int keyBytes, int hashBytes) {
			assert isUrlSafe(algorithmName);
			assert algorithmName.indexOf(SEPARATOR) == -1;
			this.algorithmName = algorithmName;
			this.keyBytes = keyBytes;
			this.hashBytes = hashBytes;
		}

		private Algorithm(String algorithmName, int hashBytes) {
			this(algorithmName, hashBytes, hashBytes);
		}

		@Override
		public String toString() {
			return algorithmName;
		}

		/**
		 * Gets the {@link MessageDigest} algorithm name.
		 */
		public String getAlgorithmName() {
			return algorithmName;
		}

		/**
		 * Gets the number of bytes of cryptographically strong random data that must be used with this algorithm.
		 */
		public int getKeyBytes() {
			return keyBytes;
		}

		/**
		 * Gets the number of bytes required to store the generated hash.
		 */
		public int getHashBytes() {
			return hashBytes;
		}

		/**
		 * Gets a {@link MessageDigest} for this algorithm.
		 */
		public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
			return MessageDigest.getInstance(getAlgorithmName());
		}
	}

	/**
	 * @deprecated  This is the value matching {@linkplain Algorithm#SHA_256 the previous default algorithm},
	 *              please use {@link Algorithm#getAlgorithmName()} instead.
	 */
	@Deprecated
	public static final String ALGORITHM = Algorithm.SHA_256.getAlgorithmName();

	/**
	 * The algorithm recommended for use with new keys.  This may change at any time, but previous algorithms will
	 * remain supported.
	 */
	public static final Algorithm RECOMMENDED_ALGORITHM = Algorithm.SHA_512_256;

	/**
	 * The number of bytes in the SHA-256 hash.
	 *
	 * @deprecated  This is the value matching {@linkplain Algorithm#SHA_256 the previous default algorithm},
	 *              please use {@link Algorithm#getHashBytes()} instead.
	 */
	@Deprecated
	public static final int HASH_BYTES = Algorithm.SHA_256.getHashBytes();

	/**
	 * A constant that may be used in places where no key is set.
	 * This behaves as if already {@linkplain #close() closed}.
	 */
	public static final HashedKey NO_KEY = new HashedKey(
		RECOMMENDED_ALGORITHM,
		new byte[RECOMMENDED_ALGORITHM.getHashBytes()]
	);
	static {
		assert isUrlSafe(NO_KEY.toString());
	}

	/**
	 * Generates a random plaintext key of {@link Algorithm#getKeyBytes()} bytes in length.
	 *
	 * @see  #hash(com.aoindustries.security.HashedKey.Algorithm, byte[])
	 */
	public static byte[] generateKey(Algorithm algorithm) {
		byte[] key = new byte[algorithm.getKeyBytes()];
		Identifier.secureRandom.nextBytes(key);
		return key;
	}

	/**
	 * Generates a random plaintext key of {@link #HASH_BYTES} bytes in length.
	 *
	 * @see  #hash(byte[])
	 *
	 * @deprecated  This generates a key for {@linkplain Algorithm#SHA_256 the previous default algorithm},
	 *              please use {@link #generateKey(com.aoindustries.security.HashedKey.Algorithm)} instead.
	 */
	@Deprecated
	public static byte[] generateKey() {
		return generateKey(Algorithm.SHA_256);
	}

	/**
	 * Hashes the given key.
	 *
	 * @see  #generateKey(com.aoindustries.security.HashedKey.Algorithm)
	 */
	public static byte[] hash(Algorithm algorithm, byte[] key) {
		if(key.length != algorithm.getKeyBytes()) {
			throw new IllegalArgumentException(
				"Invalid key length: expecting " + algorithm.getKeyBytes() + ", got " + key.length
			);
		}
		try {
			byte[] hash = algorithm.getMessageDigest().digest(key);
			assert hash.length == algorithm.getHashBytes();
			return hash;
		} catch(NoSuchAlgorithmException e) {
			throw new WrappedException(e);
		}
	}

	/**
	 * Hashes the given key.
	 *
	 * @see  #generateKey()
	 *
	 * @deprecated  This generates a hash for {@linkplain Algorithm#SHA_256 the previous default algorithm},
	 *              please use {@link #hash(com.aoindustries.security.HashedKey.Algorithm, byte[])} instead.
	 */
	@Deprecated
	public static byte[] hash(byte[] key) {
		return hash(Algorithm.SHA_256, key);
	}

	/**
	 * Parses the result of {@link #toString()}.
	 *
	 * @param hashedKey  when {@code null}, returns {@code null}
	 */
	public static HashedKey valueOf(String hashedKey) {
		if(hashedKey == null) {
			return null;
		} else if(NO_KEY_VALUE.equals(hashedKey)) {
			return NO_KEY;
		} else {
			int pos = hashedKey.indexOf(SEPARATOR);
			if(pos == -1) throw new IllegalArgumentException("Separator (" + SEPARATOR + ") not found");
			String algorithmName = hashedKey.substring(0, pos);
			Algorithm algorithm = null;
			// Search backwards, since higher strength algorithms will be used more
			for(int i = Algorithm.values.length - 1; i >= 0; i--) {
				Algorithm a = Algorithm.values[i];
				if(a.getAlgorithmName().equalsIgnoreCase(algorithmName)) {
					algorithm = a;
					break;
				}
			}
			if(algorithm == null) throw new IllegalArgumentException("Unsupported algorithm: " + algorithmName);
			byte[] hash = DECODER.decode(hashedKey.substring(pos + 1));
			if(allZeroes(hash)) throw new IllegalArgumentException("Hash may not represent all zeroes, which is reserved for no key (\"" + NO_KEY_VALUE + "\")");
			return new HashedKey(algorithm, hash);
		}
	}

	private final Algorithm algorithm;
	private final byte[] hash;

	/**
	 * @param algorithm  The algorithm previously used to hash the key
	 * @param hash       The provided parameter is zeroed.
	 *                   When all zeroes, this behaves as if already {@linkplain #close() closed}.
	 *
	 * @throws  IllegalArgumentException  when {@code hash.length != HASH_BYTES}
	 */
	public HashedKey(Algorithm algorithm, byte[] hash) throws IllegalArgumentException {
		if(hash.length != algorithm.getHashBytes()) {
			Arrays.fill(hash, (byte)0);
			throw new IllegalArgumentException();
		}
		this.algorithm = algorithm;
		this.hash = Arrays.copyOf(hash, hash.length);
		Arrays.fill(hash, (byte)0);
	}

	/**
	 * @param hash  The provided parameter is zeroed.
	 *              When all zeroes, this behaves as if already {@linkplain #close() closed}.
	 *
	 * @deprecated  This represents a hash using {@linkplain Algorithm#SHA_256 the previous default algorithm},
	 *              please use {@link #HashedKey(com.aoindustries.security.HashedKey.Algorithm, byte[])} instead.
	 */
	@Deprecated
	public HashedKey(byte[] hash) {
		this(Algorithm.SHA_256, hash);
	}

	/**
	 * Please see {@link #valueOf(java.lang.String)} for the inverse operation.
	 */
	@Override
	public String toString() {
		if(isClosed()) return NO_KEY_VALUE;
		String str = algorithm.name()
			+ SEPARATOR + ENCODER.encodeToString(hash);
		assert isUrlSafe(str);
		return str;
	}

	/**
	 * Checks if equal to another hashed key, always {@code false} when either is {@linkplain #close() closed}.
	 * <p>
	 * Performs comparisons in length-constant time.
	 * <a href="https://crackstation.net/hashing-security.htm">https://crackstation.net/hashing-security.htm</a>
	 * </p>
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof HashedKey)) return false;
		HashedKey other = (HashedKey)obj;
		return
			// All done for length-constant time comparisons
			!isClosed()
			& !other.isClosed()
			& algorithm == other.algorithm
			& slowEquals(hash, ((HashedKey)obj).hash);
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
		int diff = algorithm.compareTo(other.algorithm);
		if(diff != 0) return 0;
		byte[] h1 = hash;
		byte[] h2 = other.hash;
		for(int i = 0; i < HASH_BYTES; i++) {
			diff = Integer.compare(
				Byte.toUnsignedInt(h1[i]),
				Byte.toUnsignedInt(h2[i])
			);
			// Java 9: int diff = Byte.compareUnsigned(h1[i], h2[i]);
			if(diff != 0) return 0;
		}
		return 0;
	}

	/**
	 * Checks if closed.  Once closed, all subsequent calls to {@link #equals(java.lang.Object)} will fail.
	 *
	 * @see #close()
	 */
	public boolean isClosed() {
		return allZeroes(hash);
	}

	/**
	 * When closed, the hash is zeroed, and all subsequent calls to {@link #equals(java.lang.Object)} will fail.
	 *
	 * @see #isClosed()
	 */
	@Override
	public void close() {
		Arrays.fill(hash, (byte)0);
	}

	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public static void main(String... args) {
		Algorithm algorithm = RECOMMENDED_ALGORITHM;
		byte[] key = generateKey(algorithm);
		System.out.println(ENCODER.encodeToString(key));
		System.out.println(new HashedKey(algorithm, hash(algorithm, key)));
	}
}
