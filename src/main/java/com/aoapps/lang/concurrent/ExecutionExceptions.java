/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang.concurrent;

import com.aoapps.lang.Throwables;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

/**
 * Utilities for working with {@link ExecutionException}.
 */
public final class ExecutionExceptions {

	/**
	 * Make no instances.
	 */
	private ExecutionExceptions() {}

	/**
	 * Wraps and throws an {@link ExecutionException} when its {@linkplain ExecutionException#getCause() cause} is an
	 * instance of {@code xClass}.
	 * This is compatible with Lambda method references on common throwable constructors that take
	 * {@code (String message, Throwable cause)}.
	 * <p>
	 * First, an attempt is made to create a surrogate of the cause via
	 * {@link Throwables#newSurrogate(java.lang.Throwable, java.lang.Throwable)}, with the execution exception being the
	 * cause of the new surrogate.  When a surrogate cannot be created, uses the provided function {@code xSupplier} to
	 * create a new wrapper.
	 * </p>
	 * <p>
	 * When an {@link ExecutionException} occurs, unwrapping the {@linkplain ExecutionException#getCause() cause} may
	 * lose important stack trace information, since the cause is likely processed on a different thread and will not
	 * have the full caller stack trace.
	 * </p>
	 * <p>
	 * Furthermore, it is desirable to be able to maintain expected exception types.  This wrapping will help maintain
	 * exception types while not losing critical stack trace information.
	 * </p>
	 *
	 * <p>
	 * This is expected to typically used within a catch block, to maintain exception types:
	 * </p>
	 * <pre>try {
	 *   …
	 *   return future.get();
	 * } catch(ExecutionException ee) {
	 *   wrapAndThrow(ee, IOException.class, IOException::new);
	 *   throw ee;
	 * }</pre>
	 * <p>
	 * When the cause is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * surrogate is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  xClass  Exceptions with causes of this class are wrapped and thrown.
	 *
	 * @param  xSupplier  Performs wrapping of the execution exception itself when a surrogate cannot be created.
	 *
	 * @throws  X  When cause is an instance of {@code xClass}, throws {@code ee} wrapped via {@code xSupplier}.
	 *
	 * @see  Throwables#newSurrogate(java.lang.Throwable, java.lang.Throwable)
	 */
	public static <X extends Throwable> void wrapAndThrow(
		ExecutionException ee,
		Class<? extends X> xClass,
		BiFunction<? super String, ? super ExecutionException, ? extends X> xSupplier
	) throws X {
		if(ee != null) {
			Throwable cause = ee.getCause();
			if(xClass.isInstance(cause)) {
				X template = xClass.cast(cause);
				X surrogate = Throwables.newSurrogate(template, ee);
				if(surrogate != template) {
					throw surrogate;
				} else {
					X newExc = xSupplier.apply(template.getMessage(), ee);
					if(
						cause instanceof InterruptedException
						&& !(newExc instanceof InterruptedException)
					) {
						// Restore the interrupted status
						Thread.currentThread().interrupt();
					}
					throw newExc;
				}
			}
		}
	}

	/**
	 * Wraps and throws an {@link ExecutionException} when its {@linkplain ExecutionException#getCause() cause} is an
	 * instance of {@code xClass}.
	 * <p>
	 * First, an attempt is made to create a surrogate of the cause via
	 * {@link Throwables#newSurrogate(java.lang.Throwable, java.lang.Throwable)}, with the execution exception being the
	 * cause of the new surrogate.  When a surrogate cannot be created, uses the provided function {@code xSupplier} to
	 * create a new wrapper.
	 * </p>
	 * <p>
	 * When an {@link ExecutionException} occurs, unwrapping the {@linkplain ExecutionException#getCause() cause} may
	 * lose important stack trace information, since the cause is likely processed on a different thread and will not
	 * have the full caller stack trace.
	 * </p>
	 * <p>
	 * Furthermore, it is desirable to be able to maintain expected exception types.  This wrapping will help maintain
	 * exception types while not losing critical stack trace information.
	 * </p>
	 *
	 * <p>
	 * This is expected to typically used within a catch block, to maintain exception types:
	 * </p>
	 * <pre>try {
	 *   …
	 *   return future.get();
	 * } catch(ExecutionException ee) {
	 *   wrapAndThrowWithCause(ee, SQLException.class, (template, cause)
	 *     -&gt; new SQLException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause)));
	 *   throw ee;
	 * }</pre>
	 * <p>
	 * When the cause is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * surrogate is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  xClass  Exceptions with causes of this class are wrapped and thrown.
	 *
	 * @param  xSupplier  Performs wrapping of the execution exception itself when a surrogate cannot be created.
	 *
	 * @throws  X  When cause is an instance of {@code xClass}, throws {@code ee} wrapped via {@code xSupplier}.
	 *
	 * @see  Throwables#newSurrogate(java.lang.Throwable, java.lang.Throwable)
	 */
	public static <X extends Throwable> void wrapAndThrowWithTemplate(
		ExecutionException ee,
		Class<? extends X> xClass,
		BiFunction<? super X, ? super ExecutionException, ? extends X> xSupplier
	) throws X {
		if(ee != null) {
			Throwable cause = ee.getCause();
			if(xClass.isInstance(cause)) {
				X template = xClass.cast(cause);
				X surrogate = Throwables.newSurrogate(template, ee);
				if(surrogate != template) {
					throw surrogate;
				} else {
					X newExc = xSupplier.apply(template, ee);
					if(
						cause instanceof InterruptedException
						&& !(newExc instanceof InterruptedException)
					) {
						// Restore the interrupted status
						Thread.currentThread().interrupt();
					}
					throw newExc;
				}
			}
		}
	}
}
