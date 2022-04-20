/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.math;

import java.util.Collection;

/**
 * Some basic statistics algorithms.
 *
 * @author  AO Industries, Inc.
 */
public final class Statistics {

  /** Make no instances. */
  private Statistics() {
    throw new AssertionError();
  }

  /**
   * Computes the average of a set of samples.  <code>null</code> values will be
   * ignored, not contributing to the average in any way.
   *
   * @param samples must have at least one sample
   *
   * @return  Double.NaN if there are no samples
   */
  public static double mean(Collection<? extends Number> samples) {
    double sum = 0;
    int numSamples = 0;
    for (Number sample : samples) {
      if (sample != null) {
        sum += sample.doubleValue();
        numSamples++;
      }
    }
    return sum / numSamples;
  }

  /**
   * Computes the standard deviation of a set of samples.  <code>null</code> values will be
   * ignored, not contributing to the deviation in any way.  This does not use Bessel's correction.
   */
  public static double standardDeviation(double mean, Collection<? extends Number> samples) {
    double sum = 0;
    int numSamples = 0;
    for (Number sample : samples) {
      if (sample != null) {
        double diff = sample.doubleValue()-mean;
        sum += diff * diff;
        numSamples++;
      }
    }
    return Math.sqrt(sum/numSamples);
  }
}
