/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2013, 2016, 2017  AO Industries, Inc.
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
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to write FastExternalizable, Externalizable, and Serializable objects.
 *
 * When multiple objects are being written, this avoids the repetitive writing of classnames and serialVersionUIDs.
 *
 * @author  AO Industries, Inc.
 */
public class FastObjectOutput implements ObjectOutput {

	private static final ThreadLocal<FastObjectOutput> threadFastObjectOutput = new ThreadLocal<FastObjectOutput>();

	/**
	 * Gets the wrapper for the provided ObjectOutput, creating if needed.
	 * To avoid memory leaks, it should also be unwrapped in a finally block.
	 */
	public static FastObjectOutput wrap(ObjectOutput out) throws IOException {
		FastObjectOutput fastOut;
		if(out instanceof FastObjectOutput) {
			fastOut = (FastObjectOutput)out;
		} else {
			fastOut = threadFastObjectOutput.get();
			if(fastOut==null) {
				threadFastObjectOutput.set(fastOut = new FastObjectOutput(out));
			} else {
				// Must be same as previously used value
				if(out!=fastOut.out) throw new IOException("ObjectOutput changed unexpectedly");
			}
		}
		fastOut.incrementWrapCount();
		return fastOut;
	}

	/**
	 * Unwraps the object output.
	 */
	public void unwrap() throws IOException {
		if(decrementWrapCount()==0) threadFastObjectOutput.remove();
	}

	static final int
		// The object is null
		NULL = 0,
		// The object uses standard serialization
		STANDARD = NULL+1,
		// The object is of a previously unseen class
		FAST_NEW = STANDARD+1,
		// The object is the same class as the previous object
		FAST_SAME = FAST_NEW+1,
		// The object is of a class that has already been seen, and uses the next two bytes as its class ID - (255 - FAST_SEEN_CLASS_INT)
		FAST_SEEN_SHORT = FAST_SAME+1,
		// The object is of a class that has already been seen, and uses the next four bytes as its class ID
		FAST_SEEN_INT = FAST_SEEN_SHORT+1
		// The remaining values are for direct already seen classes between 0 <= ID < (255-FAST_SEEN_CLASS_INT)
	;

	// Arrays are used for a quick sequential scan before performing the Map lookup.
	private static final int MAP_ARRAY_LENGTH = 20; // TODO: Benchmark what is best value

	private final ObjectOutput out;
	private int wrapCount;

	/**
	 * A mapping of classes to generated class IDs.
	 */
	private Map<Class<?>,Integer> classesMap;
	private final Class<?>[] classesArray = new Class<?>[MAP_ARRAY_LENGTH];
	private int nextClassId = 0;
	private Class<?> lastClass = null;

	/**
	 * A mapping of fast string IDs.
	 */
	private Map<String,Integer> stringsMap;
	private final String[] stringsArray = new String[MAP_ARRAY_LENGTH];
	private int nextStringId = 0;
	private String lastString = null;

	private FastObjectOutput(ObjectOutput out) {
		this.out = out;
	}

	private void incrementWrapCount() throws IOException {
		if(wrapCount==Integer.MAX_VALUE) throw new IOException("Maximum wrap count reached.");
		wrapCount++;
	}

	private int decrementWrapCount() {
		if(wrapCount>0) wrapCount--;
		return wrapCount;
	}

	/**
	 * Writes the provided object in the most efficient manner possible, with no object graph tracking (if possible).
	 *
	 * If the object is null, writes a single byte of <code>NULL</code>.
	 *
	 * If the object is not FastSerializable, writes <code>STANDARD</code> and then uses standard Java serialization.
	 *
	 * Otherwise, calls writeFastObject(FastSerializable).
	 *
	 * This allows individual objects to switch between FastExternalizable and standard serialization without calling
	 * code needing to know the difference.
	 *
	 * @see  #readObject
	 */
	@Override
	public void writeObject(Object obj) throws IOException {
		if(obj==null) {
			out.write(NULL);
		} else if(!(obj instanceof FastExternalizable)) {
			out.write(STANDARD);
			out.writeObject(obj);
		} else {
			writeFastObject((FastExternalizable)obj);
		}
	}

	/**
	 * Writes a fast externalizable object to the provided stream, supporting null values.
	 *
	 * @see  #readFastObject
	 */
	protected void writeFastObject(FastExternalizable obj) throws IOException {
		if(obj==null) {
			out.write(NULL);
		} else {
			Class<?> clazz = obj.getClass();
			if(clazz==lastClass) {
				out.write(FAST_SAME);
			} else {
				int classId;
				for(
					classId=nextClassId < MAP_ARRAY_LENGTH ? nextClassId-1 : MAP_ARRAY_LENGTH-1;
					classId>=0;
					classId--
				) {
					if(classesArray[classId]==clazz) break;
				}
				if(classId==-1 && classesMap!=null) {
					Integer classIdObj = classesMap.get(clazz);
					if(classIdObj!=null) classId = classIdObj;
				}
				if(classId==-1) {
					if(nextClassId<MAP_ARRAY_LENGTH) {
						classesArray[nextClassId] = clazz;
					} else {
						if(classesMap==null) classesMap = new HashMap<Class<?>,Integer>();
						classesMap.put(clazz, nextClassId);
					}
					nextClassId++;
					out.write(FAST_NEW);
					out.writeUTF(clazz.getName());
					out.writeLong(obj.getSerialVersionUID());
				} else {
					if(classId < (255-FAST_SEEN_INT)) { // 0 - 250
						int code = classId + (FAST_SEEN_INT + 1);
						assert code>FAST_SEEN_INT;
						assert code<=255;
						out.write(code);
					} else if(classId <= (65536 + (255-FAST_SEEN_INT))) { // 251-65786
						out.write(FAST_SEEN_SHORT);
						int offset = classId - (255-FAST_SEEN_INT);
						assert offset>=0;
						assert offset<=65535;
						out.writeShort(offset);
					} else {
						out.write(FAST_SEEN_INT); // 65787-Integer.MAX_VALUE, no offset
						assert classId > (65536 + (255-FAST_SEEN_INT));
						out.writeInt(classId);
					}
				}
				lastClass = clazz;
			}
			obj.writeExternal(this);
		}
	}

	/**
	 * Writes a string to the output, not writing any duplicates.
	 * Supports nulls.
	 */
	public void writeFastUTF(String value) throws IOException {
		if(value==null) {
			out.write(NULL);
		} else {
			if(value==lastString) {
				out.write(FAST_SAME);
			} else {
				int stringId;
				for(
					stringId=nextStringId < MAP_ARRAY_LENGTH ? nextStringId-1 : MAP_ARRAY_LENGTH-1;
					stringId>=0;
					stringId--
				) {
					if(stringsArray[stringId]==value) break;
				}
				if(stringId==-1 && stringsMap!=null) {
					Integer stringIdObj = stringsMap.get(value);
					if(stringIdObj!=null) stringId = stringIdObj;
				}
				if(stringId==-1) {
					if(nextStringId<MAP_ARRAY_LENGTH) {
						stringsArray[nextStringId] = value;
					} else {
						if(stringsMap==null) stringsMap = new HashMap<String,Integer>();
						stringsMap.put(value, nextStringId);
					}
					nextStringId++;
					out.write(FAST_NEW);
					out.writeUTF(value);
				} else {
					if(stringId < (255-FAST_SEEN_INT)) { // 0 - 250
						int code = stringId + (FAST_SEEN_INT + 1);
						assert code>FAST_SEEN_INT;
						assert code<=255;
						out.write(code);
					} else if(stringId <= (65536 + (255-FAST_SEEN_INT))) { // 251-65786
						//try {
							out.write(FAST_SEEN_SHORT);
							int offset = stringId - (255-FAST_SEEN_INT);
							assert offset>=0;
							assert offset<=65535;
							out.writeShort(offset);
						//} catch(AssertionError exc) {
						//    throw exc;
						//}
					} else {
						out.write(FAST_SEEN_INT); // 65787-Integer.MAX_VALUE, no offset
						assert stringId > (65536 + (255-FAST_SEEN_INT));
						out.writeInt(stringId);
					}
				}
				lastString = value;
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		out.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		out.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		out.writeShort(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		out.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		out.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		out.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		out.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		out.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		out.writeUTF(s);
	}
}
