/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.io;

import java.io.PrintWriter;
import java.util.Locale;

/**
 * Discards all data.
 */
public final class NullPrintWriter extends PrintWriter implements NoClose {

  private static final NullPrintWriter instance = new NullPrintWriter();

  public static NullPrintWriter getInstance() {
    return instance;
  }

  private NullPrintWriter() {
    super(NullWriter.getInstance());
  }

  @Override
  public void write(int c) {
    // Do nothing
  }

  @Override
  public void write(char[] cbuf) {
    // Do nothing
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    // Do nothing
  }

  @Override
  public void write(String str) {
    // Do nothing
  }

  @Override
  public void write(String str, int off, int len) {
    // Do nothing
  }

  @Override
  public NullPrintWriter append(CharSequence csq) {
    return this;
  }

  @Override
  public NullPrintWriter append(CharSequence csq, int start, int end) {
    return this;
  }

  @Override
  public NullPrintWriter append(char c) {
    return this;
  }

  @Override
  public void flush() {
    // Do nothing
  }

  @Override
  public void close() {
    // Do nothing
  }

  @Override
  public boolean checkError() {
    return false;
  }

  @Override
  protected void setError() {
    throw new AssertionError("setError should never be called on NullPrintWriter");
  }

  @Override
  protected void clearError() {
    // Do nothing
  }

  @Override
  public void print(boolean b) {
    // Do nothing
  }

  @Override
  public void print(char c) {
    // Do nothing
  }

  @Override
  public void print(int i) {
    // Do nothing
  }

  @Override
  public void print(long l) {
    // Do nothing
  }

  @Override
  public void print(float f) {
    // Do nothing
  }

  @Override
  public void print(double d) {
    // Do nothing
  }

  @Override
  public void print(char[] s) {
    // Do nothing
  }

  @Override
  public void print(String s) {
    // Do nothing
  }

  @Override
  public void print(Object obj) {
    // Do nothing
  }

  @Override
  public void println() {
    // Do nothing
  }

  @Override
  public void println(boolean x) {
    // Do nothing
  }

  @Override
  public void println(char x) {
    // Do nothing
  }

  @Override
  public void println(int x) {
    // Do nothing
  }

  @Override
  public void println(long x) {
    // Do nothing
  }

  @Override
  public void println(float x) {
    // Do nothing
  }

  @Override
  public void println(double x) {
    // Do nothing
  }

  @Override
  public void println(char[] x) {
    // Do nothing
  }

  @Override
  public void println(String x) {
    // Do nothing
  }

  @Override
  public void println(Object x) {
    // Do nothing
  }

  @Override
  public NullPrintWriter printf(String format, Object ... args) {
    return this;
  }

  @Override
  public NullPrintWriter printf(Locale l, String format, Object ... args) {
    return this;
  }

  @Override
  public NullPrintWriter format(String format, Object ... args) {
    return this;
  }

  @Override
  public NullPrintWriter format(Locale l, String format, Object ... args) {
    return this;
  }
}
