/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.util;

/**
 * @author  AO Industries, Inc.
 */
public final class InternUtils {

  /** Make no instances. */
  private InternUtils() {
    throw new AssertionError();
  }

  /**
   * Interns the object, return null when null.
   */
  public static <T extends Internable<T>> T intern(T value) {
    if (value == null) {
      return null;
    }
    return value.intern();
  }

  /**
   * Null-safe intern: interns a String if it is not null, returns null if parameter is null.
   */
  public static String intern(String s) {
    if (s == null) {
      return null;
    }
    return s.intern();
  }
}
