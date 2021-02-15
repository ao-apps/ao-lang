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
package com.aoindustries.lang;

import com.aoindustries.exception.WrappedException;
import com.aoindustries.io.AppendableWriter;
import com.aoindustries.io.Encoder;
import com.aoindustries.io.EncoderWriter;
import com.aoindustries.io.Writable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
// TODO: Add Coercion support for Optional
public final class Coercion {

	/**
	 * Converts an object to a string.
	 */
	public static String toString(Object value) {
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
		// Support CharSequence
		if(value instanceof CharSequence) return value.toString();
		// Support char[]
		if(value instanceof char[]) return new String((char[])value);
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
	 * @see  #optimize(java.io.Writer, com.aoindustries.io.Encoder)
	 */
	private static Appendable optimize(Appendable out, Encoder encoder) throws IOException {
		if(out instanceof Writer) return optimize((Writer)out, encoder);
		return out;
	}

	/**
	 * Writes an object's String representation,
	 * supporting streaming for specialized types.
	 * <ol>
	 * <li>{@link Node} will be output as {@link StandardCharsets#UTF_8}.</li>
	 * </ol>
	 */
	public static void write(Object value, Writer out) throws IOException {
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
				// Optimize writer
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
	 * Writes an object's String representation,
	 * supporting streaming for specialized types.
	 * 
	 * @param  encoder  if null, no encoding is performed - write through
	 */
	public static void write(Object value, Encoder encoder, Writer out) throws IOException {
		// If A is null, then the result is "".
		if(value != null) {
			if(encoder == null) {
				write(value, out);
			} else {
				// Optimize writer
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
	 * <li>{@link Node} will be output as {@link StandardCharsets#UTF_8}.</li>
	 * </ol>
	 */
	public static void append(Object value, Appendable out) throws IOException {
		assert out != null;
		// If A is null, then the result is "".
		if(value != null) {
			if(out instanceof Writer) {
				write(value, (Writer)out);
			} else {
				// Optimize writer
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
					// Support CharSequence
					out.append((CharSequence)value);
				} else if(value instanceof char[]) {
					// Support char[]
					char[] chs = (char[])value;
					out.append(new Segment(chs, 0, chs.length));
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
	 * Appends an object's String representation,
	 * supporting streaming for specialized types.
	 * 
	 * @param  encoder  if null, no encoding is performed - write through
	 */
	public static void append(Object value, Encoder encoder, Appendable out) throws IOException {
		// If A is null, then the result is "".
		if(value != null) {
			if(encoder == null) {
				append(value, out);
			} else if(out instanceof Writer) {
				write(value, encoder, (Writer)out);
			} else {
				// Optimize writer
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
					// Support CharSequence
					encoder.append((CharSequence)value, out);
				} else if(value instanceof char[]) {
					// Support char[]
					char[] chs = (char[])value;
					encoder.append(new Segment(chs, 0, chs.length), out);
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
	 */
	public static boolean isEmpty(Object value) throws IOException {
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
			// Support CharSequence
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
	 * 
	 * @see  #isEmpty(java.lang.Object)
	 */
	public static Object nullIfEmpty(Object value) throws IOException {
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
			// Support CharSequence
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
	 *
	 * @return  The original value, a trimmed version of the value (possibly of a different type),
	 *          a trimmed {@link String} representation of the object,
	 *          or {@code null} when the value is {@code null}.
	 */
	public static Object trim(Object value) throws IOException {
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
			// Support CharSequence
			return Strings.trim((CharSequence)value);
		} else if(value instanceof char[]) {
			// Support char[]
			char[] chs = (char[])value;
			return Strings.trim(new Segment(chs, 0, chs.length));
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
	 *
	 * @return  The original value, a trimmed version of the value (possibly of a different type),
	 *          a trimmed {@link String} representation of the object,
	 *          or {@code null} when the value is {@code null}.
	 */
	public static Object trimNullIfEmpty(Object value) throws IOException {
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
			// Support CharSequence
			return Strings.trimNullIfEmpty((CharSequence)value);
		} else if(value instanceof char[]) {
			// Support char[]
			char[] chs = (char[])value;
			return Strings.trimNullIfEmpty(new Segment(chs, 0, chs.length));
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
