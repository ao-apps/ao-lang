/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2015, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang.io;

import com.aoapps.lang.NullArgumentException;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writer that encodes during write.
 * <p>
 * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html">MediaEncoder</a>
 * <p>
 *
 * @author  AO Industries, Inc.
 */
public class EncoderWriter extends FilterWriter {

	private final Encoder encoder;

	public EncoderWriter(Encoder encoder, Writer out) {
		super(out);
		this.encoder = NullArgumentException.checkNotNull(encoder, "encoder");
	}

	public Encoder getEncoder() {
		return encoder;
	}

	/**
	 * Gets the wrapped writer.
	 */
	public Writer getOut() {
		return out;
	}

	/**
	 * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html#writePrefixTo(java.lang.Appendable)">MediaEncoder.writePrefixTo(java.lang.Appendable)</a>
	 */
	public void writePrefix() throws IOException {
		encoder.writePrefixTo(out);
	}

	@Override
	public void write(int c) throws IOException {
		encoder.write(c, out);
	}

	@Override
	public void write(char cbuf[]) throws IOException {
		encoder.write(cbuf, out);
	}

	@Override
	public void write(char cbuf[], int off, int len) throws IOException {
		encoder.write(cbuf, off, len, out);
	}

	@Override
	public void write(String str) throws IOException {
		encoder.write(str, out);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		encoder.write(str, off, len, out);
	}

	@Override
	public EncoderWriter append(char c) throws IOException {
		encoder.append(c, out);
		return this;
	}

	@Override
	public EncoderWriter append(CharSequence csq) throws IOException {
		encoder.append(csq, out);
		return this;
	}

	@Override
	public EncoderWriter append(CharSequence csq, int start, int end) throws IOException {
		encoder.append(csq, start, end, out);
		return this;
	}

	/**
	 * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html#writeSuffixTo(java.lang.Appendable)">MediaEncoder.writeSuffixTo(java.lang.Appendable)</a>
	 */
	public void writeSuffix() throws IOException {
		encoder.writeSuffixTo(out);
	}
}
