/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2016, 2017, 2021, 2022, 2023  AO Industries, Inc.
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
 * Static utilities for dealing with long values as if they are unsigned.
 *
 * @deprecated  Please use methods available in {@link Long} as of Java 8.
 *
 * @author  AO Industries, Inc.
 */
@Deprecated
public final class UnsignedLong {

  /** Make no instances. */
  private UnsignedLong() {
    throw new AssertionError();
  }

  /**
   * Divides two unsigned long values.
   *
   * @deprecated  Please use {@link Long#divideUnsigned(long, long)} as of Java 8.
   */
  @Deprecated
  public static long divide(long dividend, long divisor) {
    return Long.divideUnsigned(dividend, divisor);
  }

  /**
   * Gets the remainder (modulus) from a division.
   *
   * @deprecated  Please use {@link Long#remainderUnsigned(long, long)} as of Java 8.
   */
  @Deprecated
  public static long remainder(long dividend, long divisor) {
    return Long.remainderUnsigned(dividend, divisor);
  }
}
