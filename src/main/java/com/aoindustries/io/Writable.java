/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
 * along with ao-lang.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Something that may be written to a Writer output instead of being
 * converted toString and then having the String written.
 *
 * @author  AO Industries, Inc.
 */
public interface Writable  {

	/**
	 * Gets the number of characters represented by this Writable.
	 */
	long getLength() throws IOException;

	/**
	 * Checks if the writable will be able to be converted toString in an extremely efficient manner.
	 * This means without allocating any new buffer space or string copies.
	 * Callers should prefer {@code toString} over {@code writeTo} or {@code appendTo} when {@code isFastToString} returns true.
	 * <p>
	 * Note: As of Java 1.7.0_06, {@link String#substring(int, int)} and related operations now
	 * copy underlying buffers.
	 * </p>
	 */
	boolean isFastToString();

	/**
	 * Anything writable must have a {@code toString} consistent with what would be
	 * written by the {@code writeTo} and {@code appendTo} methods.  For larger amounts of data, it is
	 * likely much more efficient to call the most appropriate {@code writeTo} or {@code appendTo} method.
	 */
	@Override
	String toString();

	/**
	 * Writes a streamed version of the object's String representation.
	 * What is written must be the same as if {@code out.write(this.toString())}
	 * were called, but may be a much more efficient implementation.
	 */
	void writeTo(Writer out) throws IOException;
	
	/**
	 * Writes a streamed version of the object's String representation.
	 * What is written must be the same as if {@code out.write(this.toString(), off, len)}
	 * were called, but may be a much more efficient implementation.
	 */
	void writeTo(Writer out, long off, long len) throws IOException;

	/**
	 * Writes a streamed version of the object's String representation using the given encoder.
	 * What is written must be the same as if {@code encoder.write(this.toString(), out)}
	 * were called, but may be a much more efficient implementation.
	 * 
	 * @param  encoder  if {@code null}, no encoding is performed and will be the same as a call to {@link #writeTo(java.io.Writer)}
	 */
	void writeTo(Encoder encoder, Writer out) throws IOException;

	/**
	 * Writes a streamed version of the object's String representation using the given encoder.
	 * What is written must be the same as if {@code encoder.write(this.toString(), off, len, out)}
	 * were called, but may be a much more efficient implementation.
	 * 
	 * @param  encoder  if null, no encoding is performed and will be the same as a call to {@link #writeTo(java.io.Writer, long, long)}
	 */
	void writeTo(Encoder encoder, Writer out, long off, long len) throws IOException;

	/**
	 * Appends a streamed version of the object's String representation.
	 * What is appended must be the same as if {@code out.append(this.toString())}
	 * were called, but may be a much more efficient implementation.
	 */
	default void appendTo(Appendable out) throws IOException {
		assert out != null;
		if(out instanceof Writer) {
			writeTo((Writer)out);
		} else if(isFastToString()) {
			out.append(toString());
		} else {
			writeTo(new AppendableWriter(out));
		}
	}

	/**
	 * Appends a streamed version of the object's String representation.
	 * What is appended must be the same as if {@code out.append(this.toString(), start, end)}
	 * were called, but may be a much more efficient implementation.
	 */
	default void appendTo(Appendable out, long start, long end) throws IOException {
		assert out != null;
		if(out instanceof Writer) {
			writeTo((Writer)out, start, end - start);
		} else if(
			   start >= Integer.MIN_VALUE
			&& start <= Integer.MAX_VALUE
			&& end >= Integer.MIN_VALUE
			&& end <= Integer.MAX_VALUE
			&& isFastToString()
		) {
			out.append(toString(), (int)start, (int)end);
		} else {
			writeTo(new AppendableWriter(out), start, end - start);
		}
	}

	/**
	 * Appends a streamed version of the object's String representation using the given encoder.
	 * What is appended must be the same as if {@code encoder.append(this.toString(), out)}
	 * were called, but may be a much more efficient implementation.
	 * 
	 * @param  encoder  if {@code null}, no encoding is performed and will be the same as a call to {@link #appendTo(java.lang.Appendable)}
	 */
	default void appendTo(Encoder encoder, Appendable out) throws IOException {
		assert out != null;
		if(encoder == null) {
			appendTo(out);
		} else if(out instanceof Writer) {
			writeTo(encoder, (Writer)out);
		} else if(isFastToString()) {
			encoder.append(toString(), out);
		} else {
			writeTo(encoder, new AppendableWriter(out));
		}
	}

	/**
	 * Appends a streamed version of the object's String representation using the given encoder.
	 * What is appended must be the same as if {@code encoder.append(this.toString(), start, end, out)}
	 * were called, but may be a much more efficient implementation.
	 * 
	 * @param  encoder  if null, no encoding is performed and will be the same as a call to {@link #appendTo(java.lang.Appendable, long, long)}
	 */
	default void appendTo(Encoder encoder, Appendable out, long start, long end) throws IOException {
		assert out != null;
		if(encoder == null) {
			appendTo(out, start, end);
		} else if(out instanceof Writer) {
			writeTo(encoder, (Writer)out, start, end - start);
		} else if(
			   start >= Integer.MIN_VALUE
			&& start <= Integer.MAX_VALUE
			&& end >= Integer.MIN_VALUE
			&& end <= Integer.MAX_VALUE
			&& isFastToString()
		) {
			encoder.append(toString(), (int)start, (int)end, out);
		} else {
			writeTo(encoder, new AppendableWriter(out), start, end - start);
		}
	}

	/**
	 * Trims the contents of this writable, as per rules of {@link StringUtility#isWhitespace(int)},
	 * returning the instance that represents this writable trimmed.
	 * <p>
	 * It will most likely be faster to check {@link #isFastToString()} and then trim the result
	 * of {@link #toString()}.  However, for non-fast-toString writables, this trim will be more
	 * efficient.
	 * </p>
	 */
	Writable trim() throws IOException;
}
