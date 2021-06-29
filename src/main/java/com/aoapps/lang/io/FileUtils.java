/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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

import com.aoapps.lang.util.BufferManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * File utilities.
 */
final public class FileUtils {

	/**
	 * Make no instances.
	 */
	private FileUtils() {}

	/**
	 * Deletes the provided file, throwing IOException if unsuccessful.
	 *
	 * @deprecated  Please use {@link Files#delete(java.nio.file.Path)}
	 */
	@Deprecated
	public static void delete(File file) throws IOException {
		Files.delete(file.toPath());
	}

	/**
	 * Recursively deletes the provided file, being careful to not follow symbolic links (but there are still unavoidable race conditions).
	 *
	 * @deprecated  Please use {@link org.apache.commons.io.FileUtils#deleteDirectory(java.io.File)} or
	 *              {@link org.apache.commons.io.FileUtils#forceDelete(java.io.File)}from
	 *              <a href="https://commons.apache.org/proper/commons-io/">Apache Commons IO</a>.
	 */
	@Deprecated
	// Note: This is copied to TempFile to avoid dependency
	public static void deleteRecursive(File file) throws IOException {
		Path deleteMe = file.toPath();
		Files.walkFileTree(
			deleteMe,
			new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if(exc != null) throw exc;
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			}
		);
		assert !Files.exists(deleteMe, LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * Compares the contents of a file to the provided array.
	 */
	public static boolean contentEquals(File file, byte[] contents) throws IOException {
		{
			final long length = file.length();
			if(length>Integer.MAX_VALUE) return false;
			// Be careful about file.length() returning zero on error - always read file for zero case - no shortcut.
			if(length!=0 && length!=contents.length) return false;
		}
		try (InputStream in = new FileInputStream(file)) {
			return IoUtils.contentEquals(in, contents);
		}
	}

	/**
	 * Compares the contents of two files, not supporting directories.
	 */
	public static boolean contentEquals(File file1, File file2) throws IOException {
		long len1 = file1.length();
		long len2 = file2.length();
		// Read file when zero length
		if(len1!=0 && len2!=0 && len1!=len2) return false;
		try (
			InputStream in1 = new BufferedInputStream(new FileInputStream(file1));
			InputStream in2 = new BufferedInputStream(new FileInputStream(file2))
		) {
			while(true) {
				int b1 = in1.read();
				int b2 = in2.read();
				if(b1!=b2) return false;
				if(b1==-1) break;
			}
			return true;
		}
	}

	/**
	 * Computes a hash code of the file contents, consistent with Arrays.hashCode(byte[]).
	 *
	 * @see  Arrays#hashCode(byte[])
	 */
	public static int contentHashCode(File file) throws IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			int result = 1;
			int readVal;
			while((readVal = in.read()) != -1) {
				result = 31 * result + (byte)readVal;
			}
			return result;
		}
	}

	/**
	 * Creates a temporary directory.
	 *
	 * @deprecated  Please use {@link Files#createTempDirectory(java.lang.String, java.nio.file.attribute.FileAttribute...)},
	 *              which does not suffer from any race conditions.
	 */
	@Deprecated
	public static File createTempDirectory(String prefix, String suffix) throws IOException {
		return createTempDirectory(prefix, suffix, null);
	}

	/**
	 * Creates a temporary directory.
	 *
	 * @deprecated  Please use {@link Files#createTempDirectory(java.nio.file.Path, java.lang.String, java.nio.file.attribute.FileAttribute...)},
	 *              which does not suffer from any race conditions.
	 */
	@Deprecated
	public static File createTempDirectory(String prefix, String suffix, File directory) throws IOException {
		while(true) {
			File tempFile = File.createTempFile(prefix, suffix, directory);
			Files.delete(tempFile.toPath());
			// Check result of mkdir to catch race condition
			if(tempFile.mkdir()) return tempFile;
		}
	}

	/**
	 * Copies a stream to a file.
	 *
	 * @return  the number of bytes copied
	 */
	public static long copyToFile(InputStream in, File file) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Copies a stream to a newly created temporary file in the given directory.
	 * <p>
	 * The file is created with the default permissions via
	 * {@link Files#createTempFile(java.lang.String, java.lang.String, java.nio.file.attribute.FileAttribute...)}.
	 * </p>
	 * <p>
	 * The file is not {@linkplain File#deleteOnExit() deleted on exit}.  If this is required, we recommend creating the
	 * temp file with the <a href="https://oss.aoapps.com/tempfiles/">AO TempFiles</a> project, then using
	 * {@link #copyToFile(java.io.InputStream, java.io.File)}.  This avoids the memory leak of the implementation
	 * of {@link File#deleteOnExit()}.
	 * </p>
	 */
	public static File copyToTempFile(InputStream in, String prefix, String suffix) throws IOException {
		return copyToTempFile(in, prefix, suffix, null);
	}

	/**
	 * Copies a stream to a newly created temporary file.
	 * <p>
	 * The file is created with the default permissions via
	 * {@link Files#createTempFile(java.lang.String, java.lang.String, java.nio.file.attribute.FileAttribute...)}.
	 * </p>
	 * <p>
	 * The file is not {@linkplain File#deleteOnExit() deleted on exit}.  If this is required, we recommend creating the
	 * temp file with the <a href="https://oss.aoapps.com/tempfiles/">AO TempFiles</a> project, then using
	 * {@link #copyToFile(java.io.InputStream, java.io.File)}.  This avoids the memory leak of the implementation
	 * of {@link File#deleteOnExit()}.
	 * </p>
	 */
	public static File copyToTempFile(InputStream in, String prefix, String suffix, File directory) throws IOException {
		Path tmpPath = Files.createTempFile("cache_", null);
		File tmpFile = tmpPath.toFile();
		boolean successful = false;
		try {
			copyToFile(in, tmpFile);
			successful = true;
			return tmpFile;
		} finally {
			if(!successful) Files.delete(tmpPath);
		}
	}

	/**
	 * Makes a directory.  The directory must not already exist.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if mkdir fails.
	 *
	 * @deprecated  Please use {@link Files#createDirectory(java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}.
	 */
	@Deprecated
	public static File mkdir(File directory) throws IOException {
		return Files.createDirectory(directory.toPath()).toFile();
	}

	/**
	 * Makes a directory and all of its parents.  The directory may optionally already exist.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if mkdirs fails.
	 *
	 * @deprecated  Please use {@link Files#createDirectories(java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}
	 */
	@Deprecated
	public static File mkdirs(File directory) throws IOException {
		return Files.createDirectories(directory.toPath()).toFile();
	}

	/**
	 * Ensures that the file is a directory.
	 *
	 * @return  The directory itself.
	 *
	 * @exception  IOException  if not a directory
	 */
	public static File checkIsDirectory(File directory) throws IOException {
		if(!directory.isDirectory()) throw new IOException("Not a directory: " + directory.getPath());
		return directory;
	}

	/**
	 * Copies one file over another, possibly creating if needed.
	 *
	 * @return  the number of bytes copied
	 */
	public static long copy(File from, File to) throws IOException {
		try (InputStream in = new FileInputStream(from)) {
			long modified = from.lastModified();
			long bytes = copyToFile(in, to);
			if(modified != 0) to.setLastModified(modified);
			return bytes;
		}
	}

	/**
	 * Copies a file to an output stream.
	 *
	 * @return  the number of bytes copied
	 */
	public static long copy(File from, OutputStream out) throws IOException {
		try (InputStream in = new FileInputStream(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Copies a file to a writer in system default locale.
	 *
	 * @return  the number of characters copied
	 */
	public static long copy(File from, Writer out) throws IOException {
		try (Reader in = new FileReader(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Copies a file to an appendable in system default locale.
	 *
	 * @return  the number of characters copied
	 */
	public static long copy(File from, Appendable out) throws IOException {
		try (Reader in = new FileReader(from)) {
			return IoUtils.copy(in, out);
		}
	}

	/**
	 * Recursively copies source to destination.  Destination must not exist.
	 */
	public static void copyRecursive(File from, File to) throws IOException {
		copyRecursive(from, to, null);
	}

	/**
	 * Recursively copies source to destination.  Destination must not exist.
	 */
	public static void copyRecursive(File from, File to, FileFilter fileFilter) throws IOException {
		if(fileFilter==null || fileFilter.accept(from)) {
			if(from.isDirectory()) {
				if(to.exists()) throw new IOException("Directory exists: "+to);
				long modified = from.lastModified();
				Files.createDirectory(to.toPath()).toFile();
				String[] list = from.list();
				if(list!=null) {
					for(String child : list) {
						copyRecursive(
							new File(from, child),
							new File(to, child),
							fileFilter
						);
					}
				}
				if(modified != 0) to.setLastModified(modified);
			} else if(from.isFile()) {
				if(to.exists()) throw new IOException("File exists: "+to);
				copy(from, to);
			} else {
				throw new IOException("Neither directory not file: "+to);
			}
		}
	}

	/**
	 * Gets the extension from the path, not including any period.
	 * If no extension, returns an empty string.
	 */
	public static String getExtension(String path) {
		int pos=path.lastIndexOf('.');
		if(pos<1) return "";
		// If a / follows the ., then no extension
		int pos2=path.indexOf('/', pos+1);
		if(pos2!=-1) return "";
		return path.substring(pos+1);
	}

	/**
	 * Gets a File for a URL, retrieving the contents into a temporary file if needed.
	 * <p>
	 * The file is created with the default permissions via
	 * {@link Files#createTempFile(java.lang.String, java.lang.String, java.nio.file.attribute.FileAttribute...)}.
	 * </p>
	 *
	 * @param  deleteOnExit  when <code>true</code>, any newly created temp file will be flagged for {@link File#deleteOnExit() delete on exit}
	 *
	 * @deprecated  Please use <a href="https://oss.aoapps.com/tempfiles/apidocs/com.aoapps.tempfiles/com/aoapps/tempfiles/TempFileContext.html">TempFileContext</a>
	 *              as {@link File#deleteOnExit()} is prone to memory leaks in long-running applications.
	 */
	// TODO: Is it worth having a non-deprecated version that does not delete on exit?
	@Deprecated
	public static File getFile(URL url, String urlEncoding, boolean deleteOnExit) throws IOException {
		if("file".equalsIgnoreCase(url.getProtocol())) {
			String path = url.getFile();
			if(path.length()>0) {
				File file = new File(URLDecoder.decode(path, urlEncoding).replace('/', File.separatorChar));
				if(file.exists() && file.isFile()) return file;
			}
		}
		Path tmpPath = Files.createTempFile("url", null);
		File tmpFile = tmpPath.toFile();
		boolean successful = false;
		try {
			if(deleteOnExit) tmpFile.deleteOnExit();
			try (InputStream in = url.openStream()) {
				copyToFile(in, tmpFile);
			}
			successful = true;
			return tmpFile;
		} finally {
			if(!successful) Files.delete(tmpPath);
		}
	}

	/**
	 * Atomically renames one file to another, throwing IOException when unsuccessful.
	 */
	public static void rename(File from, File to) throws IOException {
		if(!from.renameTo(to)) throw new IOException("Unable to atomically rename \""+from+"\" to \""+to+'"');
	}

	/**
	 * Renames one file to another, throwing IOException when unsuccessful.
	 * Allow a non-atomic delete/rename pair when the underlying system is unable
	 * to rename one file over another, such as in Microsoft Windows.
	 */
	public static void renameAllowNonAtomic(File from, File to) throws IOException {
		// Try atomic rename first
		if(!from.renameTo(to)) {
			try {
				// Try overwrite in-place for Windows
				copy(from, to);
				Files.delete(from.toPath());
			} catch(IOException e) {
				throw new IOException("Unable to non-atomically rename \""+from+"\" to \""+to+'"', e);
			}
		}
	}

	/**
	 * Reads the contents of a File and returns as a String in the system default character set.
	 *
	 * @see  #readFileAsString(java.io.File, java.nio.charset.Charset)
	 * @see  Charset#defaultCharset()
	 */
	public static String readFileAsString(File file) throws IOException {
		return readFileAsString(file, Charset.defaultCharset());
	}

	/**
	 * Reads the contents of a File and returns as a String in the provided character set.
	 *
	 * @see  #readFileAsString(java.io.File)
	 */
	public static String readFileAsString(File file, Charset charset) throws IOException {
		long len = file.length();
		StringBuilder SB = len>0 && len<=Integer.MAX_VALUE ? new StringBuilder((int)len) : new StringBuilder();
		try (Reader in = new InputStreamReader(new FileInputStream(file), charset)) {
			char[] buff = BufferManager.getChars();
			try {
				int numChars;
				while((numChars = in.read(buff, 0, BufferManager.BUFFER_SIZE)) != -1) {
					SB.append(buff, 0, numChars);
				}
			} finally {
				BufferManager.release(buff, false);
			}
		}
		return SB.toString();
	}
}
