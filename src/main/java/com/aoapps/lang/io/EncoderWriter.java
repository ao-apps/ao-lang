/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2015, 2016, 2017, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

import com.aoapps.lang.Coercion;
import com.aoapps.lang.NullArgumentException;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writer that encodes during write.
 *
 * <p>See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html">MediaEncoder</a></p>
 *
 * @author  AO Industries, Inc.
 */
public class EncoderWriter extends FilterWriter implements NoClose {

  private final Encoder encoder;

  /**
   * @param  out  Conditionally passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   * @param  outOptimized  Is {@code out} already known to have been passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}?
   */
  public EncoderWriter(Encoder encoder, Writer out, boolean outOptimized) {
    super(outOptimized ? out : Coercion.optimize(out, encoder));
    if (outOptimized) {
      assert out == Coercion.optimize(out, encoder);
    }
    this.encoder = NullArgumentException.checkNotNull(encoder, "encoder");
  }

  /**
   * @param  out  Will be passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   */
  public EncoderWriter(Encoder encoder, Writer out) {
    this(encoder, out, false);
  }

  /**
   * This method may be overridden for the purpose of covariant return, but must return {@link EncoderWriter#encoder}.
   */
  public Encoder getEncoder() {
    return encoder;
  }

  /**
   * Gets the wrapped writer, which has been optimized via
   * {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}.
   *
   * <p>This method may be overridden for the purpose of covariant return, but must return {@link EncoderWriter#out}.</p>
   */
  public Writer getOut() {
    return out;
  }

  @Override
  public boolean isNoClose() {
    return (out instanceof NoClose) && ((NoClose) out).isNoClose();
  }

  /**
   * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html#writePrefixTo(java.lang.Appendable)">MediaEncoder.writePrefixTo(java.lang.Appendable)</a>.
   */
  public void writePrefix() throws IOException {
    encoder.writePrefixTo(out);
  }

  @Override
  public void write(int c) throws IOException {
    encoder.write(c, out);
  }

  @Override
  public void write(char[] cbuf) throws IOException {
    encoder.write(cbuf, out);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
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
   * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html#writeSuffixTo(java.lang.Appendable)">MediaEncoder.writeSuffixTo(java.lang.Appendable)</a>.
   *
   * @deprecated  Please use {@link EncoderWriter#writeSuffix(boolean)} while specifying desired trim.
   */
  @Deprecated
  public final void writeSuffix() throws IOException {
    writeSuffix(false);
  }

  /**
   * See <a href="https://oss.aoapps.com/encoding/apidocs/com.aoapps.encoding/com/aoapps/encoding/MediaEncoder.html#writeSuffixTo(java.lang.Appendable,boolean)">MediaEncoder.writeSuffixTo(java.lang.Appendable, boolean)</a>.
   *
   * @param  trim  Requests that the buffer be trimmed, if buffered and trim supported.
   */
  public void writeSuffix(boolean trim) throws IOException {
    encoder.writeSuffixTo(out, trim);
  }
}
