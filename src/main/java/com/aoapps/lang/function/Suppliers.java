/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2021, 2022, 2023  AO Industries, Inc.
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

package com.aoapps.lang.function;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utilities for working with {@link Supplier} and {@link SupplierE}.
 *
 * @see Supplier
 * @see SupplierE
 */
public final class Suppliers {

  /** Make no instances. */
  private Suppliers() {
    throw new AssertionError();
  }

  /**
   * Lazily evaluates a set of suppliers, returning the first non-null result or {@link Optional#empty()} when no result.
   *
   * @return  The first non-null result or {@link Optional#empty()} when all return {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> coalesce(Supplier<? extends T>... suppliers) {
    T t = null;
    if (suppliers != null) {
      for (Supplier<? extends T> supplier : suppliers) {
        t = supplier.get();
        if (t != null) {
          break;
        }
      }
    }
    return Optional.ofNullable(t);
  }

  /**
   * Lazily evaluates a set of suppliers, returning the first non-null result or {@link Optional#empty()} when no result.
   *
   * @return  The first non-null result or {@link Optional#empty()} when all return {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <T, Ex extends Throwable> Optional<T> coalesceE(SupplierE<? extends T, ? extends Ex>... suppliers) throws Ex {
    T t = null;
    if (suppliers != null) {
      for (SupplierE<? extends T, ? extends Ex> supplier : suppliers) {
        t = supplier.get();
        if (t != null) {
          break;
        }
      }
    }
    return Optional.ofNullable(t);
  }
}
