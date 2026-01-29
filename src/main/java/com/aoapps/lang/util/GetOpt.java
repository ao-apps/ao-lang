/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2013, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.util;

import com.aoapps.lang.Throwables;
import com.aoapps.lang.exception.WrappedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * General-purpose command line argument processor.  Supports options in the
 * "--option=value" format.  <code>boolean</code> options default to true if no
 * value provided.  Also supports a single "--" that is used to separate
 * parameters from non-parameters.  All arguments start with -- before the "--"
 * must be valid parameters.
 *
 * @author  AO Industries, Inc.
 */
public final class GetOpt {

  /** Make no instances. */
  private GetOpt() {
    throw new AssertionError();
  }

  /**
   * Parses a String value to the provided type.
   * Values are converted by:
   * <ol>
   *   <li>Special handling for primitive types</li>
   *   <li><code>public static valueOf(String)</code> method</li>
   *   <li>public constructor with single <code>String</code> parameter</li>
   * </ol>
   */
  @SuppressWarnings("unchecked")
  public static <T> T parse(String value, Class<T> type) {
    if (value == null) {
      throw new IllegalArgumentException("value == null");
    } else if (type == String.class) {
      return (T) value;
      // Special handling for primitive types
    } else if (type == Integer.TYPE || type == Integer.class) {
      return (T) Integer.valueOf(value);
    } else if (type == Long.TYPE || type == Long.class) {
      return (T) Long.valueOf(value);
    } else if (type == Short.TYPE || type == Short.class) {
      return (T) Short.valueOf(value);
    } else if (type == Byte.TYPE || type == Byte.class) {
      return (T) Byte.valueOf(value);
    } else if (type == Float.TYPE || type == Float.class) {
      return (T) Float.valueOf(value);
    } else if (type == Double.TYPE || type == Double.class) {
      return (T) Double.valueOf(value);
    } else if (type == Boolean.TYPE || type == Boolean.class) {
      return (T) Boolean.valueOf(value);
    } else if (type == Character.TYPE || type == Character.class) {
      int len = value.length();
      if (value.length() != 1) {
        throw new IllegalArgumentException("value.length != 1: " + len);
      }
      return (T) (Character) value.charAt(0);
    } else {
      // public static valueOf(String) method
      try {
        Method method = type.getMethod("valueOf", String.class);
        int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
          T result = (T) method.invoke(null, value);
          if (result == null) {
            throw new AssertionError("result == null");
          }
          return result;
        }
      } catch (InvocationTargetException e) {
        // Unwrap cause for more direct stack traces
        Throwable cause = e.getCause();
        throw Throwables.wrap((cause == null) ? e : cause, WrappedException.class, WrappedException::new);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        // fall-through to try constructor
      }
      // public constructor with single String parameter
      try {
        return type.getConstructor(String.class).newInstance(value);
      } catch (InvocationTargetException e) {
        // Unwrap cause for more direct stack traces
        Throwable cause = e.getCause();
        throw Throwables.wrap((cause == null) ? e : cause, WrappedException.class, WrappedException::new);
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  /**
   * Gets an argument of the provided type.  If the argument is found
   * multiple times, only the most recent value is used.
   * The argument should be in the form --<i>name</i >= <i>value</i>.
   * Boolean arguments are unique in that the value is optional, when not
   * provided it defaults to <code>true</code>.
   * "--" may be used to separate parameters from arguments.
   *
   * @param args  the values used during the processing of the parameters will be set to null
   *
   * @return  the converted value or <code>null</code> if not found
   *
   * @see GetOpt#parse(String, Class)
   */
  @SuppressWarnings("unchecked")
  public static <T> T getOpt(String[] args, String name, Class<T> type) {
    final String booleanPrefix = "--" + name;
    final String paramPrefix = booleanPrefix + '=';
    T value = null;
    for (int c = 0; c < args.length; c++) {
      String arg = args[c];
      if (arg != null) {
        if ("--".equals(arg)) {
          break;
        } else if (arg.startsWith(paramPrefix)) {
          value = parse(arg.substring(paramPrefix.length()), type);
          args[c] = null;
        } else if ((type == Boolean.TYPE || type == Boolean.class) && arg.equals(booleanPrefix)) {
          value = (T) Boolean.TRUE;
          args[c] = null;
        }
      }
    }
    return value;
  }

  /**
   * Gets all of the non-parameter arguments.  If any arguments start with
   * "--" and are not a valid parameter, throws IllegalArgumentException.
   * "--" may be used to separate parameters from arguments.
   * The "--" will be included in the arguments returned.
   */
  public static List<String> getArguments(String[] args) {
    List<String> arguments = new ArrayList<>(args.length);
    int c = 0;
    for (; c < args.length; c++) {
      String arg = args[c];
      if (arg != null) {
        if (arg.startsWith("--")) {
          if ("--".equals(arg)) {
            break;
          }
          throw new IllegalArgumentException("Unexpected parameter: " + arg);
        }
        arguments.add(arg);
      }
    }
    for (; c < args.length; c++) {
      String arg = args[c];
      if (arg != null) {
        arguments.add(arg);
      }
    }
    return arguments;
  }
}
