/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2013, 2016, 2017, 2019, 2021, 2022, 2024  AO Industries, Inc.
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities to write {@link FastExternalizable}, {@link Externalizable}, and {@link Serializable} objects.
 *
 * <p>When multiple objects are being written, this avoids the repetitive writing of classnames and serialVersionUIDs.</p>
 *
 * @author  AO Industries, Inc.
 */
public class FastObjectOutput implements ObjectOutput {

  private static final ThreadLocal<FastObjectOutput> threadFastObjectOutput = new ThreadLocal<>();

  /**
   * Gets the wrapper for the provided {@link ObjectOutput}, creating if needed.
   * To avoid memory leaks, it must also be {@link #unwrap() unwrapped} in a finally block.
   *
   * <p>TODO: Can {@link FastObjectOutput} itself implement {@link AutoCloseable} for {@link #unwrap()}?
   *       Maybe this means it no longer implements {@link ObjectOutput} directly?</p>
   */
  public static FastObjectOutput wrap(ObjectOutput out) throws IOException {
    FastObjectOutput fastOut;
    if (out instanceof FastObjectOutput) {
      fastOut = (FastObjectOutput) out;
    } else {
      fastOut = threadFastObjectOutput.get();
      if (fastOut == null) {
        threadFastObjectOutput.set(fastOut = new FastObjectOutput(out));
      } else {
        // Must be same as previously used value
        if (out != fastOut.out) {
          throw new IOException("ObjectOutput changed unexpectedly");
        }
      }
    }
    fastOut.incrementWrapCount();
    return fastOut;
  }

  /**
   * The object is null.
   */
  static final int NULL = 0;

  /**
   * The object uses standard serialization.
   */
  static final int STANDARD = NULL + 1;

  /**
   * The object is of a previously unseen class.
   */
  static final int FAST_NEW = STANDARD + 1;

  /**
   * The object is the same class as the previous object.
   */
  static final int FAST_SAME = FAST_NEW + 1;

  /**
   * The object is of a class that has already been seen, and uses the next two bytes as its class ID - (255 - FAST_SEEN_INT).
   */
  static final int FAST_SEEN_SHORT = FAST_SAME + 1;

  /**
   * The object is of a class that has already been seen, and uses the next four bytes as its class ID.
   */
  static final int FAST_SEEN_INT = FAST_SEEN_SHORT + 1;
  // The remaining values are for direct already seen classes between 0 <= ID < (255-FAST_SEEN_INT)

  private final ObjectOutput out;
  private int wrapCount;

  /**
   * A mapping of classes to generated IDs.
   */
  private final Map<Class<?>, Integer> classesMap = new HashMap<>();
  private int nextClassId;
  private Class<?> lastClass;

  /**
   * A mapping of fast string IDs.
   */
  private final Map<String, Integer> stringsMap = new HashMap<>();
  private int nextStringId;
  private String lastString;

  private FastObjectOutput(ObjectOutput out) {
    this.out = out;
  }

  private void incrementWrapCount() throws IOException {
    if (wrapCount == Integer.MAX_VALUE) {
      throw new IOException("Maximum wrap count reached.");
    }
    wrapCount++;
  }

  /**
   * Unwraps the object output.
   *
   * @throws  IllegalStateException  if not wrapped
   */
  public void unwrap() throws IllegalStateException {
    assert wrapCount >= 0;
    if (wrapCount == 0) {
      throw new IllegalStateException("Not wrapped");
    }
    wrapCount--;
    if (wrapCount == 0) {
      threadFastObjectOutput.remove();
    }
  }

  /**
   * Writes the provided object in the most efficient manner possible, with no object graph tracking (if possible).
   * This allows individual objects to switch between {@link FastExternalizable} and standard serialization without calling
   * code needing to know the difference.
   *
   * <p>If the object is {@code null}, writes a single byte of {@link #NULL}.</p>
   *
   * <p>If the object is {@link FastExternalizable}, calls
   * {@link #writeFastObject(com.aoapps.lang.io.FastExternalizable)}.</p>
   *
   * <p>Otherwise, writes {@link #STANDARD} and then uses standard Java serialization.</p>
   *
   * @see  FastObjectInput#readObject()
   */
  @Override
  public void writeObject(Object obj) throws IOException {
    if (obj == null) {
      out.write(NULL);
    } else if (obj instanceof FastExternalizable) {
      writeFastObject((FastExternalizable) obj);
    } else {
      out.write(STANDARD);
      out.writeObject(obj);
    }
  }

  /**
   * Writes a {@link FastExternalizable} object to the provided stream, supporting {@code null} values.
   *
   * @see  FastObjectInput#readFastObject(int)
   */
  protected void writeFastObject(FastExternalizable obj) throws IOException {
    if (obj == null) {
      out.write(NULL);
    } else {
      Class<?> clazz = obj.getClass();
      if (clazz == lastClass) {
        out.write(FAST_SAME);
      } else {
        Integer classIdObj = classesMap.get(clazz);
        if (classIdObj == null) {
          classesMap.put(clazz, nextClassId++);
          out.write(FAST_NEW);
          // TODO: Should this do writeFastUTF here to share string pools?
          out.writeUTF(clazz.getName());
          out.writeLong(obj.getSerialVersionUID());
        } else {
          int classId = classIdObj;
          if (classId < (255 - FAST_SEEN_INT)) {
            // 0 through 250
            int code = classId + (FAST_SEEN_INT + 1);
            assert code > FAST_SEEN_INT;
            assert code <= 255;
            out.write(code);
          } else if (classId <= (65536 + (255 - FAST_SEEN_INT))) {
            // 251 through 65786
            out.write(FAST_SEEN_SHORT);
            int offset = classId - (255 - FAST_SEEN_INT);
            assert offset >= 0;
            assert offset <= 65535;
            out.writeShort(offset);
          } else {
            out.write(FAST_SEEN_INT); // 65787 through Integer.MAX_VALUE, no offset
            assert classId > (65536 + (255 - FAST_SEEN_INT));
            out.writeInt(classId);
          }
        }
        lastClass = clazz;
      }
      obj.writeExternal(this);
    }
  }

  /**
   * Writes a {@link String} to the output, not writing any duplicates.
   * Supports {@code null}.
   *
   * <p>TODO: Any benefit to string prefix compression, like in CompressedDataOutput?</p>
   */
  public void writeFastUTF(String value) throws IOException {
    if (value == null) {
      out.write(NULL);
    } else {
      if (value.equals(lastString)) {
        out.write(FAST_SAME);
      } else {
        Integer stringIdObj = stringsMap.get(value);
        if (stringIdObj == null) {
          stringsMap.put(value, nextStringId++);
          out.write(FAST_NEW);
          out.writeUTF(value);
        } else {
          int stringId = stringIdObj;
          if (stringId < (255 - FAST_SEEN_INT)) {
            // 0 through 250
            int code = stringId + (FAST_SEEN_INT + 1);
            assert code > FAST_SEEN_INT;
            assert code <= 255;
            out.write(code);
          } else if (stringId <= (65536 + (255 - FAST_SEEN_INT))) {
            // 251 through 65786
            out.write(FAST_SEEN_SHORT);
            int offset = stringId - (255 - FAST_SEEN_INT);
            assert offset >= 0;
            assert offset <= 65535;
            out.writeShort(offset);
          } else {
            out.write(FAST_SEEN_INT); // 65787 through Integer.MAX_VALUE, no offset
            assert stringId > (65536 + (255 - FAST_SEEN_INT));
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
