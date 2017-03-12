/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2012, 2013, 2015, 2016, 2017  AO Industries, Inc.
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

import com.aoindustries.lang.NullArgumentException;
import com.aoindustries.util.BufferManager;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * I/O utilities.
 */
final public class IoUtils {

	/**
	 * Make no instances.
	 */
	private IoUtils() {}

	/**
	 * copies without flush.
	 *
	 * @see #copy(java.io.InputStream, java.io.OutputStream, boolean)
	 */
	public static long copy(InputStream in, OutputStream out) throws IOException {
		return copy(in, out, false);
	}

	/**
	 * Copies all information from one stream to another.  Internally reuses thread-local
	 * buffers to avoid initial buffer zeroing cost and later garbage collection overhead.
	 *
	 * @return  the number of bytes copied
	 *
	 * @see  BufferManager#getBytes()
	 */
	public static long copy(InputStream in, OutputStream out, boolean flush) throws IOException {
		byte[] buff = BufferManager.getBytes();
		try {
			long totalBytes = 0;
			int numBytes;
			while((numBytes = in.read(buff, 0, BufferManager.BUFFER_SIZE))!=-1) {
				out.write(buff, 0, numBytes);
				if(flush) out.flush();
				totalBytes += numBytes;
			}
			return totalBytes;
		} finally {
			BufferManager.release(buff, false);
		}
	}

	/**
	 * Copies all information from one stream to another.  Internally reuses thread-local
	 * buffers to avoid initial buffer zeroing cost and later garbage collection overhead.
	 *
	 * @return  the number of bytes copied
	 *
	 * @see  BufferManager#getChars()
	 */
	public static long copy(Reader in, Writer out) throws IOException {
		char[] buff = BufferManager.getChars();
		try {
			long totalChars = 0;
			int numChars;
			while((numChars = in.read(buff, 0, BufferManager.BUFFER_SIZE))!=-1) {
				out.write(buff, 0, numChars);
				totalChars += numChars;
			}
			return totalChars;
		} finally {
			BufferManager.release(buff, false);
		}
	}

	/**
	 * Copies all information from one stream to an appendable.
	 *
	 * @return  the number of bytes copied
	 *
	 * @see  BufferManager#getChars()
	 */
	public static long copy(Reader in, Appendable out) throws IOException {
		if(in == null) throw new NullArgumentException("in");
		if(out == null) throw new NullArgumentException("out");
		char[] buff = BufferManager.getChars();
		try {
			long totalChars = 0;
			int numChars;
			while((numChars = in.read(buff, 0, BufferManager.BUFFER_SIZE))!=-1) {
				out.append(new String(buff, 0, numChars));
				totalChars += numChars;
			}
			return totalChars;
		} finally {
			BufferManager.release(buff, false);
		}
	}

	/**
	 * Copies all information from one stream to another.  Internally reuses thread-local
	 * buffers to avoid initial buffer zeroing cost and later garbage collection overhead.
	 *
	 * @return  the number of bytes copied
	 *
	 * @see  BufferManager#getChars()
	 */
	public static long copy(Reader in, StringBuilder out) throws IOException {
		char[] buff = BufferManager.getChars();
		try {
			long totalChars = 0;
			int numChars;
			while((numChars = in.read(buff, 0, BufferManager.BUFFER_SIZE))!=-1) {
				out.append(buff, 0, numChars);
				totalChars += numChars;
			}
			return totalChars;
		} finally {
			BufferManager.release(buff, false);
		}
	}

	/**
	 * readFully for any stream.
	 */
	// @ThreadSafe
	public static void readFully(InputStream in, byte[] buffer) throws IOException {
		readFully(in, buffer, 0, buffer.length);
	}

	/**
	 * readFully for any stream.
	 */
	// @ThreadSafe
	public static void readFully(InputStream in, byte[] buffer, int off, int len) throws IOException {
		while(len>0) {
			int count = in.read(buffer, off, len);
			if(count==-1) throw new EOFException();
			off += count;
			len -= count;
		}
	}

	/**
	 * Reads an input stream fully (to end of stream), returning a byte[] of the content read.
	 */
	public static byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IoUtils.copy(in, bout);
		return bout.toByteArray();
	}

	/**
	 * Reads a reader fully (to end of stream), returning a String of the content read.
	 */
	public static String readFully(Reader in) throws IOException {
		StringBuilder sb = new StringBuilder();
		IoUtils.copy(in, sb);
		return sb.toString();
	}

	/**
	 * Compares the contents retrieved from an InputStream to the provided contents.
	 *
	 * @return true  when the contents exactly match
	 */
	public static boolean contentEquals(InputStream in, byte[] contents) throws IOException {
		final int contentLen = contents.length;
		final byte[] buff = BufferManager.getBytes();
		try {
			int readPos = 0;
			while(readPos<contentLen) {
				int bytesRemaining = contentLen - readPos;
				int bytesRead = in.read(buff, 0, bytesRemaining > BufferManager.BUFFER_SIZE ? BufferManager.BUFFER_SIZE : bytesRemaining);
				if(bytesRead==-1) return false; // End of file
				int i=0;
				while(i<bytesRead) {
					if(buff[i++]!=contents[readPos++]) return false;
				}
			}
			// Next read must be end of file - otherwise file content longer than contents.
			return in.read()==-1;
		} finally {
			BufferManager.release(buff, false);
		}
	}

	// <editor-fold desc="byte[] manipulation methods">
	// @ThreadSafe
	public static void charToBuffer(char ch, byte[] ioBuffer) {
		ioBuffer[0] = (byte)(ch >>> 8);
		ioBuffer[1] = (byte)ch;
	}

	// @ThreadSafe
	public static void charToBuffer(char ch, byte[] ioBuffer, int off) {
		ioBuffer[off] = (byte)(ch >>> 8);
		ioBuffer[off+1] = (byte)ch;
	}

	// @ThreadSafe
	public static char bufferToChar(byte[] ioBuffer) {
		return
			(char)(
				(ioBuffer[0] << 8)
				| (ioBuffer[1]&255)
			)
		;
	}

	// @ThreadSafe
	public static char bufferToChar(byte[] ioBuffer, int off) {
		return
			(char)(
				(ioBuffer[off+0] << 8)
				| (ioBuffer[off+1]&255)
			)
		;
	}

	// @ThreadSafe
	public static void shortToBuffer(short s, byte[] ioBuffer) {
		ioBuffer[0] = (byte)(s >>> 8);
		ioBuffer[1] = (byte)s;
	}

	// @ThreadSafe
	public static void shortToBuffer(short s, byte[] ioBuffer, int off) {
		ioBuffer[off] = (byte)(s >>> 8);
		ioBuffer[off+1] = (byte)s;
	}

	// @ThreadSafe
	public static short bufferToShort(byte[] ioBuffer) {
		return
			(short)(
				(ioBuffer[0] << 8)
				| (ioBuffer[1]&255)
			)
		;
	}

	// @ThreadSafe
	public static short bufferToShort(byte[] ioBuffer, int off) {
		return
			(short)(
				(ioBuffer[off+0] << 8)
				| (ioBuffer[off+1]&255)
			)
		;
	}

	// @ThreadSafe
	public static void intToBuffer(int i, byte[] ioBuffer) {
		ioBuffer[0] = (byte)(i >>> 24);
		ioBuffer[1] = (byte)(i >>> 16);
		ioBuffer[2] = (byte)(i >>> 8);
		ioBuffer[3] = (byte)i;
	}

	// @ThreadSafe
	public static void intToBuffer(int i, byte[] ioBuffer, int off) {
		ioBuffer[off] = (byte)(i >>> 24);
		ioBuffer[off+1] = (byte)(i >>> 16);
		ioBuffer[off+2] = (byte)(i >>> 8);
		ioBuffer[off+3] = (byte)i;
	}

	// @ThreadSafe
	public static int bufferToInt(byte[] ioBuffer) {
		return
			  (ioBuffer[0] << 24)
			+ ((ioBuffer[1]&255) << 16)
			+ ((ioBuffer[2]&255) << 8)
			+ (ioBuffer[3]&255)
		;
	}

	// @ThreadSafe
	public static int bufferToInt(byte[] ioBuffer, int off) {
		return
			  (ioBuffer[off] << 24)
			+ ((ioBuffer[off+1]&255) << 16)
			+ ((ioBuffer[off+2]&255) << 8)
			+ (ioBuffer[off+3]&255)
		;
	}

	// @ThreadSafe
	public static void longToBuffer(long l, byte[] ioBuffer) {
		ioBuffer[0] = (byte)(l >>> 56);
		ioBuffer[1] = (byte)(l >>> 48);
		ioBuffer[2] = (byte)(l >>> 40);
		ioBuffer[3] = (byte)(l >>> 32);
		ioBuffer[4] = (byte)(l >>> 24);
		ioBuffer[5] = (byte)(l >>> 16);
		ioBuffer[6] = (byte)(l >>> 8);
		ioBuffer[7] = (byte)l;
	}

	// @ThreadSafe
	public static void longToBuffer(long l, byte[] ioBuffer, int off) {
		ioBuffer[off] = (byte)(l >>> 56);
		ioBuffer[off+1] = (byte)(l >>> 48);
		ioBuffer[off+2] = (byte)(l >>> 40);
		ioBuffer[off+3] = (byte)(l >>> 32);
		ioBuffer[off+4] = (byte)(l >>> 24);
		ioBuffer[off+5] = (byte)(l >>> 16);
		ioBuffer[off+6] = (byte)(l >>> 8);
		ioBuffer[off+7] = (byte)l;
	}

	// @ThreadSafe
	public static long bufferToLong(byte[] ioBuffer) {
		return
			  ((ioBuffer[0]&255L) << 56)
			+ ((ioBuffer[1]&255L) << 48)
			+ ((ioBuffer[2]&255L) << 40)
			+ ((ioBuffer[3]&255L) << 32)
			+ ((ioBuffer[4]&255L) << 24)
			+ ((ioBuffer[5]&255L) << 16)
			+ ((ioBuffer[6]&255L) << 8)
			+ (ioBuffer[7]&255L)
		;
	}

	// @ThreadSafe
	public static long bufferToLong(byte[] ioBuffer, int off) {
		return
			  ((ioBuffer[off  ]&255L) << 56)
			+ ((ioBuffer[off+1]&255L) << 48)
			+ ((ioBuffer[off+2]&255L) << 40)
			+ ((ioBuffer[off+3]&255L) << 32)
			+ ((ioBuffer[off+4]&255L) << 24)
			+ ((ioBuffer[off+5]&255L) << 16)
			+ ((ioBuffer[off+6]&255L) << 8)
			+ (ioBuffer[off+7]&255L)
		;
	}
	// </editor-fold>
}