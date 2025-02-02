/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2012, 2013, 2016, 2017, 2019, 2020, 2021, 2022, 2023, 2024, 2025  AO Industries, Inc.
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

package com.aoapps.lang.zip;

import com.aoapps.lang.io.FileUtils;
import com.aoapps.lang.io.IoUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZIP file utilities.
 */
public final class ZipUtils {

  /** Make no instances. */
  private ZipUtils() {
    throw new AssertionError();
  }

  private static final Comparator<File> reverseFileComparator = (File f1, File f2) -> {
    try {
      String path1 = f1.getCanonicalPath();
      String path2 = f2.getCanonicalPath();
      return path2.compareTo(path1);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  };

  /**
   * Gets the time for a ZipEntry, converting from UTC as stored in the ZIP
   * entry to make times correct between time zones.
   *
   * @return  the time assuming UTC zone or {@code null} if not specified.
   *
   * @see #setTimeUtc(java.util.zip.ZipEntry, long)
   */
  // Note: ao-ant-utils:ZipTimestampMerge.getTimeUtc is based on this
  public static Optional<Long> getTimeUtc(ZipEntry entry) throws ZipException {
    long time = entry.getTime();
    if (time == -1) {
      return Optional.empty();
    } else {
      long offset = time + TimeZone.getDefault().getOffset(time);
      if (offset == -1) {
        throw new ZipException("Time is -1 after offset: " + entry.getName());
      }
      return Optional.of(offset);
    }
  }

  /**
   * Sets the time for a ZipEntry, converting to UTC while storing to the ZIP
   * entry to make times correct between time zones.  The actual time stored
   * may be rounded to the nearest two-second interval.
   *
   * @see #getTimeUtc(java.util.zip.ZipEntry)
   */
  // Note: ao-ant-tasks:ZipTimestampMerge.getDosTimeDate is based on this
  public static void setTimeUtc(ZipEntry entry, long time) throws ZipException {
    long offset = time - TimeZone.getDefault().getOffset(time);
    if (offset == -1) {
      throw new ZipException("Time is -1 after offset: " + entry);
    }
    entry.setTime(offset);
  }

  /**
   * Gets the time for a ZipEntry, converting from UTC as stored in the ZIP
   * entry to make times correct between time zones.
   *
   * @return  the time assuming UTC zone or {@code -1} if not specified.
   *
   * @see #setZipEntryTime(java.util.zip.ZipEntry, long)
   *
   * @deprecated Please use {@link #getTimeUtc(java.util.zip.ZipEntry)} instead.
   */
  @Deprecated
  public static long getZipEntryTime(ZipEntry entry) {
    try {
      return getTimeUtc(entry).orElse(-1L);
    } catch (ZipException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Sets the time for a ZipEntry, converting to UTC while storing to the ZIP
   * entry to make times correct between time zones.  The actual time stored
   * may be rounded to the nearest two-second interval.
   *
   * @see #getZipEntryTime(java.util.zip.ZipEntry)
   *
   * @deprecated Please use {@link #setTimeUtc(java.util.zip.ZipEntry, long)} instead.
   */
  @Deprecated
  public static void setZipEntryTime(ZipEntry entry, long time) {
    try {
      setTimeUtc(entry, time);
    } catch (ZipException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Gets the creation time for a ZipEntry, converting from UTC as stored in the ZIP
   * entry to make times correct between time zones.
   *
   * @return  the creation time assuming UTC zone or {@code null} if not specified.
   *
   * @see #setCreationTimeUtc(java.util.zip.ZipEntry, long)
   */
  public static Optional<Long> getCreationTimeUtc(ZipEntry entry) {
    FileTime creationTime = entry.getCreationTime();
    if (creationTime == null) {
      return Optional.empty();
    } else {
      long millis = creationTime.toMillis();
      return Optional.of(millis + TimeZone.getDefault().getOffset(millis));
    }
  }

  /**
   * Sets the creation time for a ZipEntry, converting to UTC while storing to the ZIP
   * entry to make times correct between time zones.
   *
   * @see #getCreationTimeUtc(java.util.zip.ZipEntry)
   */
  public static void setCreationTimeUtc(ZipEntry entry, long creationTime) {
    entry.setCreationTime(FileTime.fromMillis(creationTime - TimeZone.getDefault().getOffset(creationTime)));
  }

  /**
   * Gets the last access time for a ZipEntry, converting from UTC as stored in the ZIP
   * entry to make times correct between time zones.
   *
   * @return  the last access time assuming UTC zone or {@code null} if not specified.
   *
   * @see #setLastAccessTimeUtc(java.util.zip.ZipEntry, long)
   */
  public static Optional<Long> getLastAccessTimeUtc(ZipEntry entry) {
    FileTime lastAccess = entry.getLastAccessTime();
    if (lastAccess == null) {
      return Optional.empty();
    } else {
      long millis = lastAccess.toMillis();
      return Optional.of(millis + TimeZone.getDefault().getOffset(millis));
    }
  }

  /**
   * Sets the last access time for a ZipEntry, converting to UTC while storing to the ZIP
   * entry to make times correct between time zones.
   *
   * @see #getLastAccessTimeUtc(java.util.zip.ZipEntry)
   */
  public static void setLastAccessTimeUtc(ZipEntry entry, long lastAccessTime) {
    entry.setCreationTime(FileTime.fromMillis(lastAccessTime - TimeZone.getDefault().getOffset(lastAccessTime)));
  }

  /**
   * Gets the last modified time for a ZipEntry, converting from UTC as stored in the ZIP
   * entry to make times correct between time zones.
   *
   * @return  the last modified time assuming UTC zone or {@code null} if not specified.
   *
   * @see #setLastModifiedTimeUtc(java.util.zip.ZipEntry, long)
   */
  public static Optional<Long> getLastModifiedTimeUtc(ZipEntry entry) {
    FileTime lastModifiedTime = entry.getLastModifiedTime();
    if (lastModifiedTime == null) {
      return Optional.empty();
    } else {
      long millis = lastModifiedTime.toMillis();
      return Optional.of(millis + TimeZone.getDefault().getOffset(millis));
    }
  }

  /**
   * Sets the last modified time for a ZipEntry, converting to UTC while storing to the ZIP
   * entry to make times correct between time zones.
   *
   * @see #getLastModifiedTimeUtc(java.util.zip.ZipEntry)
   */
  public static void setLastModifiedTimeUtc(ZipEntry entry, long lastModifiedTime) {
    entry.setCreationTime(FileTime.fromMillis(lastModifiedTime - TimeZone.getDefault().getOffset(lastModifiedTime)));
  }

  /**
   * Recursively packages a directory into a file.
   */
  public static void createZipFile(File sourceDirectory, File zipFile) throws IOException {
    try (OutputStream out = new FileOutputStream(zipFile)) {
      createZipFile(sourceDirectory, out);
    }
  }

  /**
   * Recursively packages a directory into an output stream.
   */
  public static void createZipFile(File sourceDirectory, OutputStream out) throws IOException {
    ZipOutputStream zipOut = new ZipOutputStream(out);
    try {
      createZipFile(sourceDirectory, zipOut);
    } finally {
      zipOut.finish();
    }
  }

  /**
   * Recursively packages a directory into a ZIP output stream.
   */
  public static void createZipFile(File sourceDirectory, ZipOutputStream zipOut) throws IOException {
    File[] list = sourceDirectory.listFiles();
    if (list != null) {
      for (File file : list) {
        createZipFile(file, zipOut, "");
      }
    }
  }

  /**
   * Recursively packages a directory into a ZIP output stream.
   */
  public static void createZipFile(File file, ZipOutputStream zipOut, String path) throws IOException {
    final String filename = file.getName();
    final String newPath = path.isEmpty() ? filename : (path + '/' + filename);
    if (file.isDirectory()) {
      // Add directory
      ZipEntry zipEntry = new ZipEntry(newPath + '/');
      setZipEntryTime(zipEntry, file.lastModified());
      zipOut.putNextEntry(zipEntry);
      zipOut.closeEntry();
      // Add all children
      File[] list = file.listFiles();
      if (list != null) {
        for (File child : list) {
          createZipFile(child, zipOut, newPath);
        }
      }
    } else {
      ZipEntry zipEntry = new ZipEntry(newPath);
      setZipEntryTime(zipEntry, file.lastModified());
      zipOut.putNextEntry(zipEntry);
      try {
        FileUtils.copy(file, zipOut);
      } finally {
        zipOut.closeEntry();
      }
    }
  }

  /**
   * Unzips the provided file to the given destination directory.
   */
  public static void unzip(File sourceFile, File destination) throws IOException {
    unzip(sourceFile, "", destination, null);
  }

  /**
   * Unzips the provided file to the given destination directory.
   *
   * @param  sourceFile  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *                     against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *                     for details.
   */
  public static void unzip(File sourceFile, String sourcePrefix, File destination, ZipEntryFilter filter) throws IOException {
    // Destination directory must exist
    if (!destination.isDirectory()) {
      throw new IOException("Not a directory: " + destination.getPath());
    }
    // Add trailing / to sourcePrefix if missing
    if (!sourcePrefix.isEmpty() && !sourcePrefix.endsWith("/")) {
      sourcePrefix += "/";
    }
    try (ZipFile zipFile = new ZipFile(sourceFile)) {
      SortedMap<File, Long> directoryModifyTimes = new TreeMap<>(reverseFileComparator);

      // Pass one: create directories and files
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith(sourcePrefix)) {
          name = name.substring(sourcePrefix.length());
          if (!name.isEmpty()) {
            if (filter == null || filter.accept(entry)) {
              long entryTime = getZipEntryTime(entry);
              if (entry.isDirectory()) {
                name = name.substring(0, name.length() - 1); // Strip trailing '/'
                //System.out.println("Directory: " + name);
                File directory = new File(destination, name);
                if (!directory.exists()) {
                  Files.createDirectories(directory.toPath());
                }
                if (entryTime != -1) {
                  directoryModifyTimes.put(directory, entryTime);
                }
              } else {
                //System.out.println("File: " + name);
                File file = new File(destination, name);
                File directory = file.getParentFile();
                if (!directory.exists()) {
                  Files.createDirectories(directory.toPath());
                }
                if (file.exists()) {
                  throw new IOException("File exists: " + file.getPath());
                }
                try (InputStream in = zipFile.getInputStream(entry)) {
                  long copyBytes = FileUtils.copyToFile(in, file);
                  long size = entry.getSize();
                  if (size != -1 && copyBytes != size) {
                    throw new IOException("copyBytes != size: " + copyBytes + " != " + size);
                  }
                  if (entryTime != -1) {
                    FileUtils.setLastModified(file, entryTime);
                  }
                }
              }
            }
          }
        }
      }

      // Pass two: go backwards through directories, setting the modification times
      for (Map.Entry<File, Long> entry : directoryModifyTimes.entrySet()) {
        //System.out.println("File: " + entry.getKey() + ", mtime = " + entry.getValue());
        FileUtils.setLastModified(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Combine contents of all ZIP files while unzipping, only allowing duplicates
   * where the file contents are equal.  When duplicates are found, uses the most
   * recent modification time.
   *
   * @param  zipFiles  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *                   against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *                   for details.
   */
  public static void mergeUnzip(File destination, File ... zipFiles) throws IOException {
    mergeUnzip(null, destination, zipFiles);
  }

  /**
   * Combine contents of all ZIP files while unzipping, only allowing duplicates
   * where the file contents are equal.  When duplicates are found, uses the most
   * recent modification time.
   *
   * @param  zipFiles  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *                   against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *                   for details.
   */
  @SuppressWarnings("deprecation")
  public static void mergeUnzip(ZipEntryFilter filter, File destination, File ... zipFiles) throws IOException {
    if (zipFiles.length > 0) {
      if (zipFiles.length == 1) {
        unzip(zipFiles[0], "", destination, filter);
      } else {
        throw new com.aoapps.lang.exception.NotImplementedException("Implement merge feature when first needed");
      }
    }
  }

  /**
   * Copies all non-directory entries.
   *
   * @param  file  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *               against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *               for details.
   */
  public static void copyEntries(File file, ZipOutputStream zipOut) throws IOException {
    copyEntries(file, zipOut, null);
  }

  /**
   * Copies all non-directory entries.
   *
   * @param  file  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *               against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *               for details.
   */
  public static void copyEntries(File file, ZipOutputStream zipOut, ZipEntryFilter filter) throws IOException {
    try (ZipFile zipFile = new ZipFile(file)) {
      copyEntries(zipFile, zipOut, filter);
    }
  }

  /**
   * Copies all non-directory entries.
   *
   * @param  zipFile  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *                  against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *                  for details.
   */
  public static void copyEntries(ZipFile zipFile, ZipOutputStream zipOut) throws IOException {
    copyEntries(zipFile, zipOut, null);
  }

  /**
   * Copies all entries.
   *
   * @param  zipFile  Please take caution when extracting untrusted ZIP files.  This method does nothing to protect
   *                  against ZIP bombs.  Please see <a href="https://rules.sonarsource.com/java/RSPEC-5042">Expanding archive files without controlling resource consumption is security-sensitive</a>
   *                  for details.
   */
  public static void copyEntries(ZipFile zipFile, ZipOutputStream zipOut, ZipEntryFilter filter) throws IOException {
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (filter == null || filter.accept(entry)) {
        ZipEntry newEntry = new ZipEntry(entry.getName());
        long time = entry.getTime();
        if (time != -1) {
          newEntry.setTime(time);
        }
        zipOut.putNextEntry(newEntry);
        try {
          try (InputStream in = zipFile.getInputStream(entry)) {
            IoUtils.copy(in, zipOut);
          }
        } finally {
          zipOut.closeEntry();
        }
      }
    }
  }
}
