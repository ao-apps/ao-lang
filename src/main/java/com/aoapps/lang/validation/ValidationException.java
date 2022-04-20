/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010-2013, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.LocalizedIllegalArgumentException;
import com.aoapps.lang.Throwables;
import com.aoapps.lang.i18n.Resources;
import java.util.ResourceBundle;

/**
 * Thrown when internal object validation fails.
 *
 * @author  AO Industries, Inc.
 */
// TODO: Extend IllegalArgumentException?  Would be major-version increment.
// TODO: Or extend javax.validation.ValidationException and deprecate this.
public class ValidationException extends Exception {

  private static final Resources RESOURCES = Resources.getResources(ResourceBundle::getBundle, ValidationException.class);

  private static final long serialVersionUID = -1153407618428602416L;

  private final ValidationResult result;

  public ValidationException(ValidationResult result) {
    super(result.toString()); // Conversion done in server
    if (result.isValid()) {
      throw new LocalizedIllegalArgumentException(RESOURCES, "init.validResult");
    }
    this.result = result;
  }

  public ValidationException(Throwable cause, ValidationResult result) {
    super(result.toString(), cause); // Conversion done in server
    if (result.isValid()) {
      throw new LocalizedIllegalArgumentException(RESOURCES, "init.validResult");
    }
    this.result = result;
  }

  @Override
  public String getLocalizedMessage() {
    return result.toString(); // Conversion done in client
  }

  public ValidationResult getResult() {
    return result;
  }

  static {
    Throwables.registerSurrogateFactory(ValidationException.class, (template, cause) ->
      new ValidationException(cause, template.result)
    );
  }
}
