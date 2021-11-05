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
package com.aoapps.lang;

import java.util.function.Function;

/**
 * Utilities for working with {@link AutoCloseable}.
 */
public abstract class AutoCloseables {

	/** Make no instances. */
	private AutoCloseables() {throw new AssertionError();}

	/**
	 * Closes the given {@link AutoCloseable}, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  closeable  The closeable to be closed
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public static Throwable closeAndCatch(Throwable t0, AutoCloseable closeable) {
		if(closeable != null) {
			try {
				closeable.close();
			} catch(Throwable t) {
				t0 = Throwables.addSuppressed(t0, t);
			}
		}
		return t0;
	}

	/**
	 * Closes the given {@link AutoCloseable}, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  closeable  The closeable to be closed
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable closeAndCatch(AutoCloseable closeable) {
		return closeAndCatch(null, closeable);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	public static Throwable closeAndCatch(Throwable t0, AutoCloseable ... closeable) {
		if(closeable != null) {
			for(AutoCloseable ac : closeable) {
				t0 = closeAndCatch(t0, ac);
			}
		}
		return t0;
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable closeAndCatch(AutoCloseable ... closeable) {
		return closeAndCatch(null, closeable);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  {@code t0}, a new throwable, or {@code null} when none given and none new
	 */
	public static Throwable closeAndCatch(Throwable t0, Iterable<? extends AutoCloseable> closeable) {
		if(closeable != null) {
			for(AutoCloseable ac : closeable) {
				t0 = closeAndCatch(t0, ac);
			}
		}
		return t0;
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * <p>
	 * See {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  A new throwable or {@code null}
	 */
	public static Throwable closeAndCatch(Iterable<? extends AutoCloseable> closeable) {
		return closeAndCatch(null, closeable);
	}

	/**
	 * Closes the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when {@code t0} is {@code null} and no new throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The closeable to be closed
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable closeable
	) throws Error, RuntimeException, X {
		Throwable t = closeAndCatch(t0, closeable);
		if(t != null) {
			if(t instanceof Error) throw (Error)t;
			if(t instanceof RuntimeException) throw (RuntimeException)t;
			if(xClass.isInstance(t)) throw xClass.cast(t);
			X newExc = xSupplier.apply(t);
			if(
				t instanceof InterruptedException
				&& !(newExc instanceof InterruptedException)
			) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
			throw newExc;
		}
	}

	/**
	 * Closes the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when no throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The closeable to be closed
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable closeable
	) throws Error, RuntimeException, X {
		closeAndThrow(null, xClass, xSupplier, closeable);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when {@code t0} is {@code null} and no new throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable ... closeable
	) throws Error, RuntimeException, X {
		Throwable t = closeAndCatch(t0, closeable);
		if(t != null) {
			if(t instanceof Error) throw (Error)t;
			if(t instanceof RuntimeException) throw (RuntimeException)t;
			if(xClass.isInstance(t)) throw xClass.cast(t);
			X newExc = xSupplier.apply(t);
			if(
				t instanceof InterruptedException
				&& !(newExc instanceof InterruptedException)
			) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
			throw newExc;
		}
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when no throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable ... closeable
	) throws Error, RuntimeException, X {
		closeAndThrow(null, xClass, xSupplier, closeable);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when {@code t0} is {@code null} and no new throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		Iterable<? extends AutoCloseable> closeable
	) throws Error, RuntimeException, X {
		Throwable t = closeAndCatch(t0, closeable);
		if(t != null) {
			if(t instanceof Error) throw (Error)t;
			if(t instanceof RuntimeException) throw (RuntimeException)t;
			if(xClass.isInstance(t)) throw xClass.cast(t);
			X newExc = xSupplier.apply(t);
			if(
				t instanceof InterruptedException
				&& !(newExc instanceof InterruptedException)
			) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
			throw newExc;
		}
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, throwing all {@link Throwable}, wrapping when needed.
	 * <p>
	 * Only returns when no throwables.
	 * </p>
	 * <p>
	 * When the exception is an {@link InterruptedException} and is wrapped via {@code xSupplier}, and the resulting
	 * wrapper is not itself an {@link InterruptedException}, the current thread will be
	 * {@linkplain Thread#interrupt() re-interrupted}.
	 * </p>
	 *
	 * @param  xClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
	 *                 are thrown directly.
	 *
	 * @param  xSupplier  Other throwables are wrapped via this function, then thrown
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @throws  Error  When resolved throwable is an {@link Error}
	 *
	 * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
	 *
	 * @throws  X      When resolved throwable is an instance of {@code xClass}, otherwise
	 *                 wrapped via {@code xSupplier}
	 */
	public static <X extends Throwable> void closeAndThrow(
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		Iterable<? extends AutoCloseable> closeable
	) throws Error, RuntimeException, X {
		closeAndThrow(null, xClass, xSupplier, closeable);
	}

	/**
	 * Closes the given {@link AutoCloseable}, catching all {@link Throwable}.
	 * Wraps any resulting throwable, unless is an instance of {@code xClass}, {@link Error}, or {@link RuntimeException}.
	 * <ol>
	 * <li>When {@code null}, returns {@code null}.</li>
	 * <li>When is an instance of {@code xClass}, returns the exception.</li>
	 * <li>When is {@link Error} or {@link RuntimeException}, throws the exception directly.</li>
	 * <li>Otherwise, returns the exception wrapped via {@code xSupplier}.</li>
	 * </ol>
	 * <p>
	 * This is expected to typically used within a catch block, to throw a narrower scope:
	 * </p>
	 * <pre>try {
	 *   …
	 * } catch(Throwable t) {
	 *   throw AutoCloseables.closeAndWrap(t, SQLException.class, SQLException::new, closeable);
	 * }</pre>
	 * <p>
	 * See {@link Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class are returned directly.
	 *
	 * @param  xSupplier  Throwables that a not returned directly, and are not {@link Error} or
	 *                    {@link RuntimeException}, are wrapped via this function, then returned.
	 *
	 * @param  closeable  The closeable to be closed
	 *
	 * @return  {@code null} when {@code t0} is {@code null} and no new throwables,
	 *          resulting throwable when is an instance of {@code xClass},
	 *          otherwise wrapped via {@code xSupplier}.
	 *
	 * @throws  Error             When resulting throwable is an {@link Error}
	 * @throws  RuntimeException  When resulting throwable is a {@link RuntimeException}
	 *
	 * @see  Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)
	 */
	public static <X extends Throwable> X closeAndWrap(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable closeable
	) {
		return Throwables.wrap(
			closeAndCatch(t0, closeable),
			xClass,
			xSupplier
		);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * Wraps any resulting throwable, unless is an instance of {@code xClass}, {@link Error}, or {@link RuntimeException}.
	 * <ol>
	 * <li>When {@code null}, returns {@code null}.</li>
	 * <li>When is an instance of {@code xClass}, returns the exception.</li>
	 * <li>When is {@link Error} or {@link RuntimeException}, throws the exception directly.</li>
	 * <li>Otherwise, returns the exception wrapped via {@code xSupplier}.</li>
	 * </ol>
	 * <p>
	 * This is expected to typically used within a catch block, to throw a narrower scope:
	 * </p>
	 * <pre>try {
	 *   …
	 * } catch(Throwable t) {
	 *   throw AutoCloseables.closeAndWrap(t, SQLException.class, SQLException::new, closeable);
	 * }</pre>
	 * <p>
	 * See {@link Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class are returned directly.
	 *
	 * @param  xSupplier  Throwables that a not returned directly, and are not {@link Error} or
	 *                    {@link RuntimeException}, are wrapped via this function, then returned.
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  {@code null} when {@code t0} is {@code null} and no new throwables,
	 *          resulting throwable when is an instance of {@code xClass},
	 *          otherwise wrapped via {@code xSupplier}.
	 *
	 * @throws  Error             When resulting throwable is an {@link Error}
	 * @throws  RuntimeException  When resulting throwable is a {@link RuntimeException}
	 *
	 * @see  Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)
	 */
	public static <X extends Throwable> X closeAndWrap(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		AutoCloseable ... closeable
	) {
		return Throwables.wrap(
			closeAndCatch(t0, closeable),
			xClass,
			xSupplier
		);
	}

	/**
	 * Closes all of the given {@link AutoCloseable} in order, catching all {@link Throwable}.
	 * Wraps any resulting throwable, unless is an instance of {@code xClass}, {@link Error}, or {@link RuntimeException}.
	 * <ol>
	 * <li>When {@code null}, returns {@code null}.</li>
	 * <li>When is an instance of {@code xClass}, returns the exception.</li>
	 * <li>When is {@link Error} or {@link RuntimeException}, throws the exception directly.</li>
	 * <li>Otherwise, returns the exception wrapped via {@code xSupplier}.</li>
	 * </ol>
	 * <p>
	 * This is expected to typically used within a catch block, to throw a narrower scope:
	 * </p>
	 * <pre>try {
	 *   …
	 * } catch(Throwable t) {
	 *   throw AutoCloseables.closeAndWrap(t, SQLException.class, SQLException::new, closeable);
	 * }</pre>
	 * <p>
	 * See {@link Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)} for details on how
	 * {@link ThreadDeath} and {@link InterruptedException} are managed.
	 * </p>
	 *
	 * @param  t0  If not {@code null}, any new throwables will be combined via
	 *             {@link Throwables#addSuppressed(java.lang.Throwable, java.lang.Throwable)}
	 *
	 * @param  xClass  Throwables of this class are returned directly.
	 *
	 * @param  xSupplier  Throwables that a not returned directly, and are not {@link Error} or
	 *                    {@link RuntimeException}, are wrapped via this function, then returned.
	 *
	 * @param  closeable  The set of all closeables, which will be closed in order
	 *
	 * @return  {@code null} when {@code t0} is {@code null} and no new throwables,
	 *          resulting throwable when is an instance of {@code xClass},
	 *          otherwise wrapped via {@code xSupplier}.
	 *
	 * @throws  Error             When resulting throwable is an {@link Error}
	 * @throws  RuntimeException  When resulting throwable is a {@link RuntimeException}
	 *
	 * @see  Throwables#wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)
	 */
	public static <X extends Throwable> X closeAndWrap(
		Throwable t0,
		Class<? extends X> xClass,
		Function<? super Throwable, ? extends X> xSupplier,
		Iterable<? extends AutoCloseable> closeable
	) {
		return Throwables.wrap(
			closeAndCatch(t0, closeable),
			xClass,
			xSupplier
		);
	}
}
