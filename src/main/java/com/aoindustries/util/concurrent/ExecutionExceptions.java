/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.util.concurrent;

import com.aoindustries.sql.WrappedSQLException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

/**
 * Utilities for working with {@link ExecutionException}.
 */
// TODO: Instead of this approach, is it possible to merge the stack traces in a meaningful way?
// TODO: Can we copy the stack trace from the ExecutionException, and discard it?
// TODO: Is this all overkill?
final public class ExecutionExceptions {

	/**
	 * Make no instances.
	 */
	private ExecutionExceptions() {}

	/**
	 * Wraps and throws an {@link ExecutionException} when its {@linkplain ExecutionException#getCause() cause} is an
	 * instance of {@code xClass}.
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
	 *
	 * @param  xClass  Exceptions with causes of this class are wrapped and thrown.
	 *
	 * @param  xSupplier  Performs wrapping of the execution exception.
	 *
	 * @throws  X  When cause is an instance of {@code xClass}, throws {@code ee} wrapped via {@code xSupplier}.
	 */
	public static <X extends Throwable> void wrapAndThrow(
		ExecutionException ee,
		Class<? extends X> xClass,
		BiFunction<? super String, ? super ExecutionException, ? extends X> xSupplier
	) throws X {
		if(ee != null) {
			Throwable cause = ee.getCause();
			if(xClass.isInstance(cause)) throw xSupplier.apply(cause.getMessage(), ee);
		}
	}

	/**
	 * Wraps and throws an {@link ExecutionException} when its {@linkplain ExecutionException#getCause() cause} is an
	 * instance of {@code xClass}.
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
	 *   wrapAndThrowWithCause(ee, SQLException.class, (orig, cause)
	 *     -&gt; new SQLException(cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), orig)));
	 *   throw ee;
	 * }</pre>
	 *
	 * @param  xClass  Exceptions with causes of this class are wrapped and thrown.
	 *
	 * @param  xSupplier  Performs wrapping of the execution exception.
	 *
	 * @throws  X  When cause is an instance of {@code xClass}, throws {@code ee} wrapped via {@code xSupplier}.
	 */
	public static <X extends Throwable> void wrapAndThrowWithCause(
		ExecutionException ee,
		Class<? extends X> xClass,
		BiFunction<? super X, ? super ExecutionException, ? extends X> xSupplier
	) throws X {
		if(ee != null) {
			Throwable cause = ee.getCause();
			if(xClass.isInstance(cause)) throw xSupplier.apply(xClass.cast(cause), ee);
		}
	}

	// TODO: wrapAndThrowIOException, maintaining common types?

	/**
	 * Wraps and throws an {@link ExecutionException} when its {@linkplain ExecutionException#getCause() cause} is a
	 * {@link SQLException}.
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
	 *   wrapAndThrowSQLException(ee, IOException.class, IOException::new);
	 *   throw ee;
	 * }</pre>
	 *
	 * @throws  WrappedSQLException  When cause is an instance of {@link WrappedSQLException}, throws {@code ee} wrapped via
	 *                               {@link WrappedSQLException#WrappedSQLException(java.lang.String, java.lang.String, int, java.lang.Throwable, java.lang.String)}
	 * @throws  SQLException  When cause is an instance of {@link SQLException}, throws {@code ee} wrapped via
	 *                        {@link SQLException#SQLException(java.lang.String, java.lang.String, int, java.lang.Throwable)}.
	 */
	// TODO: More specializations of SQLException?
	public static <X extends Throwable> void wrapAndThrowSQLException(ExecutionException ee) throws WrappedSQLException, SQLException {
		if(ee != null) {
			Throwable cause = ee.getCause();
			if(cause instanceof WrappedSQLException) {
				WrappedSQLException sqlCause = (WrappedSQLException)cause;
				throw new WrappedSQLException(
					sqlCause.getMessage(),
					sqlCause.getSQLState(),
					sqlCause.getErrorCode(),
					ee,
					sqlCause.getSqlString()
				);
			}
			if(cause instanceof SQLException) {
				SQLException sqlCause = (SQLException)cause;
				throw new SQLException(
					sqlCause.getMessage(),
					sqlCause.getSQLState(),
					sqlCause.getErrorCode(),
					ee
				);
			}
		}
	}
}
