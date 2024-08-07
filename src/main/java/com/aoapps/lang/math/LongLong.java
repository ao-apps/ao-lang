/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoapps.lang.math;

import com.aoapps.lang.io.IoUtils;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * 128-bit number useful for storing values such as MD5 hashes and IPv6 addresses.
 * This class generally matches the standard <code>java.lang.Long</code> implementation.
 *
 * @author  AO Industries, Inc.
 */
public class LongLong extends Number implements Comparable<LongLong> {

  public static final LongLong MIN_VALUE = valueOf(Long.MIN_VALUE, 0L);

  public static final LongLong MAX_VALUE = valueOf(Long.MAX_VALUE, -1L);

  private static final int CACHE_MIN = -128;
  private static final int CACHE_MAX = 127;

  private static final class LongLongCache {

    /** Make no instances. */
    private LongLongCache() {
      throw new AssertionError();
    }

    static final LongLong[] cache = new LongLong[-CACHE_MIN + CACHE_MAX + 1];

    static {
      for (int i = 0; i < cache.length; i++) {
        int value = i + CACHE_MIN;
        cache[i] = new LongLong(value < 0 ? -1 : 0, value);
      }
    }
  }

  private static byte[] getBytes(BigInteger bigInteger) {
    if (bigInteger.bitLength() >= SIZE) {
      throw new NumberFormatException("For input string: \"" + bigInteger.toString() + "\"");
    }
    byte[] bytes = bigInteger.toByteArray();
    int diff = BYTES - bytes.length;
    if (diff < 0) {
      throw new NumberFormatException("For input string: \"" + bigInteger.toString() + "\"");
    }
    if (diff > 0) {
      // Pad with the sign bit
      byte[] newBytes = new byte[BYTES];
      if ((bytes[0] & 0x80) != 0) {
        Arrays.fill(newBytes, 0, diff, (byte) 0xff);
      }
      System.arraycopy(bytes, 0, newBytes, diff, bytes.length);
      bytes = newBytes;
    }
    return bytes;
  }

  private static byte[] parseLongLongToBytes(String s, int radix) throws NumberFormatException {
    return getBytes(new BigInteger(s, radix));
  }

  public static LongLong parseLongLong(String s, int radix) throws NumberFormatException {
    byte[] bytes = parseLongLongToBytes(s, radix);
    return valueOf(
        IoUtils.bufferToLong(bytes),
        IoUtils.bufferToLong(bytes, Long.BYTES)
    );
  }

  public static LongLong parseLongLong(String s) throws NumberFormatException {
    return parseLongLong(s, 10);
  }

  /**
   * @see  #parseLongLong(java.lang.String, int)
   */
  public static LongLong valueOf(String s, int radix) throws NumberFormatException {
    return parseLongLong(s, radix);
  }

  /**
   * @see  #parseLongLong(java.lang.String, int)
   */
  public static LongLong valueOf(String s) throws NumberFormatException {
    return parseLongLong(s, 10);
  }

  public static LongLong valueOf(long hi, long lo) {
    if (
        (hi == -1 && lo >= CACHE_MIN && lo < 0)
            || (hi == 0 && lo <= CACHE_MAX && lo >= 0)
    ) {
      // will cache
      return LongLongCache.cache[(int) lo - CACHE_MIN];
    }
    return new LongLong(hi, lo);
  }

  /* Unused
  public static LongLong decode(String nm) throws NumberFormatException {
    int radix = 10;
    int index = 0;
    boolean negative = false;
    LongLong result;

    // Handle minus sign, if present
    if (nm.startsWith("-")) {
      negative = true;
      index++;
    }

    // Handle radix specifier, if present
    if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
      index += 2;
      radix = 16;
    } else if (nm.startsWith("#", index)) {
      index++;
      radix = 16;
    } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
      index++;
      radix = 8;
    }

    if (nm.startsWith("-", index)) {
      throw new NumberFormatException("Negative sign in wrong position");
    }

    try {
      result = LongLong.valueOf(nm.substring(index), radix);
      result = negative ? result.negate() : result;
    } catch (NumberFormatException e) {
      // If number is LongLong.MIN_VALUE, we'll end up here. The next line
      // handles this case, and causes any genuine format error to be
      // rethrown.
      String constant = negative ? new String("-" + nm.substring(index))
          : nm.substring(index);
      result = LongLong.valueOf(constant, radix);
    }
    return result;
  }*/

  private static final long serialVersionUID = -8296704159343817686L;

  private final long hi;
  private final long lo;

  public LongLong(long hi, long lo) {
    this.hi = hi;
    this.lo = lo;
  }

  public LongLong(String s) throws NumberFormatException {
    byte[] bytes = parseLongLongToBytes(s, 10);
    this.hi = IoUtils.bufferToLong(bytes);
    this.lo = IoUtils.bufferToLong(bytes, Long.BYTES);
  }

  @Override
  public byte byteValue() {
    return (byte) lo;
  }

  @Override
  public short shortValue() {
    return (short) lo;
  }

  @Override
  public int intValue() {
    return (int) lo;
  }

  @Override
  public long longValue() {
    return lo;
  }

  private BigInteger getBigInteger() {
    byte[] bytes = new byte[BYTES];
    IoUtils.longToBuffer(hi, bytes);
    IoUtils.longToBuffer(lo, bytes, Long.BYTES);
    return new BigInteger(bytes);
  }

  @Override
  public float floatValue() {
    return getBigInteger().floatValue();
  }

  @Override
  public double doubleValue() {
    return getBigInteger().doubleValue();
  }

  @Override
  public String toString() {
    return getBigInteger().toString();
  }

  public static int hashCode(long hi, long lo) {
    return Long.hashCode(hi) ^ Long.hashCode(lo);
  }

  @Override
  public int hashCode() {
    return hashCode(hi, lo);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LongLong) {
      LongLong other = (LongLong) obj;
      return (hi == other.hi) && (lo == other.lo);
    }
    return false;
  }

  /* Unused
  public static LongLong getLongLong(String nm) {
    return getLongLong(nm, null);
  }*/

  /* Unused
  public static LongLong getLongLong(String nm, LongLong val) {
    String v = null;
    try {
      v = System.getProperty(nm);
    } catch (IllegalArgumentException e) {
      // TODO
    } catch (NullPointerException e) {
      // TODO
    }
    if (v != null) {
      try {
        return LongLong.decode(v);
      } catch (NumberFormatException e) {
        // TODO
      }
    }
    return val;
  }*/

  /**
   * Compares two longs as unsigned.
   *
   * @deprecated  Please use {@link Long#compareUnsigned(long, long)} as of Java 8.
   */
  @Deprecated
  public static int compareUnsigned(long value1, long value2) {
    return Long.compareUnsigned(value1, value2);
  }

  @Override
  public int compareTo(LongLong other) {
    if (hi < other.hi) {
      return -1;
    }
    if (hi > other.hi) {
      return 1;
    }
    //return (lo<other.lo ? -1 : (lo == other.lo ? 0 : 1));
    // Compare lo as unsigned
    return Long.compareUnsigned(lo, other.lo);
  }


  public int compareToUnsigned(LongLong other) {
    int diff = Long.compareUnsigned(hi, other.hi);
    if (diff != 0) {
      return diff;
    }
    return Long.compareUnsigned(lo, other.lo);
  }

  /**
   * The number of bits used to represent a {@link LongLong} value in two's
   * complement binary form.
   */
  public static final int SIZE = Long.SIZE * 2;

  /**
   * The number of bytes used to represent a {@link LongLong} value in two's
   * complement binary form.
   */
  public static final int BYTES = SIZE / Byte.SIZE;


  public LongLong highestOneBit() {
    long newHi = Long.highestOneBit(hi);
    long newLo;
    if (newHi != 0) {
      newLo = 0;
    } else {
      newLo = Long.highestOneBit(lo);
    }
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  public LongLong lowestOneBit() {
    long newLo = Long.lowestOneBit(lo);
    long newHi;
    if (newLo != 0) {
      newHi = 0;
    } else {
      newHi = Long.lowestOneBit(hi);
    }
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  public int numberOfLeadingZeros() {
    int leading = Long.numberOfLeadingZeros(hi);
    if (leading == 64) {
      leading += Long.numberOfLeadingZeros(lo);
    }
    return leading;
  }

  public int numberOfTrailingZeros() {
    int trailing = Long.numberOfTrailingZeros(lo);
    if (trailing == 64) {
      trailing += Long.numberOfTrailingZeros(hi);
    }
    return trailing;
  }

  public int bitCount() {
    return Long.bitCount(hi) + Long.bitCount(lo);
  }

  /**
   * TODO: test this.
   */
  public LongLong rotateLeft(int distance) {
    // Rotate multiple of 128 yields same answer
    if ((distance & (SIZE - 1)) == 0) {
      return this;
    }
    // Rotate multiple of 64 yields swapped longs
    long newHi;
    long newLo;
    if ((distance & (Long.SIZE - 1)) == 0) {
      newHi = lo;
      newLo = hi;
    } else {
      // TODO: test this
      newHi = (hi << distance) | (lo >>> -distance);
      newLo = (lo << distance) | (hi >>> -distance);
    }
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  /**
   * TODO: test this.
   */
  public LongLong rotateRight(int distance) {
    // Rotate multiple of 128 yields same answer
    if ((distance & (SIZE - 1)) == 0) {
      return this;
    }
    // Rotate multiple of 64 yields swapped longs
    long newHi;
    long newLo;
    if ((distance & (Long.SIZE - 1)) == 0) {
      newHi = lo;
      newLo = hi;
    } else {
      // TODO: test this
      newHi = (hi >>> distance) | (lo << -distance);
      newLo = (lo >>> distance) | (hi << -distance);
    }
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  public LongLong reverse() {
    long newHi = Long.reverse(lo);
    long newLo = Long.reverse(hi);
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  public int signum() {
    return Long.signum(hi);
  }

  public LongLong reverseBytes() {
    long newHi = Long.reverseBytes(lo);
    long newLo = Long.reverseBytes(hi);
    if (newHi == hi && newLo == lo) {
      return this;
    }
    return valueOf(newHi, newLo);
  }

  public LongLong negate() {
    byte[] bytes = getBytes(getBigInteger().negate());
    return valueOf(
        IoUtils.bufferToLong(bytes),
        IoUtils.bufferToLong(bytes, Long.BYTES)
    );
  }

  /**
   * Gets the high-order long.
   */
  public long getHigh() {
    return hi;
  }

  /**
   * Gets the low-order long.
   */
  public long getLow() {
    return lo;
  }
}
