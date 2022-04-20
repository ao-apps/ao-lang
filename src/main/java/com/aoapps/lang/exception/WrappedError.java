/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.exception;

import com.aoapps.lang.Throwables;

/**
 * <p>
 * A wrapped error may be used to rethrow any throwable in a context where {@link Error} is expected.  As this is not
 * often the case, you probably want to use {@link WrappedException} to wrap checked exceptions.
 * </p>
 * <p>
 * This could be accomplished by
 * rethrowing with {@link Error} directly, but having this distinct
 * class provides more meaning as well as the ability to catch wrapped
 * errors while letting all other errors go through directly.
 * </p>
 * <p>
 * Catching {@link WrappedError} may be used to unwrap expected throwable types.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class WrappedError extends Error implements ExtraInfo {

  private static final long serialVersionUID = 1L;

  private final Object[] extraInfo;

  /**
   * Uses extra info of the original cause when it is an {@link ExtraInfo}.
   */
  public WrappedError(Throwable cause) {
    super(cause);
    this.extraInfo = (cause instanceof ExtraInfo) ? ((ExtraInfo)cause).getExtraInfo() : null;
  }

  /**
   * @param  extraInfo No defensive copy
   */
  public WrappedError(Throwable cause, Object... extraInfo) {
    super(cause);
    this.extraInfo = extraInfo;
  }

  /**
   * Uses extra info of the original cause when it is an {@link ExtraInfo}.
   */
  public WrappedError(String message, Throwable cause) {
    super(message, cause);
    this.extraInfo = (cause instanceof ExtraInfo) ? ((ExtraInfo)cause).getExtraInfo() : null;
  }

  /**
   * @param  extraInfo No defensive copy
   */
  public WrappedError(String message, Throwable cause, Object... extraInfo) {
    super(message, cause);
    this.extraInfo = extraInfo;
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

  /**
   * @return  No defensive copy
   */
  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField")
  public Object[] getExtraInfo() {
    return extraInfo;
  }

  static {
    Throwables.registerSurrogateFactory(WrappedError.class, (template, cause) ->
      new WrappedError(template.getMessage(), cause, template.extraInfo)
    );
  }
}
