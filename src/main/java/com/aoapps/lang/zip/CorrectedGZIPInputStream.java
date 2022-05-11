/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Works around the "Corrupt GZIP trailer" problem in <code>GZIPInputStream</code> by catching and ignoring this exception.
 *
 * @author  AO Industries, Inc.
 */
public class CorrectedGZIPInputStream extends GZIPInputStream {

  public CorrectedGZIPInputStream(InputStream in) throws IOException {
    super(in);
  }

  public CorrectedGZIPInputStream(InputStream in, int size) throws IOException {
    super(in, size);
  }

  private static class FoundErrorLock {
    // Empty lock class to help heap profile
  }

  private final FoundErrorLock foundErrorLock = new FoundErrorLock();
  private boolean foundError;

  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    synchronized (foundErrorLock) {
      if (foundError) {
        return -1;
      }
      try {
        return super.read(buf, off, len);
      } catch (IOException err) {
        String message = err.getMessage();
        if (message.contains("Corrupt GZIP trailer")) {
          foundError = true;
          return -1;
        } else {
          throw err;
        }
      }
    }
  }
}
