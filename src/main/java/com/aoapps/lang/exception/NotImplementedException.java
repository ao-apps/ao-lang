/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2016, 2017, 2018, 2020, 2021, 2022  AO Industries, Inc.
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
 * Indicates some part of the code has not been implemented yet.
 *
 * @author  AO Industries, Inc.
 *
 * @deprecated  Please use {@link org.apache.commons.lang3.NotImplementedException} from
 *              <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>.
 */
@Deprecated
public class NotImplementedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotImplementedException() {
    super();
  }

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(Throwable cause) {
    super(cause);
  }

  public NotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }

  static {
    Throwables.registerSurrogateFactory(NotImplementedException.class, (template, cause) ->
      new NotImplementedException(template.getMessage(), cause)
    );
  }
}
