/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2026  AO Industries, Inc.
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

import static org.junit.Assert.assertNotSame;

import org.junit.Test;

/**
 * Tests {@link Throwables}.
 *
 * @author  AO Industries, Inc.
 */
public class ThrowablesTest {

  /**
   * This is sufficient to trigger the static initializers in {@link Throwable}, which effectively accomplishes
   * the Java version compatibility across all registered exception types.
   */
  @Test
  public void testNewSurrogate() {
    Exception original = new Exception("Test exception");
    @SuppressWarnings("ThrowableResultIgnored")
    Exception surrogate = Throwables.newSurrogate(original);
    assertNotSame(original, surrogate);
  }
}
