/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoapps.lang;

/**
 * Runtime utilities that enhance behavior of java.lang.Runtime.
 */
public final class RuntimeUtils {

  /** Make no instances. */
  private RuntimeUtils() {
    throw new AssertionError();
  }

  private static class AvailableProcessorsLock {
    private AvailableProcessorsLock() {
      // Empty lock class to help heap profile
    }
  }

  private static final AvailableProcessorsLock availableProcessorsLock = new AvailableProcessorsLock();
  private static long availableProcessorsLastRetrieved = Long.MIN_VALUE;
  private static int availableProcessors;

  /**
   * Faster way to get the number of processors in the system.
   *
   * <p>The call the Runtime.availableProcessors is prohibitively slow (at least
   * in Java 1.6 on Debian 6).  The number of processors in a system is unlikely
   * to change frequently.  This will only call Runtime.availableProcessors
   * once a second.</p>
   */
  public static int getAvailableProcessors() {
    long currentTime = System.currentTimeMillis();
    synchronized (availableProcessorsLock) {
      long timeSince;
      if (
          availableProcessors == 0
              || (timeSince = availableProcessorsLastRetrieved - currentTime) >= 1000
              || timeSince <= -1000 // System time set to the past
      ) {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        availableProcessorsLastRetrieved = currentTime;
      }
      return availableProcessors;
    }
  }
}
