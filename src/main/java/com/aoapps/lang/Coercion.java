/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  AO Industries, Inc.
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

package com.aoapps.lang;

import com.aoapps.lang.exception.WrappedException;
import com.aoapps.lang.io.AppendableWriter;
import com.aoapps.lang.io.Encoder;
import com.aoapps.lang.io.EncoderWriter;
import com.aoapps.lang.io.Writable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Segment;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

/**
 * Coerces objects to String compatible with JSP Expression Language (JSP EL)
 * and the Java Standard Taglib (JSTL).  Also adds support for seamless output
 * of XML DOM nodes.
 *
 * @author  AO Industries, Inc.
 */
public final class Coercion {

  /** Make no instances. */
  private Coercion() {
    throw new AssertionError();
  }

  private static final Logger logger = Logger.getLogger(Coercion.class.getName());

  /**
   * Converts an object to a string.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} return {@code ""}.</li>
   * <li>When {@link String} return directly.</li>
   * <li>When {@link Writable} return {@link Writable#toString()}.</li>
   * <li>When {@link Segment} or {@link CharSequence} return {@link CharSequence#toString()}.</li>
   * <li>When {@code char[]} return {@code ""} when empty or {@link String#String(char[])}.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8}.</li>
   * <li>Otherwise return {@link Object#toString() value.toString()}.</li>
   * </ol>
   */
  public static String toString(Object value) {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    // If A is null, then the result is "".
    if (value == null) {
      return "";
    }
    // If A is a string, then the result is A.
    if (value instanceof String) {
      return (String) value;
    }
    // If is a Writable, support optimizations
    if (value instanceof Writable) {
      // Note: This is only optimal for Writable that are "isFastToString()",
      //       but we don't have much better option since a String is required.
      //       Keeping this here, instead of falling-through to toString() below,
      //       so behavior is consistent in the odd chance a class is a Node and implements Writable.
      //       This should keep it consistent with other coercions.
      return value.toString();
    }
    // Support Segment and CharSequence
    if (value instanceof CharSequence) {
      return value.toString();
    }
    // Support char[]
    if (value instanceof char[]) {
      char[] chs = (char[]) value;
      return chs.length == 0 ? "" : new String(chs);
    }
    // If is a DOM node, serialize the output
    if (value instanceof Node) {
      try {
        // Can use thread-local or pooled transformers if performance is ever an issue
        // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
        TransformerFactory transFactory = TransformerFactory.newInstance();
        try {
          transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException e) {
          throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
        }
        // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
        // See https://rules.sonarsource.com/java/RSPEC-2755
        transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.transform(
            new DOMSource((Node) value),
            new StreamResult(buffer)
        );
        return buffer.toString();
      } catch (TransformerException e) {
        throw new WrappedException(e);
      }
    }
    // If A.toString() throws an exception, then raise an error
    // Otherwise, the result is A.toString();
    return value.toString();
  }

  private static final Object optimizersLock = new Object();

  /**
   * Using array for maximum iteration performance since is in tight loops.
   */
  @SuppressWarnings("VolatileArrayField")
  private static volatile CoercionOptimizer[] optimizers = new CoercionOptimizer[0];

  /**
   * Registers a new coercion optimizer.
   */
  public static void registerOptimizer(CoercionOptimizer optimizer) {
    synchronized (optimizersLock) {
      CoercionOptimizer[] newOptimizer = Arrays.copyOf(optimizers, optimizers.length + 1);
      newOptimizer[newOptimizer.length - 1] = optimizer;
      optimizers = newOptimizer;
    }
  }

  /**
   * Optimizes the given writer by passing through {@link CoercionOptimizer#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   * on all {@linkplain #registerOptimizer(com.aoapps.lang.CoercionOptimizer) registered coercion optimizers} until
   * there are no replacements.
   */
  public static Writer optimize(Writer out, Encoder encoder) {
    final Writer original = out;
    Writer newOut = out;
    while (true) {
      for (CoercionOptimizer optimizer : optimizers) {
        newOut = optimizer.optimize(newOut, encoder);
      }
      if (newOut == out) {
        break;
      }
      // Will keep looping for further optimization
      out = newOut;
    }
    if (newOut != original && logger.isLoggable(Level.FINER)) {
      logger.finer(
          "Writer optimized from " + original.getClass().getName() + " to " + newOut.getClass().getName()
              + " with encoder " + (encoder == null ? null : encoder.getClass().getName())
      );
    }
    return newOut;
  }

  /**
   * Optimizes the given appendable by dispatching to {@link #optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   * when it is a {@link Writer}.
   *
   * <p>There is currently no implementation of appendable-specific unwrapping,
   * but this is here for consistency with the writer unwrapping.</p>
   *
   * @see  #optimize(java.io.Writer, com.aoapps.lang.io.Encoder)
   */
  public static Appendable optimize(Appendable out, Encoder encoder) {
    if (out instanceof Writer) {
      return optimize((Writer) out, encoder);
    }
    return out;
  }

  /**
   * Writes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not write.</li>
   * <li>When {@link EncoderWriter} unwrap and dispatch to {@link #write(java.lang.Object, com.aoapps.lang.io.Encoder, java.io.Writer)}.</li>
   * <li>When {@link String} write directly.</li>
   * <li>When {@link Writable} write {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#writeTo(java.io.Writer)}.</li>
   * <li>When {@link Segment} write {@link Segment#array}.</li>
   * <li>When {@link CharSequence} append directly.</li>
   * <li>When {@code char[]} write directly.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8}.</li>
   * <li>Otherwise write {@link Object#toString() value.toString()}.</li>
   * </ol>
   */
  public static void write(Object value, Writer out) throws IOException {
    write(value, out, false);
  }

  /**
   * Writes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not write.</li>
   * <li>When {@link EncoderWriter} unwrap and dispatch to {@link #write(java.lang.Object, com.aoapps.lang.io.Encoder, java.io.Writer, boolean)}.</li>
   * <li>When {@link String} write directly.</li>
   * <li>When {@link Writable} write {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#writeTo(java.io.Writer)}.</li>
   * <li>When {@link Segment} write {@link Segment#array}.</li>
   * <li>When {@link CharSequence} append directly.</li>
   * <li>When {@code char[]} write directly.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8}.</li>
   * <li>Otherwise write {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  outOptimized  Is {@code out} already known to have been passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   *                       (with {@code encoder = null})?
   */
  public static void write(Object value, Writer out, boolean outOptimized) throws IOException {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    // If A is null, then the result is "".
    if (value != null) {
      assert out != null;
      if (out instanceof EncoderWriter) {
        // Unwrap media writer and use encoder directly
        EncoderWriter encoderWriter = (EncoderWriter) out;
        write(
            value,
            encoderWriter.getEncoder(),
            encoderWriter.getOut(),
            true // EncoderWriter always optimizes out
        );
      } else {
        // Optimize output
        Writer optimized;
        if (outOptimized) {
          optimized = out;
          assert optimized == optimize(out, null);
        } else {
          optimized = optimize(out, null);
        }
        if (value instanceof String) {
          // If A is a string, then the result is A.
          optimized.write((String) value);
        } else if (value instanceof Writable) {
          // If is a Writable, support optimizations
          Writable writable = (Writable) value;
          if (writable.isFastToString()) {
            optimized.write(writable.toString());
          } else {
            // Avoid intermediate String from Writable
            writable.writeTo(optimized);
          }
        } else if (value instanceof Segment) {
          // Support Segment
          Segment s = (Segment) value;
          optimized.write(s.array, s.offset, s.count);
        } else if (value instanceof CharSequence) {
          // Support CharSequence
          optimized.append((CharSequence) value);
        } else if (value instanceof char[]) {
          // Support char[]
          optimized.write((char[]) value);
        } else if (value instanceof Node) {
          // If is a DOM node, serialize the output
          try {
            // Can use thread-local or pooled transformers if performance is ever an issue
            // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
            TransformerFactory transFactory = TransformerFactory.newInstance();
            try {
              transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
              throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
            }
            // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
            // See https://rules.sonarsource.com/java/RSPEC-2755
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.transform(
                new DOMSource((Node) value),
                new StreamResult(optimized)
            );
          } catch (TransformerException e) {
            throw new IOException(e);
          }
        } else {
          // If A.toString() throws an exception, then raise an error
          // Otherwise, the result is A.toString();
          optimized.write(value.toString());
        }
      }
    }
  }

  /**
   * Encodes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code encoder == null} dispatch to {@link #write(java.lang.Object, java.io.Writer)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not encode.</li>
   * <li>When {@link String} encode directly.</li>
   * <li>When {@link Writable} encode {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#writeTo(com.aoapps.lang.io.Encoder, java.io.Writer)}.</li>
   * <li>When {@link Segment} encode {@link Segment#array}.</li>
   * <li>When {@link CharSequence} encode directly.</li>
   * <li>When {@code char[]} encode directly.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8} while encoding through {@link EncoderWriter}.</li>
   * <li>Otherwise encode {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  encoder  if null, no encoding is performed - write through
   */
  public static void write(Object value, Encoder encoder, Writer out) throws IOException {
    write(value, encoder, out, false);
  }

  /**
   * Encodes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code encoder == null} dispatch to {@link #write(java.lang.Object, java.io.Writer, boolean)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not encode.</li>
   * <li>When {@link String} encode directly.</li>
   * <li>When {@link Writable} encode {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#writeTo(com.aoapps.lang.io.Encoder, java.io.Writer)}.</li>
   * <li>When {@link Segment} encode {@link Segment#array}.</li>
   * <li>When {@link CharSequence} encode directly.</li>
   * <li>When {@code char[]} encode directly.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8} while encoding through {@link EncoderWriter}.</li>
   * <li>Otherwise encode {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  encoder  if null, no encoding is performed - write through
   * @param  outOptimized  Is {@code out} already known to have been passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}?
   */
  public static void write(Object value, Encoder encoder, Writer out, boolean outOptimized) throws IOException {
    if (encoder == null) {
      write(value, out, outOptimized);
    } else {
      // Support Optional
      while (value instanceof Optional) {
        value = ((Optional<?>) value).orElse(null);
      }
      // If A is null, then the result is "".
      if (value != null) {
        // Optimize output
        Writer optimized;
        if (outOptimized) {
          optimized = out;
          assert optimized == optimize(out, encoder);
        } else {
          optimized = optimize(out, encoder);
        }
        // Write through the given encoder
        if (value instanceof String) {
          // If A is a string, then the result is A.
          encoder.write((String) value, optimized);
        } else if (value instanceof Writable) {
          // If is a Writable, support optimizations
          Writable writable = (Writable) value;
          if (writable.isFastToString()) {
            encoder.write(writable.toString(), optimized);
          } else {
            // Avoid intermediate String from Writable
            writable.writeTo(encoder, optimized);
          }
        } else if (value instanceof Segment) {
          // Support Segment
          Segment s = (Segment) value;
          encoder.write(s.array, s.offset, s.count, optimized);
        } else if (value instanceof CharSequence) {
          // Support CharSequence
          encoder.append((CharSequence) value, optimized);
        } else if (value instanceof char[]) {
          // Support char[]
          encoder.write((char[]) value, optimized);
        } else if (value instanceof Node) {
          // If is a DOM node, serialize the output
          try {
            // Can use thread-local or pooled transformers if performance is ever an issue
            // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
            TransformerFactory transFactory = TransformerFactory.newInstance();
            try {
              transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
              throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
            }
            // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
            // See https://rules.sonarsource.com/java/RSPEC-2755
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.transform(
                new DOMSource((Node) value),
                new StreamResult(new EncoderWriter(encoder, optimized, true))
            );
          } catch (TransformerException e) {
            throw new IOException(e);
          }
        } else {
          // If A.toString() throws an exception, then raise an error
          // Otherwise, the result is A.toString();
          encoder.write(value.toString(), optimized);
        }
      }
    }
  }

  /**
   * Appends an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code out} is a {@link Writer} dispatch to {@link #write(java.lang.Object, java.io.Writer)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not append.</li>
   * <li>When {@link String} append directly.</li>
   * <li>When {@link Writable} append {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#appendTo(java.lang.Appendable)}.</li>
   * <li>When {@link Segment} or {@link CharSequence} append directly.</li>
   * <li>When {@code char[]} append wrapped in new {@link Segment}.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8}.</li>
   * <li>Otherwise append {@link Object#toString() value.toString()}.</li>
   * </ol>
   */
  public static void append(Object value, Appendable out) throws IOException {
    append(value, out, false);
  }

  /**
   * Appends an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code out} is a {@link Writer} dispatch to {@link #write(java.lang.Object, java.io.Writer, boolean)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not append.</li>
   * <li>When {@link String} append directly.</li>
   * <li>When {@link Writable} append {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#appendTo(java.lang.Appendable)}.</li>
   * <li>When {@link Segment} or {@link CharSequence} append directly.</li>
   * <li>When {@code char[]} append wrapped in new {@link Segment}.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8}.</li>
   * <li>Otherwise append {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  outOptimized  Is {@code out} already known to have been passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}
   *                       (with {@code encoder = null})?
   */
  public static void append(Object value, Appendable out, boolean outOptimized) throws IOException {
    assert out != null;
    if (out instanceof Writer) {
      write(value, (Writer) out, outOptimized);
    } else {
      // Support Optional
      while (value instanceof Optional) {
        value = ((Optional<?>) value).orElse(null);
      }
      // If A is null, then the result is "".
      if (value != null) {
        // Optimize output
        Appendable optimized;
        if (outOptimized) {
          optimized = out;
          assert optimized == optimize(out, null);
        } else {
          optimized = optimize(out, null);
        }
        if (value instanceof String) {
          // If A is a string, then the result is A.
          optimized.append((String) value);
        } else if (value instanceof Writable) {
          // If is a Writable, support optimizations
          Writable writable = (Writable) value;
          if (writable.isFastToString()) {
            optimized.append(writable.toString());
          } else {
            // Avoid intermediate String from Writable
            writable.appendTo(optimized);
          }
        } else if (value instanceof CharSequence) {
          // Support Segment and CharSequence
          optimized.append((CharSequence) value);
        } else if (value instanceof char[]) {
          // Support char[]
          char[] chs = (char[]) value;
          int chsLen = chs.length;
          if (chsLen > 0) {
            optimized.append(new Segment(chs, 0, chsLen));
          }
        } else if (value instanceof Node) {
          // If is a DOM node, serialize the output
          try {
            // Can use thread-local or pooled transformers if performance is ever an issue
            // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
            TransformerFactory transFactory = TransformerFactory.newInstance();
            try {
              transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
              throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
            }
            // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
            // See https://rules.sonarsource.com/java/RSPEC-2755
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.transform(
                new DOMSource((Node) value),
                new StreamResult(AppendableWriter.wrap(optimized))
            );
          } catch (TransformerException e) {
            throw new IOException(e);
          }
        } else {
          // If A.toString() throws an exception, then raise an error
          // Otherwise, the result is A.toString();
          optimized.append(value.toString());
        }
      }
    }
  }

  /**
   * Encodes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code encoder == null} dispatch to {@link #append(java.lang.Object, java.lang.Appendable)}.</li>
   * <li>When {@code out} is a {@link Writer} dispatch to {@link #write(java.lang.Object, com.aoapps.lang.io.Encoder, java.io.Writer)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not encode.</li>
   * <li>When {@link String} encode directly.</li>
   * <li>When {@link Writable} encode {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to
   *     {@link Writable#appendTo(com.aoapps.lang.io.Encoder, java.lang.Appendable)}.</li>
   * <li>When {@link Segment} or {@link CharSequence} encode directly.</li>
   * <li>When {@code char[]} encode wrapped in new {@link Segment}.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8} while encoding through {@link EncoderWriter} and {@link AppendableWriter}.</li>
   * <li>Otherwise encode {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  encoder  if null, no encoding is performed - write through
   */
  public static void append(Object value, Encoder encoder, Appendable out) throws IOException {
    append(value, encoder, out, false);
  }

  /**
   * Encodes an object's String representation,
   * supporting streaming for specialized types.
   * <ol>
   * <li>When {@code encoder == null} dispatch to {@link #append(java.lang.Object, java.lang.Appendable, boolean)}.</li>
   * <li>When {@code out} is a {@link Writer} dispatch to {@link #write(java.lang.Object, com.aoapps.lang.io.Encoder, java.io.Writer, boolean)}.</li>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} do not encode.</li>
   * <li>When {@link String} encode directly.</li>
   * <li>When {@link Writable} encode {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to
   *     {@link Writable#appendTo(com.aoapps.lang.io.Encoder, java.lang.Appendable)}.</li>
   * <li>When {@link Segment} or {@link CharSequence} encode directly.</li>
   * <li>When {@code char[]} encode wrapped in new {@link Segment}.</li>
   * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8} while encoding through {@link EncoderWriter} and {@link AppendableWriter}.</li>
   * <li>Otherwise encode {@link Object#toString() value.toString()}.</li>
   * </ol>
   *
   * @param  encoder  if null, no encoding is performed - write through
   * @param  outOptimized  Is {@code out} already known to have been passed through {@link Coercion#optimize(java.io.Writer, com.aoapps.lang.io.Encoder)}?
   */
  public static void append(Object value, Encoder encoder, Appendable out, boolean outOptimized) throws IOException {
    if (encoder == null) {
      append(value, out, outOptimized);
    } else if (out instanceof Writer) {
      write(value, encoder, (Writer) out, outOptimized);
    } else {
      // Support Optional
      while (value instanceof Optional) {
        value = ((Optional<?>) value).orElse(null);
      }
      // If A is null, then the result is "".
      if (value != null) {
        // Optimize output
        Appendable optimized;
        if (outOptimized) {
          optimized = out;
          assert optimized == optimize(out, encoder);
        } else {
          optimized = optimize(out, encoder);
        }
        // Write through the given encoder
        if (value instanceof String) {
          // If A is a string, then the result is A.
          encoder.append((String) value, optimized);
        } else if (value instanceof Writable) {
          // If is a Writable, support optimizations
          Writable writable = (Writable) value;
          if (writable.isFastToString()) {
            encoder.append(writable.toString(), optimized);
          } else {
            // Avoid intermediate String from Writable
            writable.appendTo(encoder, optimized);
          }
        } else if (value instanceof CharSequence) {
          // Support Segment and CharSequence
          encoder.append((CharSequence) value, optimized);
        } else if (value instanceof char[]) {
          // Support char[]
          char[] chs = (char[]) value;
          int chsLen = chs.length;
          if (chsLen > 0) {
            encoder.append(new Segment(chs, 0, chsLen), optimized);
          }
        } else if (value instanceof Node) {
          // If is a DOM node, serialize the output
          try {
            // Can use thread-local or pooled transformers if performance is ever an issue
            // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
            TransformerFactory transFactory = TransformerFactory.newInstance();
            try {
              transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
              throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
            }
            // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
            // See https://rules.sonarsource.com/java/RSPEC-2755
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.transform(
                new DOMSource((Node) value),
                new StreamResult(new EncoderWriter(encoder, AppendableWriter.wrap(optimized), true))
            );
          } catch (TransformerException e) {
            throw new IOException(e);
          }
        } else {
          // If A.toString() throws an exception, then raise an error
          // Otherwise, the result is A.toString();
          encoder.append(value.toString(), optimized);
        }
      }
    }
  }

  /**
   * Checks if a value is null or empty.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} return {@code true}.</li>
   * <li>When {@link String} return {@link String#isEmpty()}.</li>
   * <li>When {@link Writable} return <code>{@linkplain Writable#getLength()} == 0</code>.</li>
   * <li>When {@link Segment} or {@link CharSequence} return <code>{@linkplain CharSequence#length()} == 0</code>.</li>
   * <li>When {@code char[]} return {@code value.length == 0}.</li>
   * <li>When {@link Node} return {@code false}.</li>
   * <li>Otherwise return <code>{@linkplain Object#toString() value.toString()}.{@linkplain String#isEmpty() isEmpty()}</code>.</li>
   * </ol>
   *
   * @throws UncheckedIOException on any underlying {@link IOException}.
   */
  public static boolean isEmpty(Object value) {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    if (value == null) {
      // If A is null, then the result is "".
      return true;
    } else if (value instanceof String) {
      // If A is a string, then the result is A.
      return ((String) value).isEmpty();
    } else if (value instanceof Writable) {
      // If is a Writable, support optimizations
      try {
        return ((Writable) value).getLength() == 0;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else if (value instanceof CharSequence) {
      // Support Segment and CharSequence
      return ((CharSequence) value).length() == 0;
    } else if (value instanceof char[]) {
      // Support char[]
      return ((char[]) value).length == 0;
    } else if (value instanceof Node) {
      // If is a DOM node, serialize the output
      return false; // There is a node, is not empty
    } else {
      // If A.toString() throws an exception, then raise an error
      // Otherwise, the result is A.toString();
      return value.toString().isEmpty();
    }
  }

  /**
   * Returns the provided value (possibly converted to a different form, like String) or null if the value is empty.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} return {@code null}.</li>
   * <li>When {@link String} return {@code null} when {@link String#isEmpty()}.</li>
   * <li>When {@link Writable} return {@code null} when <code>{@linkplain Writable#getLength()} == 0</code>.</li>
   * <li>When {@link Segment} or {@link CharSequence} return {@code null} when <code>{@linkplain CharSequence#length()} == 0</code>.</li>
   * <li>When {@code char[]} return {@code null} when {@code value.length == 0}.</li>
   * <li>When {@link Node} return {@code value}.</li>
   * <li>Otherwise return <code>{@linkplain Strings#nullIfEmpty(java.lang.String) Strings.nullIfEmpty}({@linkplain Object#toString() value.toString()})</code>.</li>
   * </ol>
   *
   * @see  #isEmpty(java.lang.Object)
   *
   * @throws UncheckedIOException on any underlying {@link IOException}.
   */
  public static Object nullIfEmpty(Object value) {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    if (value == null) {
      // If A is null, then the result is "".
      return null;
    } else if (value instanceof String) {
      // If A is a string, then the result is A.
      return Strings.nullIfEmpty((String) value);
    } else if (value instanceof Writable) {
      // If is a Writable, support optimizations
      try {
        return ((Writable) value).getLength() == 0 ? null : value;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else if (value instanceof CharSequence) {
      // Support Segment and CharSequence
      return ((CharSequence) value).length() == 0 ? null : value;
    } else if (value instanceof char[]) {
      // Support char[]
      return ((char[]) value).length == 0 ? null : value;
    } else if (value instanceof Node) {
      // If is a DOM node, serialize the output
      return value; // There is a node, is not empty
    } else {
      // If A.toString() throws an exception, then raise an error
      // Otherwise, the result is A.toString();
      return Strings.nullIfEmpty(value.toString());
    }
  }

  /**
   * Returns the provided value trimmed, as per rules of {@link Strings#isWhitespace(int)}.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} return {@code null}.</li>
   * <li>When {@link String} return {@link Strings#trim(java.lang.String)}.</li>
   * <li>When {@link Writable} return {@link Strings#trim(java.lang.String)} when {@link Writable#isFastToString()} otherwise {@link Writable#trim()}.</li>
   * <li>When {@link Segment} or {@link CharSequence} return {@link Strings#trim(java.lang.CharSequence)}.</li>
   * <li>When {@code char[]} return {@code ""} when {@code value.length == 0} or
   *     <code>{@linkplain Strings#trim(java.lang.CharSequence) Strings.trim}({@linkplain Segment#Segment(char[], int, int) new Segment(value, 0, value.length)}</code>)
   *     then {@code value} if nothing trimmed.</li>
   * <li>When {@link Node} return {@code value}.</li>
   * <li>Otherwise return <code>{@linkplain Strings#trim(java.lang.String) Strings.trim}({@linkplain Object#toString() value.toString()})</code>.</li>
   * </ol>
   *
   * @return  The original value (possibly of a different type even when nothing to trim),
   *          a trimmed version of the value (possibly of a different type),
   *          a trimmed {@link String} representation of the object,
   *          or {@code null} when the value is {@code null}.
   *
   * @throws UncheckedIOException on any underlying {@link IOException}.
   */
  public static Object trim(Object value) {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    if (value == null) {
      // If A is null, then the result is "".
      return null;
    } else if (value instanceof String) {
      // If A is a string, then the result is A.
      return Strings.trim((String) value);
    } else if (value instanceof Writable) {
      // If is a Writable, support optimizations
      Writable writable = (Writable) value;
      if (writable.isFastToString()) {
        return Strings.trim(writable.toString());
      } else {
        try {
          return writable.trim();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    } else if (value instanceof CharSequence) {
      // Support Segment and CharSequence
      CharSequence cs = Strings.trim((CharSequence) value);
      return cs.length() == 0 ? "" : cs;
    } else if (value instanceof char[]) {
      // Support char[]
      char[] chs = (char[]) value;
      int chsLen = chs.length;
      if (chsLen == 0) {
        return "";
      }          // Already empty
      CharSequence cs = Strings.trim(new Segment(chs, 0, chsLen));
      return
          (cs.length() == 0) ? ""       // Now empty
              : (cs.length() == chsLen) ? chs // Unchanged
              : cs;                           // Trimmed
    } else if (value instanceof Node) {
      // If is a DOM node, serialize the output
      return value; // There is a node, is not empty
    } else {
      // If A.toString() throws an exception, then raise an error
      // Otherwise, the result is A.toString();
      return Strings.trim(value.toString());
    }
  }

  /**
   * Returns the provided value trimmed, as per rules of {@link Strings#isWhitespace(int)},
   * or {@code null} if the value is empty after trimming.
   * <ol>
   * <li>Any {@link Optional} is unwrapped (supporting any levels of nesting).</li>
   * <li>When {@code null} return {@code null}.</li>
   * <li>When {@link String} return {@link Strings#trimNullIfEmpty(java.lang.String)}.</li>
   * <li>When {@link Writable} return {@link Strings#trimNullIfEmpty(java.lang.String)} when
   *     {@link Writable#isFastToString()} otherwise {@link Writable#trim()} then {@code null} if empty after
   *     trimming.</li>
   * <li>When {@link Segment} or {@link CharSequence} return {@link Strings#trimNullIfEmpty(java.lang.CharSequence)}.</li>
   * <li>When {@code char[]} return {@code null} when {@code value.length == 0} or
   *     <code>{@linkplain Strings#trimNullIfEmpty(java.lang.CharSequence) Strings.trimNullIfEmpty}({@linkplain Segment#Segment(char[], int, int) new Segment(value, 0, value.length)}</code>)
   *     then {@code value} if nothing trimmed.</li>
   * <li>When {@link Node} return {@code value}.</li>
   * <li>Otherwise return <code>{@linkplain Strings#trimNullIfEmpty(java.lang.String) Strings.trimNullIfEmpty}({@linkplain Object#toString() value.toString()})</code>.</li>
   * </ol>
   *
   * @return  The original value (possibly of a different type even when nothing to trim),
   *          a trimmed version of the value (possibly of a different type),
   *          a trimmed {@link String} representation of the object,
   *          or {@code null} when the value is {@code null} or empty after trimming.
   *
   * @throws UncheckedIOException on any underlying {@link IOException}.
   */
  public static Object trimNullIfEmpty(Object value) {
    // Support Optional
    while (value instanceof Optional) {
      value = ((Optional<?>) value).orElse(null);
    }
    if (value == null) {
      // If A is null, then the result is "".
      return null;
    } else if (value instanceof String) {
      // If A is a string, then the result is A.
      return Strings.trimNullIfEmpty((String) value);
    } else if (value instanceof Writable) {
      // If is a Writable, support optimizations
      Writable writable = (Writable) value;
      if (writable.isFastToString()) {
        return Strings.trimNullIfEmpty(writable.toString());
      } else {
        try {
          writable = writable.trim();
          return writable.getLength() == 0 ? null : writable;
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    } else if (value instanceof CharSequence) {
      // Support Segment and CharSequence
      return Strings.trimNullIfEmpty((CharSequence) value);
    } else if (value instanceof char[]) {
      // Support char[]
      char[] chs = (char[]) value;
      int chsLen = chs.length;
      if (chsLen == 0) {
        return null;
      }        // Already empty
      CharSequence cs = Strings.trimNullIfEmpty(new Segment(chs, 0, chsLen));
      return
          (cs == null) ? null           // Now empty
              : (cs.length() == chsLen) ? chs // Unchanged
              : cs;                           // Trimmed
    } else if (value instanceof Node) {
      // If is a DOM node, serialize the output
      return value; // There is a node, is not empty
    } else {
      // If A.toString() throws an exception, then raise an error
      // Otherwise, the result is A.toString();
      return Strings.trimNullIfEmpty(value.toString());
    }
  }

  static {
    for (CoercionOptimizerInitializer initializer : ServiceLoader.load(CoercionOptimizerInitializer.class)) {
      initializer.run();
    }
  }
}
