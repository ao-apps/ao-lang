/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2013, 2016, 2017, 2020, 2021, 2022, 2023  AO Industries, Inc.
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

import java.io.IOException;
import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  AO Industries, Inc.
 */
public class LocalesTest extends TestCase {

  public LocalesTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(LocalesTest.class);
  }

  public void testParseLocale() throws IOException {
    for (Locale locale : Locale.getAvailableLocales()) {
      // Ignore locales with script or extensions for preload, since the rest of this API is unaware of them
      if (
          locale.getScript().isEmpty()
              && locale.getExtensionKeys().isEmpty()
      ) {
        Locale parsed = Locales.parseLocale(locale.toString());
        //System.out.println(locale+"→"+parsed);
        assertEquals(locale, parsed);
      }
    }
  }
}
