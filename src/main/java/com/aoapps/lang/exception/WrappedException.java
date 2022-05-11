/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.exception;

import com.aoapps.lang.RunnableE;
import com.aoapps.lang.Throwables;
import com.aoapps.lang.concurrent.CallableE;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;

/**
 * <p>
 * A wrapped exception may be used to rethrow checked exceptions in a context
 * where they are otherwise not allowed.
 * </p>
 * <p>
 * This could be accomplished by
 * rethrowing with {@link RuntimeException} directly, but having this distinct
 * class provides more meaning as well as the ability to catch wrapped
 * exceptions while letting all other runtime exceptions go through directly.
 * </p>
 * <p>
 * Catching {@link WrappedException} may be used to unwrap expected throwable types.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class WrappedException extends RuntimeException implements ExtraInfo {

  private static final long serialVersionUID = -987777760527780052L;

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V call(Callable<V> callable) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class, WrappedException::new);
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   *
   * @deprecated  Please use {@link #call(java.util.concurrent.Callable)}
   */
  @Deprecated
  public static <V> V wrapChecked(Callable<V> callable) {
    return call(callable);
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V call(Callable<V> callable, Object... extraInfo) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(cause, extraInfo));
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   *
   * @deprecated  Please use {@link #call(java.util.concurrent.Callable, java.lang.Object...)}
   */
  @Deprecated
  public static <V> V wrapChecked(Callable<V> callable, Object... extraInfo) {
    return call(callable, extraInfo);
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V call(Callable<V> callable, String message) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause));
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   *
   * @deprecated  Please use {@link #call(java.util.concurrent.Callable, java.lang.String)}
   */
  @Deprecated
  public static <V> V wrapChecked(Callable<V> callable, String message) {
    return call(callable, message);
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V call(Callable<V> callable, String message, Object... extraInfo) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause, extraInfo));
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   *
   * @deprecated  Please use {@link #call(java.util.concurrent.Callable, java.lang.String, java.lang.Object...)}
   */
  @Deprecated
  public static <V> V wrapChecked(Callable<V> callable, String message, Object... extraInfo) {
    return call(callable, message, extraInfo);
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V callE(CallableE<V, ?> callable) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class, WrappedException::new);
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V callE(CallableE<V, ?> callable, Object... extraInfo) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(cause, extraInfo));
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V callE(CallableE<V, ?> callable, String message) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause));
    }
  }

  /**
   * Invokes the given callable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static <V> V callE(CallableE<V, ?> callable, String message, Object... extraInfo) {
    try {
      return callable.call();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause, extraInfo));
    }
  }

  /**
   * Invokes the given runnable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static void runE(RunnableE<?> runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class, WrappedException::new);
    }
  }

  /**
   * Invokes the given runnable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static void runE(RunnableE<?> runnable, Object... extraInfo) {
    try {
      runnable.run();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(cause, extraInfo));
    }
  }

  /**
   * Invokes the given runnable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static void runE(RunnableE<?> runnable, String message) {
    try {
      runnable.run();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause));
    }
  }

  /**
   * Invokes the given runnable, wrapping any checked exceptions.
   */
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public static void runE(RunnableE<?> runnable, String message, Object... extraInfo) {
    try {
      runnable.run();
    } catch (Throwable t) {
      throw Throwables.wrap(t, WrappedException.class,
          cause -> new WrappedException(message, cause, extraInfo));
    }
  }

  private final Object[] extraInfo;

  /**
   * @deprecated Please provide cause.
   */
  @Deprecated
  public WrappedException() {
    super();
    this.extraInfo = null;
  }

  /**
   * @deprecated Please provide cause.
   */
  @Deprecated
  public WrappedException(String message) {
    super(message);
    this.extraInfo = null;
  }

  /**
   * Uses extra info of the original cause when it is an {@link ExtraInfo}.
   */
  public WrappedException(Throwable cause) {
    super(cause);
    this.extraInfo = (cause instanceof ExtraInfo) ? ((ExtraInfo) cause).getExtraInfo() : null;
  }

  /**
   * @deprecated  Please use {@link UncheckedIOException}.
   */
  @Deprecated
  public WrappedException(IOException cause) {
    this((Throwable) cause);
  }

  /**
   * @param  extraInfo No defensive copy
   */
  public WrappedException(Throwable cause, Object... extraInfo) {
    super(cause);
    this.extraInfo = extraInfo;
  }

  /**
   * @deprecated  Please use {@link UncheckedIOException}.
   */
  @Deprecated
  public WrappedException(IOException cause, Object... extraInfo) {
    this((Throwable) cause, extraInfo);
  }

  /**
   * Uses extra info of the original cause when it is an {@link ExtraInfo}.
   */
  public WrappedException(String message, Throwable cause) {
    super(message, cause);
    this.extraInfo = (cause instanceof ExtraInfo) ? ((ExtraInfo) cause).getExtraInfo() : null;
  }

  /**
   * @deprecated  Please use {@link UncheckedIOException}.
   */
  @Deprecated
  public WrappedException(String message, IOException cause) {
    this(message, (Throwable) cause);
  }

  /**
   * @param  extraInfo No defensive copy
   */
  public WrappedException(String message, Throwable cause, Object... extraInfo) {
    super(message, cause);
    this.extraInfo = extraInfo;
  }

  /**
   * @deprecated  Please use {@link UncheckedIOException}.
   */
  @Deprecated
  public WrappedException(String message, IOException cause, Object... extraInfo) {
    this(message, (Throwable) cause, extraInfo);
  }

  @Override
  public String getMessage() {
    String message = super.getMessage();
    if (message != null) {
      return message;
    }
    Throwable cause = getCause();
    return (cause == null) ? null : cause.getMessage();
  }

  @Override
  public String getLocalizedMessage() {
    String message = super.getMessage();
    if (message != null) {
      return message;
    }
    Throwable cause = getCause();
    return (cause == null) ? null : cause.getLocalizedMessage();
  }

  /**
   * {@inheritDoc}
   *
   * @return  No defensive copy
   */
  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField")
  public Object[] getExtraInfo() {
    return extraInfo;
  }

  static {
    Throwables.registerSurrogateFactory(WrappedException.class, (template, cause) ->
        new WrappedException(template.getMessage(), cause, template.extraInfo)
    );
  }
}
