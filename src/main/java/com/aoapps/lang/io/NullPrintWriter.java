/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2021  AO Industries, Inc.
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
public final class NullPrintWriter extends PrintWriter {

	private static final NullPrintWriter instance = new NullPrintWriter();

	public static NullPrintWriter getInstance() {
		return instance;
	}

	private NullPrintWriter() {
		super(NullWriter.getInstance());
	}

	@Override
	public void write(int c) {
	}

	@Override
	public void write(char[] cbuf) {
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
	}

	@Override
	public void write(String str) {
	}

	@Override
	public void write(String str, int off, int len) {
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
	}

	@Override
	public void close() {
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
	}

	@Override
	public void print(boolean b) {
	}

	@Override
	public void print(char c) {
	}

	@Override
	public void print(int i) {
	}

	@Override
	public void print(long l) {
	}

	@Override
	public void print(float f) {
	}

	@Override
	public void print(double d) {
	}

	@Override
	public void print(char[] s) {
	}

	@Override
	public void print(String s) {
	}

	@Override
	public void print(Object obj) {
	}

	@Override
	public void println() {
	}

	@Override
	public void println(boolean x) {
	}

	@Override
	public void println(char x) {
	}

	@Override
	public void println(int x) {
	}

	@Override
	public void println(long x) {
	}

	@Override
	public void println(float x) {
	}

	@Override
	public void println(double x) {
	}

	@Override
	public void println(char[] x) {
	}

	@Override
	public void println(String x) {
	}

	@Override
	public void println(Object x) {
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
