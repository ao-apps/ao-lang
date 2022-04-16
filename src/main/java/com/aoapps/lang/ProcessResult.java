/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.util.BufferManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Contains the result of executing a process, including return code, standard output, and standard error.
 *
 * @author  AO Industries, Inc.
 */
public class ProcessResult {

	/**
	 * Executes the provided command and gets the result in the system default character set.
	 */
	public static ProcessResult exec(String ... command) throws IOException {
		return getProcessResult(Runtime.getRuntime().exec(command), Charset.defaultCharset());
	}

	/**
	 * Executes the provided command and gets the result in the provided character set.
	 */
	public static ProcessResult exec(String[] command, Charset charset) throws IOException {
		return getProcessResult(Runtime.getRuntime().exec(command), charset);
	}

	/**
	 * Gets the result of the provided process in the system default character set.
	 */
	public static ProcessResult getProcessResult(Process process) throws IOException {
		return getProcessResult(process, Charset.defaultCharset());
	}

	/**
	 * Gets the result of the provided process in the provided character set.
	 */
	public static ProcessResult getProcessResult(final Process process, final Charset charset) throws IOException {
		// Close the input immediately
		process.getOutputStream().close();

		// Read stderr in background thread
		final String[] stderrWrapper = new String[1];
		final IOException[] stderrException = new IOException[1];
		Thread stderrThread = new Thread(() -> {
			StringBuilder stderrBuilder = null; // Instantiated when first needed
			try {
				try (Reader stderrIn = new InputStreamReader(process.getErrorStream(), charset)) {
					char[] buff = BufferManager.getChars();
					try {
						int count;
						while((count = stderrIn.read(buff, 0, BufferManager.BUFFER_SIZE)) != -1) {
							if(count > 0) {
								if(stderrBuilder == null) stderrBuilder = new StringBuilder(Math.max(count, 16));
								stderrBuilder.append(buff, 0, count);
							}
						}
					} finally {
						BufferManager.release(buff, false);
					}
				}
			} catch(IOException exc) {
				synchronized(stderrException) {
					stderrException[0] = exc;
				}
			} finally {
				synchronized(stderrWrapper) {
					stderrWrapper[0] = stderrBuilder==null ? "" : stderrBuilder.toString();
				}
			}
		});
		stderrThread.start();

		try {
			// Read stdout in current thread
			StringBuilder stdoutBuilder = null; // Instantiated when first needed
			IOException stdoutException = null;
			try {
				try (Reader stdoutIn = new InputStreamReader(process.getInputStream(), charset)) {
					char[] buff = BufferManager.getChars();
					try {
						int count;
						while((count = stdoutIn.read(buff, 0, BufferManager.BUFFER_SIZE)) != -1) {
							if(count > 0) {
								if(stdoutBuilder == null) stdoutBuilder = new StringBuilder(Math.max(count, 16));
								stdoutBuilder.append(buff, 0, count);
							}
						}
					} finally {
						BufferManager.release(buff, false);
					}
				}
			} catch(IOException exc) {
				stdoutException = exc;
			}

			// Wait for full read of stderr
			stderrThread.join();

			// Wait for process to exit
			int exitVal = process.waitFor();

			// Check for exceptions in threads
			if(stdoutException != null) throw stdoutException;
			synchronized(stderrException) {
				if(stderrException[0] != null) throw stderrException[0];
			}

			// Get output
			String stdout = stdoutBuilder==null ? "" : stdoutBuilder.toString();
			// Get error from background thread
			String stderr;
			synchronized(stderrWrapper) {
				stderr = stderrWrapper[0];
			}
			if(stderr == null) stderr = "";

			// Return results
			return new ProcessResult(
				exitVal,
				stdout,
				stderr
			);
		} catch(InterruptedException err) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();
			InterruptedIOException ioErr = new InterruptedIOException();
			ioErr.initCause(err);
			throw ioErr;
		}
	}

	private final int exitVal;
	private final String stdout;
	private final String stderr;

	private ProcessResult(int exitVal, String stdout, String stderr) {
		this.exitVal = exitVal;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public int getExitVal() {
		return exitVal;
	}

	public String getStdout() {
		return stdout;
	}

	public String getStderr() {
		return stderr;
	}
}
