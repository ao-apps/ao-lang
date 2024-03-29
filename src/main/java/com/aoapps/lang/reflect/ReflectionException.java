/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.reflect;

import com.aoapps.lang.Throwables;

/**
 * @author  AO Industries, Inc.
 */
// TODO: Deprecate in favor of ReflectiveOperationException, which is a checked exception?
public class ReflectionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ReflectionException(Throwable cause) {
    super(cause);
  }

  protected ReflectionException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    String message = super.getMessage();
    if (message != null) {
      return message;
    }
    Throwable cause = getCause();
    return (cause == null) ? null : cause.getMessage();
  }

  @Override
  public String getLocalizedMessage() {
    String message = super.getMessage();
    if (message != null) {
      return message;
    }
    Throwable cause = getCause();
    return (cause == null) ? null : cause.getLocalizedMessage();
  }

  static {
    Throwables.registerSurrogateFactory(ReflectionException.class, (template, cause) ->
        new ReflectionException(template.getMessage(), cause)
    );
  }
}
