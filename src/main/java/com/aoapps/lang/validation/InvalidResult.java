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

import com.aoapps.lang.EmptyArrays;
import com.aoapps.lang.i18n.Resources;
import java.io.Serializable;

/**
 * An invalid result with a user-friendly message.
 *
 * @author  AO Industries, Inc.
 */
public final class InvalidResult implements ValidationResult {

  private static final long serialVersionUID = -105878200149461063L;

  private final Resources resources;
  private final String key;
  private final Serializable[] args;

  public InvalidResult(Resources resources, String key) {
    this.resources = resources;
    this.key = key;
    this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
  }

  /**
   * @param  args  No defensive copy
   */
  public InvalidResult(Resources resources, String key, Serializable... args) {
    this.resources = resources;
    this.key = key;
    this.args = args;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public String toString() {
    return resources.getMessage(key, (Object[]) args);
  }

  public Resources getResources() {
    return resources;
  }

  public String getKey() {
    return key;
  }

  /**
   * @return  No defensive copy.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField")
  public Serializable[] getArgs() {
    return args;
  }
}
