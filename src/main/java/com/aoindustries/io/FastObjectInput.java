/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2013, 2016, 2017, 2019  AO Industries, Inc.
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputValidation;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to read {@link FastExternalizable}, {@link Externalizable}, and {@link Serializable} objects.
 * <p>
 * When multiple objects are being written, this avoids the repetitive writing of classnames and serialVersionUIDs.
 * </p>
 * <p>
 * Any object that is {@link ObjectInputValidation} is validated immediately - there is no need
 * and no mechanism to register the validation since this is for simple value objects
 * that don't participate in more complex object graphs.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class FastObjectInput implements ObjectInput {

	private static final ThreadLocal<FastObjectInput> threadFastObjectInput = new ThreadLocal<>();

	/**
	 * Gets the wrapper for the provided {@link ObjectInput}, creating if needed.
	 * To avoid memory leaks, it must also be {@link #unwrap() unwrapped} in a finally block.
	 * <p>
	 * TODO: Can {@link FastObjectInput} itself implement {@link AutoCloseable} for {@link #unwrap()}?
	 *       Maybe this means it no longer implements {@link ObjectInput} directly?
	 * </p>
	 */
	public static FastObjectInput wrap(ObjectInput in) throws IOException {
		FastObjectInput fastIn;
		if(in instanceof FastObjectInput) {
			fastIn = (FastObjectInput)in;
		} else {
			fastIn = threadFastObjectInput.get();
			if(fastIn == null) {
				threadFastObjectInput.set(fastIn = new FastObjectInput(in));
			} else {
				// Must be same as previously used value
				if(in != fastIn.in) throw new IOException("ObjectInput changed unexpectedly");
			}
		}
		fastIn.incrementWrapCount();
		return fastIn;
	}

	private final ObjectInput in;
	private int wrapCount;

	/**
	 * A mapping of generated IDs to classes.
	 */
	private final List<Class<?>> classesById = new ArrayList<>();
	private final List<Long> serialVersionUIDsById = new ArrayList<>();
	private int nextClassId = 0;

	private Class<?> lastClass = null;
	private long lastSerialVersionUID = 0;

	/**
	 * A mapping of generated IDs to strings.
	 */
	private final List<String> stringsById = new ArrayList<>();
	private int nextStringId = 0;

	private String lastString = null;

	private FastObjectInput(ObjectInput in) {
		this.in = in;
	}

	private void incrementWrapCount() throws IOException {
		if(wrapCount == Integer.MAX_VALUE) throw new IOException("Maximum wrap count reached.");
		wrapCount++;
	}

	/**
	 * Unwraps the object input.
	 *
	 * @throws  IllegalStateException  if not wrapped
	 */
	public void unwrap() throws IllegalStateException {
		assert wrapCount >= 0;
		if(wrapCount == 0) throw new IllegalStateException("Not wrapped");
		wrapCount--;
		if(wrapCount == 0) threadFastObjectInput.remove();
	}

	/**
	 * Reads a possibly-{@link FastExternalizable} object from the stream.
	 *
	 * @see  FastObjectOutput#writeObject(java.lang.Object)
	 */
	@Override
	public Object readObject() throws IOException, ClassNotFoundException {
		int code = in.read();
		switch(code) {
			case FastObjectOutput.NULL :
				return null;
			case FastObjectOutput.STANDARD :
				return in.readObject();
			case -1 :
				throw new EOFException();
			default :
				return readFastObject(code);
		}
	}

	/**
	 * Reads a {@link FastExternalizable} object from the stream.
	 *
	 * @see  FastObjectOutput#writeFastObject(com.aoindustries.io.FastExternalizable)
	 */
	protected FastExternalizable readFastObject() throws IOException, ClassNotFoundException {
		int code = in.read();
		switch(code) {
			case FastObjectOutput.NULL :
				return null;
			case FastObjectOutput.STANDARD :
				// This is OK, perhaps we just recently changed this object to now be a fast class
				return (FastExternalizable)in.readObject();
			case -1 :
				throw new EOFException();
			default :
				return readFastObject(code);
		}
	}

	/**
	 * Reads a {@link FastExternalizable} object from the stream.
	 *
	 * @see  #writeFastObject(java.io.ObjectOutput, com.aoindustries.io.FastExternalizable)
	 */
	private FastExternalizable readFastObject(int code) throws IOException, ClassNotFoundException {
		assert code >= FastObjectOutput.FAST_NEW;
		// Resolve class (as lastClass) by code
		switch(code) {
			case FastObjectOutput.FAST_SAME :
			{
				if(lastClass == null) throw new StreamCorruptedException("lastClass is null");
				break;
			}
			case FastObjectOutput.FAST_NEW :
			{
				classesById.add(lastClass = Class.forName(in.readUTF()));
				serialVersionUIDsById.add(lastSerialVersionUID = in.readLong());
				nextClassId++;
				break;
			}
			case FastObjectOutput.FAST_SEEN_SHORT :
			{
				int offset = in.readShort() & 0xffff;
				int classId = offset + (255 - FastObjectOutput.FAST_SEEN_INT);
				if(classId >= nextClassId) throw new StreamCorruptedException("Class ID not already in steam: " + classId);
				lastClass = classesById.get(classId);
				lastSerialVersionUID = serialVersionUIDsById.get(classId);
				break;
			}
			case FastObjectOutput.FAST_SEEN_INT :
			{
				int classId = in.readInt();
				if(classId >= nextClassId) throw new StreamCorruptedException("Class ID not already in steam: " + classId);
				lastClass = classesById.get(classId);
				lastSerialVersionUID = serialVersionUIDsById.get(classId);
				break;
			}
			default :
			{
				assert code > FastObjectOutput.FAST_SEEN_INT;
				int classId = code - (FastObjectOutput.FAST_SEEN_INT + 1);
				if(classId >= nextClassId) throw new StreamCorruptedException("Class ID not already in steam: " + classId);
				lastClass = classesById.get(classId);
				lastSerialVersionUID = serialVersionUIDsById.get(classId);
			}
		}
		try {
			FastExternalizable obj = (FastExternalizable)lastClass.newInstance();
			long actualSerialVersionUID = obj.getSerialVersionUID();
			if(lastSerialVersionUID != actualSerialVersionUID) throw new InvalidClassException(lastClass.getName(), "Mismatched serialVersionUID: expected " + lastSerialVersionUID + ", got " + actualSerialVersionUID);
			obj.readExternal(this);
			if(obj instanceof ObjectInputValidation) ((ObjectInputValidation)obj).validateObject();
			return obj;
		} catch(InstantiationException exc) {
			InvalidClassException newExc = new InvalidClassException("InstantiationException");
			newExc.initCause(exc);
			throw newExc;
		} catch(IllegalAccessException exc) {
			InvalidClassException newExc = new InvalidClassException("IllegalAccessException");
			newExc.initCause(exc);
			throw newExc;
		}
	}

	/**
	 * Reads a fast serialized {@link String} from the stream.
	 *
	 * @see  #writeFastUTF(java.io.ObjectOutput, com.aoindustries.io.FastExternalizable)
	 */
	public String readFastUTF() throws IOException, ClassNotFoundException {
		int code = in.read();
		// Resolve string by code
		switch(code) {
			case FastObjectOutput.NULL :
			{
				return null;
			}
			case FastObjectOutput.STANDARD :
			{
				throw new IOException("Unexpected code: " + code);
			}
			case -1 :
			{
				throw new EOFException();
			}
			case FastObjectOutput.FAST_SAME :
			{
				if(lastString == null) throw new StreamCorruptedException("lastString is null");
				return lastString;
			}
			case FastObjectOutput.FAST_NEW :
			{
				lastString = in.readUTF();
				stringsById.add(lastString);
				nextStringId++;
				return lastString;
			}
			case FastObjectOutput.FAST_SEEN_SHORT :
			{
				int offset = in.readShort() & 0xffff;
				int stringId = offset + (255 - FastObjectOutput.FAST_SEEN_INT);
				if(stringId >= nextStringId) throw new StreamCorruptedException("String ID not already in steam: " + stringId);
				return lastString = stringsById.get(stringId);
			}
			case FastObjectOutput.FAST_SEEN_INT :
			{
				int stringId = in.readInt();
				if(stringId >= nextStringId) throw new StreamCorruptedException("String ID not already in steam: " + stringId);
				return lastString = stringsById.get(stringId);
			}
			default :
			{
				assert code > FastObjectOutput.FAST_SEEN_INT;
				int stringId = code - (FastObjectOutput.FAST_SEEN_INT + 1);
				if(stringId >= nextStringId) throw new StreamCorruptedException("String ID not already in steam: " + stringId);
				return lastString = stringsById.get(stringId);
			}
		}
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		in.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		in.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return in.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public String readLine() throws IOException {
		return in.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}
}
