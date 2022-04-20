/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011-2013, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.validation;

import com.aoapps.lang.i18n.Resources;
import java.util.ResourceBundle;

/**
 * A valid result singleton.
 *
 * @author  AO Industries, Inc.
 */
public final class ValidResult implements ValidationResult {

  private static final Resources RESOURCES = Resources.getResources(ResourceBundle::getBundle, ValidResult.class);

  private static final long serialVersionUID = -5742207860354792003L;

  private static final ValidResult singleton = new ValidResult();

  public static ValidResult getInstance() {
    return singleton;
  }

  private ValidResult() {
    // Do nothing
  }

  private Object readResolve() {
    return singleton;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public String toString() {
    return RESOURCES.getMessage("toString");
  }
}
