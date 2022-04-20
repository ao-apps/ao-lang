/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.time;

/**
 * Access to the current system clock.
 * <p>
 * This will be deprecated once Java 8 is ubiquitous and only serves as an extremely
 * simplified stop-gap.
 * </p>
 *
 * @author  AO Industries, Inc.
 *
 * @deprecated  Please use standard Java 8 classes.
 */
@Deprecated
public final class Clock {

  /** Make no instances. */
  private Clock() {
    throw new AssertionError();
  }

  /**
   * Gets the current time with up to millisecond precision.
   *
   * @see  System#currentTimeMillis()
   */
  public static Instant instant() {
    long millis = System.currentTimeMillis();
    long seconds = millis / 1000;
    int nanos = (int)(millis % 1000) * 1000000;
    if (nanos < 0) {
      seconds--;
      nanos += 1000000000;
    }
    return new Instant(seconds, nanos);
  }
}
