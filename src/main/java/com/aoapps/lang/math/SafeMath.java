/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2013, 2014, 2016, 2017, 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

/**
 * Math routines that check for overflow conditions.
 *
 * @author  AO Industries, Inc.
 */
public final class SafeMath {

  /** Make no instances. */
  private SafeMath() {
    throw new AssertionError();
  }

  /**
   * Casts int to byte, looking for any underflow or overflow.
   *
   * @exception  ArithmeticException  for underflow or overflow
   */
  public static byte castByte(int value) throws ArithmeticException {
    if (value < Byte.MIN_VALUE) {
      throw new ArithmeticException("byte underflow: " + value);
    }
    if (value > Byte.MAX_VALUE) {
      throw new ArithmeticException("byte overflow: " + value);
    }
    return (byte)value;
  }

  /**
   * Casts long to byte, looking for any underflow or overflow.
   *
   * @exception  ArithmeticException  for underflow or overflow
   */
  public static byte castByte(long value) throws ArithmeticException {
    if (value < Byte.MIN_VALUE) {
      throw new ArithmeticException("byte underflow: " + value);
    }
    if (value > Byte.MAX_VALUE) {
      throw new ArithmeticException("byte overflow: " + value);
    }
    return (byte)value;
  }

  /**
   * Casts int to short, looking for any underflow or overflow.
   *
   * @exception  ArithmeticException  for underflow or overflow
   */
  public static short castShort(int value) throws ArithmeticException {
    if (value < Short.MIN_VALUE) {
      throw new ArithmeticException("short underflow: " + value);
    }
    if (value > Short.MAX_VALUE) {
      throw new ArithmeticException("short overflow: " + value);
    }
    return (short)value;
  }

  /**
   * Casts long to short, looking for any underflow or overflow.
   *
   * @exception  ArithmeticException  for underflow or overflow
   */
  public static short castShort(long value) throws ArithmeticException {
    if (value < Short.MIN_VALUE) {
      throw new ArithmeticException("short underflow: " + value);
    }
    if (value > Short.MAX_VALUE) {
      throw new ArithmeticException("short overflow: " + value);
    }
    return (short)value;
  }

  /**
   * Casts long to int, looking for any underflow or overflow.
   *
   * @exception  ArithmeticException  for underflow or overflow
   */
  public static int castInt(long value) throws ArithmeticException {
    if (value < Integer.MIN_VALUE) {
      throw new ArithmeticException("int underflow: " + value);
    }
    if (value > Integer.MAX_VALUE) {
      throw new ArithmeticException("int overflow: " + value);
    }
    return (int)value;
  }

  /**
   * Multiplies two longs, looking for any overflow.
   *
   * @exception  ArithmeticException  for overflow
   *
   * @deprecated  Please use {@link Math#multiplyExact(long, long)}
   */
  @Deprecated
  public static long multiply(long value1, long value2) {
    return Math.multiplyExact(value1, value2);
  }

  /**
   * Multiplies any number of longs, looking for any overflow.
   *
   * @exception  ArithmeticException  for overflow
   *
   * @return  The product or {@code 1} when no values
   *
   * @see Math#multiplyExact(long, long)
   */
  public static long multiply(long ... values) {
    long product = 1;
    for (long value : values) {
      product = Math.multiplyExact(product, value);
    }
    return product;
  }

  /**
   * Computes the average of two values without overflow or underflow.
   */
  public static int avg(int value1, int value2) {
    return (int)(
      (
        (long)value1
        + (long)value2
      ) / 2
    );
  }

  /**
   * Computes the average of multiple values without overflow or underflow.
   *
   * @throws ArithmeticException  When values is empty (due to resulting division by zero)
   */
  public static int avg(int ... values) {
    long sum = 0;
    for (int value : values) sum += value;
    return (int)(sum / values.length);
  }
}
