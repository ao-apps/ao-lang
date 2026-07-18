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

package com.aoapps.lang.util;

import com.aoapps.lang.function.ConsumerE;
import com.aoapps.lang.function.FunctionE;
import com.aoapps.lang.function.PredicateE;
import com.aoapps.lang.function.SupplierE;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An optional that is allowed to throw a checked exception.
 * This should not be used as a return type, use standard {@link Optional} instead.
 * This is useful for specific scenarios where the methods of standard {@link Optional} do not play well with
 * checked exceptions.
 *
 * @see Optional
 */
public final class OptionalE<T> {

  private static final OptionalE<?> EMPTY = new OptionalE<>(null);

  private final T value;

  /**
   * @see Optional#empty()
   */
  public static <T> OptionalE<T> empty() {
    @SuppressWarnings("unchecked")
    OptionalE<T> t = (OptionalE<T>) EMPTY;
    return t;
  }

  private OptionalE(T value) {
    this.value = value;
  }

  /**
   * @see Optional#of(java.lang.Object)
   */
  public static <T> OptionalE<T> of(T value) {
    return new OptionalE<>(Objects.requireNonNull(value));
  }

  /**
   * @see Optional#ofNullable(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  public static <T> OptionalE<T> ofNullable(T value) {
    return value == null ? (OptionalE<T>) EMPTY : new OptionalE<>(value);
  }

  /**
   * Convert from standard {@link Optional}.
   */
  public static <T> OptionalE<T> fromOptional(Optional<? extends T> value) {
    return ofNullable(Objects.requireNonNull(value).orElse(null));
  }

  /**
   * Convert to standard {@link Optional}.
   */
  public Optional<T> toOptional() {
    return Optional.ofNullable(value);
  }

  /**
   * @see Optional#get()
   */
  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /**
   * @see Optional#isPresent()
   */
  public boolean isPresent() {
    return value != null;
  }

  /**
   * @see Optional#isEmpty()
   */
  public boolean isEmpty() {
    return value == null;
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#ifPresent(java.util.function.Consumer)
   */
  public <Ex extends Throwable> void ifPresent(ConsumerE<? super T, Ex> action) throws Ex {
    if (value != null) {
      action.accept(value);
    }
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#ifPresentOrElse(java.util.function.Consumer, java.lang.Runnable)
   */
  public <Ex extends Throwable> void ifPresentOrElse(ConsumerE<? super T, Ex> action, Runnable emptyAction) throws Ex {
    if (value != null) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#filter(java.util.function.Predicate)
   */
  public <Ex extends Throwable> OptionalE<T> filter(PredicateE<? super T, Ex> predicate) throws Ex {
    Objects.requireNonNull(predicate);
    if (isEmpty()) {
      return this;
    } else {
      return predicate.test(value) ? this : empty();
    }
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#map(java.util.function.Function)
   */
  public <U, Ex extends Throwable> OptionalE<U> map(FunctionE<? super T, ? extends U, Ex> mapper) throws Ex {
    Objects.requireNonNull(mapper);
    if (isEmpty()) {
      return empty();
    } else {
      return OptionalE.ofNullable(mapper.apply(value));
    }
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#flatMap(java.util.function.Function)
   */
  public <U, Ex extends Throwable> OptionalE<U> flatMap(FunctionE<? super T, ? extends Optional<? extends U>, Ex> mapper) throws Ex {
    Objects.requireNonNull(mapper);
    if (isEmpty()) {
      return empty();
    } else {
      return OptionalE.fromOptional(Objects.requireNonNull(mapper.apply(value)));
    }
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#or(java.util.function.Supplier)
   */
  public <Ex extends Throwable> OptionalE<T> or(SupplierE<? extends Optional<? extends T>, Ex> supplier) throws Ex {
    Objects.requireNonNull(supplier);
    if (isPresent()) {
      return this;
    } else {
      return OptionalE.fromOptional(Objects.requireNonNull(supplier.get()));
    }
  }

  /**
   * @see Optional#stream()
   */
  public Stream<T> stream() {
    return isEmpty() ? Stream.empty() : Stream.of(value);
  }

  /**
   * @see Optional#orElse(java.lang.Object)
   */
  public T orElse(T other) {
    return value != null ? value : other;
  }

  /**
   * @param  <Ex>  An arbitrary exception type that may be thrown
   *
   * @see Optional#orElseGet(java.util.function.Supplier)
   */
  public <Ex extends Throwable> T orElseGet(SupplierE<? extends T, Ex> supplier) throws Ex {
    return value != null ? value : supplier.get();
  }

  /**
   * @see Optional#orElseThrow()
   */
  public T orElseThrow() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  /**
   * @see Optional#orElseThrow(java.util.function.Supplier)
   */
  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  /**
   * @see Optional#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof OptionalE<?>) {
      return Objects.equals(value, ((OptionalE<?>) obj).value);
    } else {
      return false;
    }
  }

  /**
   * @see Optional#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  /**
   * @see Optional#toString()
   */
  @Override
  public String toString() {
    return value != null ? ("OptionalE[" + value + "]") : "OptionalE.empty";
  }
}
