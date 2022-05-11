/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.sql;

import com.aoapps.lang.EmptyArrays;
import com.aoapps.lang.Throwables;
import com.aoapps.lang.exception.LocalizedException;
import com.aoapps.lang.i18n.Resources;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Extends <code>SQLException</code> to provide exceptions with user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedSQLException extends SQLException implements LocalizedException {

  private static final long serialVersionUID = 3L;

  protected final Resources resources;
  protected final String key;
  protected final Serializable[] args;

  public LocalizedSQLException(String sqlState, int vendorCode, Resources resources, String key) {
    super(resources.getMessage(key), sqlState, vendorCode);
    this.resources = resources;
    this.key = key;
    this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
  }

  public LocalizedSQLException(String sqlState, int vendorCode, Resources resources, String key, Serializable... args) {
    super(resources.getMessage(key, (Object[]) args), sqlState, vendorCode);
    this.resources = resources;
    this.key = key;
    this.args = args;
  }

  public LocalizedSQLException(String sqlState, Resources resources, String key) {
    super(resources.getMessage(key), sqlState);
    this.resources = resources;
    this.key = key;
    this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
  }

  public LocalizedSQLException(String sqlState, Resources resources, String key, Serializable... args) {
    super(resources.getMessage(key, (Object[]) args), sqlState);
    this.resources = resources;
    this.key = key;
    this.args = args;
  }

  public LocalizedSQLException(String sqlState, Throwable cause, Resources resources, String key) {
    super(resources.getMessage(key), sqlState, cause);
    this.resources = resources;
    this.key = key;
    this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
  }

  public LocalizedSQLException(String sqlState, Throwable cause, Resources resources, String key, Serializable... args) {
    super(resources.getMessage(key, (Object[]) args), sqlState, cause);
    this.resources = resources;
    this.key = key;
    this.args = args;
  }

  public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, Resources resources, String key) {
    super(resources.getMessage(key), sqlState, vendorCode, cause);
    this.resources = resources;
    this.key = key;
    this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
  }

  public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, Resources resources, String key, Serializable... args) {
    super(resources.getMessage(key, (Object[]) args), sqlState, vendorCode, cause);
    this.resources = resources;
    this.key = key;
    this.args = args;
  }

  @Override
  public String getLocalizedMessage() {
    return resources.getMessage(key, (Object[]) args);
  }

  @Override
  public final Resources getResources() {
    return resources;
  }

  @Override
  public final String getKey() {
    return key;
  }

  /**
   * {@inheritDoc}
   *
   * @return  No defensive copy
   */
  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField")
  public final Serializable[] getArgs() {
    return args;
  }

  static {
    Throwables.registerSurrogateFactory(LocalizedSQLException.class, (template, cause) ->
        new LocalizedSQLException(
            template.getSQLState(),
            template.getErrorCode(),
            cause,
            template.resources,
            template.key,
            template.args
        )
    );
  }
}
