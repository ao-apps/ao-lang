/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoapps.lang.time;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Wraps the number of seconds from the Epoch as well as positive nanoseconds into an
 * immutable value type.
 *
 * <p>This will be deprecated once Java 8 is ubiquitous and only serves as an extremely
 * simplified stop-gap.</p>
 *
 * @author  AO Industries, Inc.
 *
 * @deprecated  Please use standard Java 8 classes.
 */
@Deprecated
public class Instant implements Comparable<Instant>, Serializable {

  static final int NANOS_PER_SECOND = 1000000000;

  public static final Instant EPOCH = new Instant(0, 0);

  /**
   * Parses an Instant's string representation.
   *
   * @return Instant the instant or null when toString is null
   *
   * @throws IllegalArgumentException when unable to parse
   */
  public static Instant valueOf(String s) {
    if (s == null) {
      return null;
    }
    int dotPos = s.indexOf('.');
    if (dotPos == -1) {
      throw new IllegalArgumentException("Period (.) not found: " + s);
    }
    return new Instant(
        Long.parseLong(s.substring(0, dotPos)),
        Integer.parseInt(s.substring(dotPos + 1))
    );
  }

  private static final long serialVersionUID = 2L;

  final long epochSecond;
  final int nano;

  public Instant(long epochSecond, int nano) {
    this.epochSecond = epochSecond;
    this.nano = nano;
    validate();
  }

  private void validate() throws IllegalArgumentException {
    if (nano < 0 || nano >= NANOS_PER_SECOND) {
      throw new IllegalArgumentException("nanoseconds out of range 0-" + (NANOS_PER_SECOND - 1));
    }
  }

  /**
   * Perform same validation as constructor on readObject.
   */
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    try {
      validate();
    } catch (IllegalArgumentException err) {
      InvalidObjectException newErr = new InvalidObjectException(err.getMessage());
      newErr.initCause(err);
      throw newErr;
    }
  }

  protected Object readResolve() {
    if (epochSecond == 0 && nano == 0) {
      return EPOCH;
    }
    return this;
  }

  static String toString(long epochSecond, int nano) {
    StringBuilder sb = new StringBuilder(
        20 // Length of "-9223372036854775808"
            + 1 // "."
            + 9 // Nanoseconds
    );
    sb.append(epochSecond).append('.');
    if (nano < 100000000) {
      sb.append('0');
      if (nano < 10000000) {
        sb.append('0');
        if (nano < 1000000) {
          sb.append('0');
          if (nano < 100000) {
            sb.append('0');
            if (nano < 10000) {
              sb.append('0');
              if (nano < 1000) {
                sb.append('0');
                if (nano < 100) {
                  sb.append('0');
                  if (nano < 10) {
                    sb.append('0');
                  }
                }
              }
            }
          }
        }
      }
    }
    sb.append(nano);
    return sb.toString();
  }

  @Override
  public String toString() {
    return toString(epochSecond, nano);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Instant)) {
      return false;
    }
    return equals((Instant) obj);
  }

  public boolean equals(Instant other) {
    return
        other != null
            && epochSecond == other.epochSecond
            && nano == other.nano;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(epochSecond) ^ nano;
  }

  @Override
  public int compareTo(Instant other) {
    if (epochSecond < other.epochSecond) {
      return -1;
    }
    if (epochSecond > other.epochSecond) {
      return 1;
    }
    if (nano < other.nano) {
      return -1;
    }
    if (nano > other.nano) {
      return 1;
    }
    return 0;
  }

  public long getEpochSecond() {
    return epochSecond;
  }

  /**
   * The nanoseconds, to simplify this is always in the positive direction.
   * For negative instants, this means the nanos goes up from zero to 1 billion,
   * then the seconds go up one (toward zero).  This may be counterintuitive if
   * one things of nanoseconds as a fractional part of seconds, but this definition
   * leads to a very clean implementation.
   *
   * <p>Counting up by nanoseconds:</p>
   *
   * <ol>
   *   <li>-1.999999998</li>
   *   <li>-1.999999999</li>
   *   <li>0.000000000</li>
   *   <li>0.000000001</li>
   *   <li>0.000000002</li>
   * </ol>
   */
  public int getNano() {
    return nano;
  }

  public Instant plusNanos(long nanosToAdd) {
    if (nanosToAdd == 0) {
      return this;
    }
    long newSeconds = this.epochSecond + nanosToAdd / NANOS_PER_SECOND;
    int newNanos = this.nano + (int) (nanosToAdd % NANOS_PER_SECOND);
    if (newNanos >= NANOS_PER_SECOND) {
      newSeconds++;
      newNanos -= NANOS_PER_SECOND;
    } else if (newNanos < 0) {
      newSeconds--;
      newNanos += NANOS_PER_SECOND;
    }
    return new Instant(newSeconds, newNanos);
  }
}
