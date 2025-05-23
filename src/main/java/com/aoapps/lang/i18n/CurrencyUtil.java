/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2019, 2021, 2022, 2024  AO Industries, Inc.
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

import java.util.Currency;
import java.util.Locale;

/**
 * Utility methods for working with currencies.
 *
 * @author  AO Industries, Inc.
 */
public final class CurrencyUtil {

  /** Make no instances. */
  private CurrencyUtil() {
    throw new AssertionError();
  }

  /**
   * Gets a symbol for a currency in the provided locale, with some extra hard-coded value when the Java
   * runtime returns the {@link Currency#getCurrencyCode() currencyCode} as the symbol.
   * {@link Currency#getSymbol(java.util.Locale)} does not seem to be as fully implemented as desired.
   *
   * @see  Currency#getSymbol(java.util.Locale)
   */
  public static String getSymbol(Currency currency, Locale locale) {
    String symbol = currency.getSymbol(locale);
    String currencyCode = currency.getCurrencyCode();
    if (symbol.equals(currencyCode)) {
      switch (currencyCode) {
        case "USD":
          return "$";
        case "EUR":
          return "€";
        case "JPY":
          return "¥";
        case "CAD":
          {
            // https://www.btb.termiumplus.gc.ca/tpv2guides/guides/wrtps/index-eng.html?lang=eng&lettr=indx_catlog_c&page=9Rl-N63dyxbA.html
            if (Locale.FRENCH.getLanguage().equals(locale.getLanguage())) {
              return "$\u00A0CA"; // non-breaking space
            } else {
              return "Can$";
            }
          }
        default:
          // fall-through
      }
    }
    return symbol;
  }

  /**
   * Gets a symbol for a currency in the current thread locale, with some extra hard-coded value when the Java
   * runtime returns the currencyCode as the symbol.
   * {@link Currency#getSymbol(java.util.Locale)} does not seem to be as fully implemented as desired.
   *
   * @see  Currency#getSymbol(java.util.Locale)
   * @see  ThreadLocale
   */
  public static String getSymbol(Currency currency) {
    return getSymbol(currency, ThreadLocale.get());
  }
}
