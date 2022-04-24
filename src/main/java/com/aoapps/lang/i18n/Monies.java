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

package com.aoapps.lang.i18n;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Stores multiple monetary values, with one value per currency.
 *
 * @author  AO Industries, Inc.
 */
public class Monies implements Comparable<Monies>, Iterable<Money> {

  private static void add(Map<Currency, Money> monies, Money addend) {
    Currency currency = addend.getCurrency();
    Money total = monies.get(currency);
    total = (total == null) ? addend : total.add(addend);
    monies.put(currency, total);
  }

  private static void subtract(Map<Currency, Money> monies, Money subtrahend) {
    Currency currency = subtrahend.getCurrency();
    Money total = monies.get(currency);
    total = (total == null) ? subtrahend : total.subtract(subtrahend);
    monies.put(currency, total);
  }

  private static final Monies EMPTY_MONIES = new Monies(Collections.<Currency, Money>emptyMap());

  public static Monies of() {
    return EMPTY_MONIES;
  }

  public static Monies of(Money money) {
    if (money == null) {
      return of();
    }
    return new Monies(Collections.singletonMap(money.getCurrency(), money));
  }

  private static Monies of(SortedMap<Currency, Money> newMap) {
    int size = newMap.size();
    if (size == 0) {
      return of();
    }
    if (size == 1) {
      return of(newMap.values().iterator().next());
    }
    return new Monies(Collections.unmodifiableMap(newMap));
  }

  /**
   * Combines all the provided the money, adding together any that have the same currency.
   */
  public static Monies of(Money ... monies) {
    if (monies == null || monies.length == 0) {
      return of();
    }
    if (monies.length == 1) {
      return of(monies[0]);
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money m : monies) {
      if (m != null) {
        add(newMap, m);
      }
    }
    return of(newMap);
  }

  /**
   * Combines all the provided the money, adding together any that have the same currency.
   */
  public static Monies of(Iterable<Money> monies) {
    if (monies == null) {
      return of();
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money m : monies) {
      if (m != null) {
        add(newMap, m);
      }
    }
    return of(newMap);
  }

  private final Map<Currency, Money> monies;

  private Monies(Map<Currency, Money> monies) {
    this.monies = monies;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Monies)) {
      return false;
    }
    Monies other = (Monies) o;
    return monies.equals(other.monies);
  }

  @Override
  public int hashCode() {
    return monies.hashCode();
  }

  private static int compare(Money m1, Money m2) {
    if (m1 == null) {
      if (m2 == null) {
        return 0;
      } else {
        return Long.compare(0, m2.getUnscaledValue());
      }
    } else {
      if (m2 == null) {
        return Long.compare(m1.getUnscaledValue(), 0);
      } else {
        return m1.compareTo(m2);
      }
    }
  }

  /**
   * Compares two {@link Monies} by comparing each value matched by {@link Currency}.
   * During comparison, any currency not set is handled as zero.
   * <p>
   * Two {@link Monies} are not comparable when they have a conflict where one currency is higher
   * and a different currency is lower.
   * </p>
   */
  @Override
  public int compareTo(Monies o) {
    // Find the set of all currencies
    Set<Currency> currencies = new HashSet<>();
    currencies.addAll(monies.keySet());
    currencies.addAll(o.monies.keySet());
    boolean isLessThan = false;
    boolean isMoreThan = false;
    for (Currency currency : currencies) {
      int diff = compare(monies.get(currency), o.monies.get(currency));
      if (diff < 0) {
        if (isMoreThan) {
          throw new IllegalArgumentException("Incomparable monies, both less-than and greater-than: " + this + " and " + o);
        }
        isLessThan = true;
      } else if (diff > 0) {
        if (isLessThan) {
          throw new IllegalArgumentException("Incomparable monies, both less-than and greater-than: " + this + " and " + o);
        }
        isMoreThan = true;
      }
    }
    if (isLessThan) {
      return -1;
    }
    if (isMoreThan) {
      return 1;
    }
    return 0;
  }

  @Override
  public Iterator<Money> iterator() {
    return getValues().iterator();
  }

  public Money get(Currency currency) {
    return monies.get(currency);
  }

  public Map<Currency, Money> getMap() {
    return monies;
  }

  public boolean isEmpty() {
    return monies.isEmpty();
  }

  public int size() {
    return monies.size();
  }

  public Set<Currency> getCurrencies() {
    return monies.keySet();
  }

  public Collection<Money> getValues() {
    return monies.values();
  }

  /**
   * Displays as a comma-separated list of {@link Money#toString()}
   * or {@code ""} for an empty set.
   *
   * @see  Money#toString()
   */
  @Override
  public String toString() {
    int size = monies.size();
    if (size == 0) {
      return "";
    }
    if (size == 1) {
      return monies.values().iterator().next().toString();
    }
    StringBuilder sb = new StringBuilder();
    for (Money money : monies.values()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(money);
    }
    return sb.toString();
  }

  /**
   * @see  Money#add(com.aoapps.lang.i18n.Money)
   */
  public Monies add(Money addend) throws ArithmeticException {
    if (addend == null) {
      return this;
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    newMap.putAll(monies);
    add(newMap, addend);
    return of(newMap);
  }

  /**
   * @see  Money#add(com.aoapps.lang.i18n.Money)
   */
  public Monies add(Monies addend) throws ArithmeticException {
    if (addend == null) {
      return this;
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    newMap.putAll(monies);
    for (Money money : addend) {
      add(newMap, money);
    }
    return of(newMap);
  }

  /**
   * Multiplies without rounding.
   *
   * @see  Money#multiply(java.math.BigDecimal)
   */
  public Monies multiply(BigDecimal multiplicand) throws ArithmeticException {
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money money : monies.values()) {
      Currency currency = money.getCurrency();
      newMap.put(currency, money.multiply(multiplicand));
    }
    return of(newMap);
  }

  /**
   * Multiplies with rounding.
   *
   * @see  Money#multiply(java.math.BigDecimal, java.math.RoundingMode)
   */
  public Monies multiply(BigDecimal multiplicand, RoundingMode roundingMode) throws ArithmeticException {
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money money : monies.values()) {
      Currency currency = money.getCurrency();
      newMap.put(currency, money.multiply(multiplicand, roundingMode));
    }
    return of(newMap);
  }

  /**
   * Returns with monetary amounts that are the negative of these amounts.
   *
   * @see  Money#negate()
   */
  public Monies negate() {
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money money : monies.values()) {
      Currency currency = money.getCurrency();
      newMap.put(currency, money.negate());
    }
    return of(newMap);
  }

  /**
   * @see  Money#subtract(com.aoapps.lang.i18n.Money)
   */
  public Monies subtract(Money subtrahend) throws ArithmeticException {
    if (subtrahend == null) {
      return this;
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    newMap.putAll(monies);
    subtract(newMap, subtrahend);
    return of(newMap);
  }

  /**
   * @see  Money#subtract(com.aoapps.lang.i18n.Money)
   */
  public Monies subtract(Monies subtrahend) throws ArithmeticException {
    if (subtrahend == null) {
      return this;
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    newMap.putAll(monies);
    for (Money money : subtrahend) {
      subtract(newMap, money);
    }
    return of(newMap);
  }

  /**
   * Check if this is empty or all values are zero.
   */
  public boolean isZero() {
    for (Money money : monies.values()) {
      if (money.getUnscaledValue() != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Removes all currencies with a zero monetary value.
   */
  public Monies removeZeros() {
    boolean hasZero = false;
    for (Money money : monies.values()) {
      if (money.getUnscaledValue() == 0) {
        hasZero = true;
        break;
      }
    }
    if (!hasZero) {
      return this;
    }
    SortedMap<Currency, Money> newMap = new TreeMap<>(CurrencyComparator.getInstance());
    for (Money money : monies.values()) {
      if (money.getUnscaledValue() != 0) {
        newMap.put(money.getCurrency(), money);
      }
    }
    return of(newMap);
  }
}
