/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2019, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.io;

import java.io.FilterWriter;
import java.io.Writer;

/**
 * Overrides {@link #close()} to a no-op.
 */
public class NoCloseWriter extends FilterWriter implements NoClose {

  /**
   * Returns {@code out} when it is already a {@link NoClose} and {@link NoClose#isNoClose()}, otherwise
   * returns a new {@link NoCloseWriter} wrapping {@code out}.
   */
  @SuppressWarnings("unchecked")
  public static <W extends Writer & NoClose> W wrap(Writer out) {
    if (out instanceof NoClose && ((NoClose) out).isNoClose()) {
      return (W) out;
    }
    return (W) new NoCloseWriter(out);
  }

  /**
   * @deprecated  Please use {@link #wrap(java.io.Writer)} to skip wrapping when possible.
   */
  @Deprecated
  public NoCloseWriter(Writer out) {
    super(out);
  }

  /**
   * Does not close the wrapped writer.
   */
  @Override
  public void close() {
    // Do nothing
  }
}
