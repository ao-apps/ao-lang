/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2013, 2016, 2017, 2018, 2019, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.nio.charset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Defines some constants for handling standard character sets.
 *
 * @deprecated  Use {@link java.nio.charset.StandardCharsets} as of Java 1.7.
 *
 * @author  AO Industries, Inc.
 */
@Deprecated
public final class Charsets {

  /** Make no instances. */
  private Charsets() {
    throw new AssertionError();
  }

  /**
   * @deprecated  Use {@link java.nio.charset.StandardCharsets} as of Java 1.7.
   */
  @Deprecated
  public static final Charset
    US_ASCII   = StandardCharsets.US_ASCII,
    ISO_8859_1 = StandardCharsets.ISO_8859_1,
    UTF_8      = StandardCharsets.UTF_8,
    UTF_16BE   = StandardCharsets.UTF_16BE,
    UTF_16LE   = StandardCharsets.UTF_16LE,
    UTF_16     = StandardCharsets.UTF_16
  ;
}
