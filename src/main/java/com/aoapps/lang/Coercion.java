/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2015, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang;

import com.aoapps.lang.exception.WrappedException;
import com.aoapps.lang.io.AppendableWriter;
import com.aoapps.lang.io.Encoder;
import com.aoapps.lang.io.EncoderWriter;
import com.aoapps.lang.io.Writable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.swing.text.Segment;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		// If A is null, then the result is "".
		if(value == null) return "";
		// If A is a string, then the result is A.
		if(value instanceof String) return (String)value;
		// If is a Writable, support optimizations
		if(value instanceof Writable) {
			// Note: This is only optimal for Writable that are "isFastToString()",
			//       but we don't have much better option since a String is required.
			//       Keeping this here, instead of falling-through to toString() below,
			//       so behavior is consistent in the odd chance a class is a Node and implements Writable.
			//       This should keep it consistent with other coercions.
			return value.toString();
		}
		// Support Segment and CharSequence
		if(value instanceof CharSequence) return value.toString();
		// Support char[]
		if(value instanceof char[]) {
			char[] chs = (char[])value;
			return chs.length == 0 ? "" : new String(chs);
		}
		// If is a DOM node, serialize the output
		if(value instanceof Node) {
			try {
				// Can use thread-local or pooled transformers if performance is ever an issue
				TransformerFactory transFactory = TransformerFactory.newInstance();
				Transformer transformer = transFactory.newTransformer();
				StringWriter buffer = new StringWriter();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
				transformer.transform(
					new DOMSource((Node)value),
					new StreamResult(buffer)
				);
				return buffer.toString();
			} catch(TransformerException e) {
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
		synchronized(optimizersLock) {
			CoercionOptimizer[] newOptimizer = Arrays.copyOf(optimizers, optimizers.length + 1);
			newOptimizer[newOptimizer.length - 1] = optimizer;
			optimizers = newOptimizer;
		}
	}

	/**
	 * Optimizes the given writer.
	 */
	private static Writer optimize(Writer out, Encoder encoder) throws IOException {
		while(true) {
			Writer newOut = out;
			for(CoercionOptimizer optimizer : optimizers) {
				newOut = optimizer.optimize(newOut, encoder);
			}
			if(newOut == out) return out;
			// Will keep looping for further optimization
			out = newOut;
		}
	}

	/**
	 * Optimizes the given appendable.
	 * <p>
	 * There is currently no implementation of appendable-specific unwrapping,
	 * but this is here for consistency with the writer unwrapping.
	 * </p>
	 *
	 * @see  #optimize(java.io.Writer, com.aoapps.lang.io.Encoder)
	 */
	private static Appendable optimize(Appendable out, Encoder encoder) throws IOException {
		if(out instanceof Writer) return optimize((Writer)out, encoder);
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
		// Support Optional
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		// If A is null, then the result is "".
		if(value != null) {
			assert out != null;
			if(out instanceof EncoderWriter) {
				// Unwrap media writer and use encoder directly
				EncoderWriter encoderWriter = (EncoderWriter)out;
				write(
					value,
					encoderWriter.getEncoder(),
					encoderWriter.getOut()
				);
			} else {
				// Optimize output
				out = optimize(out, null);
				if(value instanceof String) {
					// If A is a string, then the result is A.
					out.write((String)value);
				} else if(value instanceof Writable) {
					// If is a Writable, support optimizations
					Writable writable = (Writable)value;
					if(writable.isFastToString()) {
						out.write(writable.toString());
					} else {
						// Avoid intermediate String from Writable
						writable.writeTo(out);
					}
				} else if(value instanceof Segment) {
					// Support Segment
					Segment s = (Segment)value;
					out.write(s.array, s.offset, s.count);
				} else if(value instanceof CharSequence) {
					// Support CharSequence
					out.append((CharSequence)value);
				} else if(value instanceof char[]) {
					// Support char[]
					out.write((char[])value);
				} else if(value instanceof Node) {
					// If is a DOM node, serialize the output
					try {
						// Can use thread-local or pooled transformers if performance is ever an issue
						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
						transformer.transform(
							new DOMSource((Node)value),
							new StreamResult(out)
						);
					} catch(TransformerException e) {
						throw new IOException(e);
					}
				} else {
					// If A.toString() throws an exception, then raise an error
					// Otherwise, the result is A.toString();
					out.write(value.toString());
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
		if(encoder == null) {
			write(value, out);
		} else {
			// Support Optional
			while(value instanceof Optional) {
				value = ((Optional<?>)value).orElse(null);
			}
			// If A is null, then the result is "".
			if(value != null) {
				// Optimize output
				out = optimize(out, encoder);
				// Write through the given encoder
				if(value instanceof String) {
					// If A is a string, then the result is A.
					encoder.write((String)value, out);
				} else if(value instanceof Writable) {
					// If is a Writable, support optimizations
					Writable writable = (Writable)value;
					if(writable.isFastToString()) {
						encoder.write(writable.toString(), out);
					} else {
						// Avoid intermediate String from Writable
						writable.writeTo(encoder, out);
					}
				} else if(value instanceof Segment) {
					// Support Segment
					Segment s = (Segment)value;
					encoder.write(s.array, s.offset, s.count, out);
				} else if(value instanceof CharSequence) {
					// Support CharSequence
					encoder.append((CharSequence)value, out);
				} else if(value instanceof char[]) {
					// Support char[]
					encoder.write((char[])value, out);
				} else if(value instanceof Node) {
					// If is a DOM node, serialize the output
					try {
						// Can use thread-local or pooled transformers if performance is ever an issue
						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
						transformer.transform(
							new DOMSource((Node)value),
							new StreamResult(new EncoderWriter(encoder, out))
						);
					} catch(TransformerException e) {
						throw new IOException(e);
					}
				} else {
					// If A.toString() throws an exception, then raise an error
					// Otherwise, the result is A.toString();
					encoder.write(value.toString(), out);
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
		assert out != null;
		if(out instanceof Writer) {
			write(value, (Writer)out);
		} else {
			// Support Optional
			while(value instanceof Optional) {
				value = ((Optional<?>)value).orElse(null);
			}
			// If A is null, then the result is "".
			if(value != null) {
				// Optimize output
				out = optimize(out, null);
				if(value instanceof String) {
					// If A is a string, then the result is A.
					out.append((String)value);
				} else if(value instanceof Writable) {
					// If is a Writable, support optimizations
					Writable writable = (Writable)value;
					if(writable.isFastToString()) {
						out.append(writable.toString());
					} else {
						// Avoid intermediate String from Writable
						writable.appendTo(out);
					}
				} else if(value instanceof CharSequence) {
					// Support Segment and CharSequence
					out.append((CharSequence)value);
				} else if(value instanceof char[]) {
					// Support char[]
					char[] chs = (char[])value;
					int chsLen = chs.length;
					if(chsLen > 0) out.append(new Segment(chs, 0, chsLen));
				} else if(value instanceof Node) {
					// If is a DOM node, serialize the output
					try {
						// Can use thread-local or pooled transformers if performance is ever an issue
						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
						transformer.transform(
							new DOMSource((Node)value),
							new StreamResult(AppendableWriter.wrap(out))
						);
					} catch(TransformerException e) {
						throw new IOException(e);
					}
				} else {
					// If A.toString() throws an exception, then raise an error
					// Otherwise, the result is A.toString();
					out.append(value.toString());
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
	 * <li>When {@link Writable} encode {@link Writable#toString()} when {@link Writable#isFastToString()} or dispatch to {@link Writable#appendTo(com.aoapps.lang.io.Encoder, java.lang.Appendable)}.</li>
	 * <li>When {@link Segment} or {@link CharSequence} encode directly.</li>
	 * <li>When {@code char[]} encode wrapped in new {@link Segment}.</li>
	 * <li>When {@link Node} serialize the output as {@link StandardCharsets#UTF_8} while encoding through {@link EncoderWriter} and {@link AppendableWriter}.</li>
	 * <li>Otherwise encode {@link Object#toString() value.toString()}.</li>
	 * </ol>
	 *
	 * @param  encoder  if null, no encoding is performed - write through
	 */
	public static void append(Object value, Encoder encoder, Appendable out) throws IOException {
		if(encoder == null) {
			append(value, out);
		} else if(out instanceof Writer) {
			write(value, encoder, (Writer)out);
		} else {
			// Support Optional
			while(value instanceof Optional) {
				value = ((Optional<?>)value).orElse(null);
			}
			// If A is null, then the result is "".
			if(value != null) {
				// Optimize output
				out = optimize(out, encoder);
				// Write through the given encoder
				if(value instanceof String) {
					// If A is a string, then the result is A.
					encoder.append((String)value, out);
				} else if(value instanceof Writable) {
					// If is a Writable, support optimizations
					Writable writable = (Writable)value;
					if(writable.isFastToString()) {
						encoder.append(writable.toString(), out);
					} else {
						// Avoid intermediate String from Writable
						writable.appendTo(encoder, out);
					}
				} else if(value instanceof CharSequence) {
					// Support Segment and CharSequence
					encoder.append((CharSequence)value, out);
				} else if(value instanceof char[]) {
					// Support char[]
					char[] chs = (char[])value;
					int chsLen = chs.length;
					if(chsLen > 0) encoder.append(new Segment(chs, 0, chsLen), out);
				} else if(value instanceof Node) {
					// If is a DOM node, serialize the output
					try {
						// Can use thread-local or pooled transformers if performance is ever an issue
						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
						transformer.transform(
							new DOMSource((Node)value),
							new StreamResult(new EncoderWriter(encoder, AppendableWriter.wrap(out)))
						);
					} catch(TransformerException e) {
						throw new IOException(e);
					}
				} else {
					// If A.toString() throws an exception, then raise an error
					// Otherwise, the result is A.toString();
					encoder.append(value.toString(), out);
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
	 */
	public static boolean isEmpty(Object value) throws IOException {
		// Support Optional
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		if(value == null) {
			// If A is null, then the result is "".
			return true;
		} else if(value instanceof String) {
			// If A is a string, then the result is A.
			return ((String)value).isEmpty();
		} else if(value instanceof Writable) {
			// If is a Writable, support optimizations
			return ((Writable)value).getLength() == 0;
		} else if(value instanceof CharSequence) {
			// Support Segment and CharSequence
			return ((CharSequence)value).length() == 0;
		} else if(value instanceof char[]) {
			// Support char[]
			return ((char[])value).length == 0;
		} else if(value instanceof Node) {
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
	 */
	public static Object nullIfEmpty(Object value) throws IOException {
		// Support Optional
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		if(value == null) {
			// If A is null, then the result is "".
			return null;
		} else if(value instanceof String) {
			// If A is a string, then the result is A.
			return Strings.nullIfEmpty((String)value);
		} else if(value instanceof Writable) {
			// If is a Writable, support optimizations
			return ((Writable)value).getLength() == 0 ? null : value;
		} else if(value instanceof CharSequence) {
			// Support Segment and CharSequence
			return ((CharSequence)value).length() == 0 ? null : value;
		} else if(value instanceof char[]) {
			// Support char[]
			return ((char[])value).length == 0 ? null : value;
		} else if(value instanceof Node) {
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
	 * <li>When {@code char[]} return {@code ""} when {@code value.length == 0} or <code>{@linkplain Strings#trim(java.lang.CharSequence) Strings.trim}({@linkplain Segment#Segment(char[], int, int) new Segment(value, 0, value.length)}</code>) then {@code value} if nothing trimmed.</li>
	 * <li>When {@link Node} return {@code value}.</li>
	 * <li>Otherwise return <code>{@linkplain Strings#trim(java.lang.String) Strings.trim}({@linkplain Object#toString() value.toString()})</code>.</li>
	 * </ol>
	 *
	 * @return  The original value (possibly of a different type even when nothing to trim),
	 *          a trimmed version of the value (possibly of a different type),
	 *          a trimmed {@link String} representation of the object,
	 *          or {@code null} when the value is {@code null}.
	 */
	public static Object trim(Object value) throws IOException {
		// Support Optional
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		if(value == null) {
			// If A is null, then the result is "".
			return null;
		} else if(value instanceof String) {
			// If A is a string, then the result is A.
			return Strings.trim((String)value);
		} else if(value instanceof Writable) {
			// If is a Writable, support optimizations
			Writable writable = (Writable)value;
			if(writable.isFastToString()) {
				return Strings.trim(writable.toString());
			} else {
				return writable.trim();
			}
		} else if(value instanceof CharSequence) {
			// Support Segment and CharSequence
			CharSequence cs = Strings.trim((CharSequence)value);
			return cs.length() == 0 ? "" : cs;
		} else if(value instanceof char[]) {
			// Support char[]
			char[] chs = (char[])value;
			int chsLen = chs.length;
			if(chsLen == 0) return "";          // Already empty
			CharSequence cs = Strings.trim(new Segment(chs, 0, chsLen));
			return
				  (cs.length() == 0) ? ""       // Now empty
				: (cs.length() == chsLen) ? chs // Unchanged
				: cs;                           // Trimmed
		} else if(value instanceof Node) {
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
	 * <li>When {@link Writable} return {@link Strings#trimNullIfEmpty(java.lang.String)} when {@link Writable#isFastToString()} otherwise {@link Writable#trim()} then {@code null} if empty after trimming.</li>
	 * <li>When {@link Segment} or {@link CharSequence} return {@link Strings#trimNullIfEmpty(java.lang.CharSequence)}.</li>
	 * <li>When {@code char[]} return {@code null} when {@code value.length == 0} or <code>{@linkplain Strings#trimNullIfEmpty(java.lang.CharSequence) Strings.trimNullIfEmpty}({@linkplain Segment#Segment(char[], int, int) new Segment(value, 0, value.length)}</code>) then {@code value} if nothing trimmed.</li>
	 * <li>When {@link Node} return {@code value}.</li>
	 * <li>Otherwise return <code>{@linkplain Strings#trimNullIfEmpty(java.lang.String) Strings.trimNullIfEmpty}({@linkplain Object#toString() value.toString()})</code>.</li>
	 * </ol>
	 *
	 * @return  The original value (possibly of a different type even when nothing to trim),
	 *          a trimmed version of the value (possibly of a different type),
	 *          a trimmed {@link String} representation of the object,
	 *          or {@code null} when the value is {@code null} or empty after trimming.
	 */
	public static Object trimNullIfEmpty(Object value) throws IOException {
		// Support Optional
		while(value instanceof Optional) {
			value = ((Optional<?>)value).orElse(null);
		}
		if(value == null) {
			// If A is null, then the result is "".
			return null;
		} else if(value instanceof String) {
			// If A is a string, then the result is A.
			return Strings.trimNullIfEmpty((String)value);
		} else if(value instanceof Writable) {
			// If is a Writable, support optimizations
			Writable writable = (Writable)value;
			if(writable.isFastToString()) {
				return Strings.trimNullIfEmpty(writable.toString());
			} else {
				writable = writable.trim();
				return writable.getLength() == 0 ? null : writable;
			}
		} else if(value instanceof CharSequence) {
			// Support Segment and CharSequence
			return Strings.trimNullIfEmpty((CharSequence)value);
		} else if(value instanceof char[]) {
			// Support char[]
			char[] chs = (char[])value;
			int chsLen = chs.length;
			if(chsLen == 0) return null;        // Already empty
			CharSequence cs = Strings.trimNullIfEmpty(new Segment(chs, 0, chsLen));
			return
				  (cs == null) ? null           // Now empty
				: (cs.length() == chsLen) ? chs // Unchanged
				: cs;                           // Trimmed
		} else if(value instanceof Node) {
			// If is a DOM node, serialize the output
			return value; // There is a node, is not empty
		} else {
			// If A.toString() throws an exception, then raise an error
			// Otherwise, the result is A.toString();
			return Strings.trimNullIfEmpty(value.toString());
		}
	}

	/**
	 * Make no instances.
	 */
	private Coercion() {
	}

	static {
		for(CoercionOptimizerInitializer initializer : ServiceLoader.load(CoercionOptimizerInitializer.class)) {
			initializer.run();
		}
	}
}
