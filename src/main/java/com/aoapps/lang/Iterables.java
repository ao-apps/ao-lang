/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022, 2024  AO Industries, Inc.
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
 * A set of various {@link Iterable} over common Java types,
 * useful to avoid conflict after type erasure.
 *
 * <p>Sometimes different method overloads want to accept different types of
 * {@link Iterable}, but they cannot due to conflict after type erasure.</p>
 */
// TODO: @see  Suppliers
public final class Iterables {

  /** Make no instances. */
  private Iterables() {
    throw new AssertionError();
  }

  /**
   * An {@link Iterable} over {@link java.lang.Boolean},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Boolean<T extends java.lang.Boolean> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Byte},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Byte<T extends java.lang.Byte> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Character},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Character<T extends java.lang.Character> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.CharSequence},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface CharSequence<T extends java.lang.CharSequence> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Double},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Double<T extends java.lang.Double> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Enum},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Enum<E extends java.lang.Enum<E>, T extends E> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Float},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Float<T extends java.lang.Float> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Integer},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Integer<T extends java.lang.Integer> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Long},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Long<T extends java.lang.Long> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Number},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Number<T extends java.lang.Number> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.Short},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface Short<T extends java.lang.Short> extends Iterable<T> {
  }

  /**
   * An {@link Iterable} over {@link java.lang.String},
   * useful to avoid conflict after type erasure.
   */
  @FunctionalInterface
  public static interface String<T extends java.lang.String> extends Iterable<T> {
  }

  /**
   * A set of various {@link Iterable} over common Java types from the <code>java.awt</code> package,
   * useful to avoid conflict after type erasure.
   */
  public static final class awt {

    /** Make no instances. */
    private awt() {
      throw new AssertionError();
    }

    /**
     * An {@link Iterable} over {@link java.awt.Polygon},
     * useful to avoid conflict after type erasure.
     */
    @FunctionalInterface
    public static interface Polygon<T extends java.awt.Polygon> extends Iterable<T> {
    }

    /**
     * An {@link Iterable} over {@link java.awt.Rectangle},
     * useful to avoid conflict after type erasure.
     */
    @FunctionalInterface
    public static interface Rectangle<T extends java.awt.Rectangle> extends Iterable<T> {
    }

    /**
     * An {@link Iterable} over {@link java.awt.Shape},
     * useful to avoid conflict after type erasure.
     */
    @FunctionalInterface
    public static interface Shape<T extends java.awt.Shape> extends Iterable<T> {
    }
  }

  /**
   * A set of various {@link Iterable} over common Java types from the <code>java.util</code> package,
   * useful to avoid conflict after type erasure.
   */
  public static final class util {

    /** Make no instances. */
    private util() {
      throw new AssertionError();
    }

    /**
     * An {@link Iterable} over {@link java.util.Currency},
     * useful to avoid conflict after type erasure.
     */
    @FunctionalInterface
    public static interface Currency<T extends java.util.Currency> extends Iterable<T> {
    }

    /**
     * An {@link Iterable} over {@link java.util.Locale},
     * useful to avoid conflict after type erasure.
     */
    @FunctionalInterface
    public static interface Locale<T extends java.util.Locale> extends Iterable<T> {
    }
  }
}
