/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022, 2023, 2024, 2025  AO Industries, Inc.
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

import com.aoapps.lang.concurrent.ExecutionExceptions;
import com.aoapps.lang.exception.WrappedError;
import com.aoapps.lang.exception.WrappedException;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Utilities for working with {@link Throwable}.
 */
// TODO: Automatically use cause from InvocationTargetException, javax.management.ReflectionException, and WrappedException, when cause is Error, Runtime, or assignable to X?
public final class Throwables {

  /** Make no instances. */
  private Throwables() {
    throw new AssertionError();
  }

  /**
   * Checks if a throwable is already suppressed.
   */
  public static boolean isSuppressed(Throwable t0, Throwable suppressed) {
    for (Throwable t : t0.getSuppressed()) {
      if (t == suppressed) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a suppressed exception, unless already in the list of suppressed exceptions.
   *
   * <p>When {@code suppressed} is an {@link InterruptedException} and {@code t0} is not an {@link InterruptedException},
   * the current thread will be {@linkplain Thread#interrupt() re-interrupted}.</p>
   *
   * <p>When {@code suppressed} is a {@link ThreadDeath} and {@code t0} is not itself a {@link ThreadDeath},
   * {@code suppressed} will be returned instead, with {@code t0} added to it as suppressed.
   * This is to maintain the precedence of {@link ThreadDeath} for fail-fast behavior.</p>
   *
   * @param  t0  The throwable to add to.  When {@code null}, {@code suppressed} is returned instead.
   *
   * @param  suppressed  The suppressed throwable, skipped when {@code null}
   *
   * @return  {@code t0} when not null, otherwise {@code suppressed}.
   *
   * @see  #addSuppressedAndThrow(java.lang.Throwable, java.lang.Class, java.util.function.Function, java.lang.Throwable)
   */
  // TODO: Rename "merge", since either one of them might be returned?
  public static Throwable addSuppressed(Throwable t0, Throwable suppressed) {
    if (
        suppressed != null
            // Never try to suppress self
            && suppressed != t0
    ) {
      if (t0 == null) {
        t0 = suppressed;
      } else {
        if (
            suppressed instanceof InterruptedException
                && !(t0 instanceof InterruptedException)
        ) {
          // Restore the interrupted status
          Thread.currentThread().interrupt();
        }
        if (
            suppressed instanceof ThreadDeath
                && !(t0 instanceof ThreadDeath)
        ) {
          // Swap order to maintain fail-fast ThreadDeath
          Throwable t = t0;
          t0 = suppressed;
          suppressed = t;
        }
        if (!isSuppressed(t0, suppressed)) {
          t0.addSuppressed(suppressed);
        }
      }
    }
    return t0;
  }

  /**
   * Adds a suppressed exception, unless already in the list of suppressed exceptions,
   * wrapping when needed, then throwing the result.
   *
   * <p>When {@code suppressed} is an {@link InterruptedException} and {@code t0} is not an {@link InterruptedException},
   * the current thread will be {@linkplain Thread#interrupt() re-interrupted}.</p>
   *
   * <p>When {@code suppressed} is a {@link ThreadDeath} and {@code t0} is not itself a {@link ThreadDeath},
   * {@code suppressed} will be returned instead, with {@code t0} added to it as suppressed.
   * This is to maintain the precedence of {@link ThreadDeath} for fail-fast behavior.</p>
   *
   * <p>Only returns when both {@code t0} and {@code suppressed} are {@code null}.</p>
   *
   * @param  t0  The throwable to add to.  When {@code null}, {@code suppressed} is thrown instead.
   *
   * @param  exClass  Throwables of this class, as well as {@link Error} and {@link RuntimeException},
   *                  are thrown directly.
   *
   * @param  exSupplier  Other throwables are wrapped via this function, then thrown
   *
   * @param  suppressed  The suppressed throwable, skipped when {@code null}
   *
   * @throws  Error  When resolved throwable is an {@link Error}
   *
   * @throws  RuntimeException  When resolved throwable is a {@link RuntimeException}
   *
   * @throws  Ex      When resolved throwable is an instance of {@code exClass}, otherwise
   *                  wrapped via {@code exSupplier}
   *
   * @see  #addSuppressed(java.lang.Throwable, java.lang.Throwable)
   * @see  #wrap(java.lang.Throwable, java.lang.Class, java.util.function.Function)
   */
  // TODO: Rename "mergeAndThrow", since either one of them might be returned?
  public static <Ex extends Throwable> void addSuppressedAndThrow(
      Throwable t0,
      Class<? extends Ex> exClass,
      Function<? super Throwable, ? extends Ex> exSupplier,
      Throwable suppressed
  ) throws Error, RuntimeException, Ex {
    t0 = addSuppressed(t0, suppressed);
    if (t0 != null) {
      if (t0 instanceof Error) {
        throw (Error) t0;
      }
      if (t0 instanceof RuntimeException) {
        throw (RuntimeException) t0;
      }
      if (exClass.isInstance(t0)) {
        throw exClass.cast(t0);
      }
      throw exSupplier.apply(t0);
    }
  }

  /**
   * Wraps an exception, unless is an instance of {@code exClass}, {@link Error}, or {@link RuntimeException}.
   * <ol>
   * <li>When {@code null}, returns {@code null}.</li>
   * <li>When is an instance of {@code exClass}, returns the exception.</li>
   * <li>When is {@link Error} or {@link RuntimeException}, throws the exception directly.</li>
   * <li>Otherwise, throws the exception wrapped via {@code exSupplier}.</li>
   * </ol>
   *
   * <p>This is expected to typically used within a catch block, to throw a narrower scope:</p>
   *
   * <pre>try {
   *   …
   * } catch (Throwable t) {
   *   throw Throwables.wrap(t, SQLException.class, SQLException::new);
   * }</pre>
   *
   * <p>When the exception is an {@link InterruptedException} and is wrapped via {@code exSupplier}, and the resulting
   * wrapper is not itself an {@link InterruptedException}, the current thread will be
   * {@linkplain Thread#interrupt() re-interrupted}.</p>
   *
   * @param  t  The throwable to return, throw, or wrap and return.
   *
   * @param  exClass  Throwables of this class are returned directly.
   *
   * @param  exSupplier  Throwables that a not returned directly, and are not {@link Error} or
   *                     {@link RuntimeException}, are wrapped via this function, then returned.
   *
   * @return  {@code t} when is an instance of {@code exClass} or when {@code t} has been wrapped via {@code exSupplier}.
   *
   * @throws  Error             When {@code t} is an {@link Error}
   * @throws  RuntimeException  When {@code t} is a {@link RuntimeException}
   *
   * @see  #addSuppressedAndThrow(java.lang.Throwable, java.lang.Class, java.util.function.Function, java.lang.Throwable)
   */
  public static <Ex extends Throwable> Ex wrap(
      Throwable t,
      Class<? extends Ex> exClass,
      Function<? super Throwable, ? extends Ex> exSupplier
  ) throws Error, RuntimeException {
    if (t == null) {
      return null;
    }
    if (exClass.isInstance(t)) {
      return exClass.cast(t);
    }
    if (t instanceof Error) {
      throw (Error) t;
    }
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    Ex newExc = exSupplier.apply(t);
    if (
        t instanceof InterruptedException
            && !(newExc instanceof InterruptedException)
    ) {
      // Restore the interrupted status
      Thread.currentThread().interrupt();
    }
    return newExc;
  }

  private static final ConcurrentMap<Class<?>, ThrowableSurrogateFactory<?>> surrogateFactories =
      new ConcurrentHashMap<>();

  /**
   * Attempts to create a new instance of the same class as the given template {@link Throwable}, with all important
   * state maintained, but with the stack trace of the current {@link Thread}.
   * When a new throwable is created, it will use the given cause,
   * possibly with additional wrapping for compatibility.
   *
   * <p>This is used to maintain exception types and states across thread boundaries, such as when an exception cause
   * is obtained from an {@link ExecutionException}.  By wrapping the template, the full stack traces of both threads
   * are maintained.  This new {@link Throwable} provides the full stack trace of the caller, while the template
   * contains the stack trace from the other thread.</p>
   *
   * <p>Only types with registered factories will perform the conversion, otherwise the given cause is returned without
   * wrapping.</p>
   *
   * <p>This current implementation has registered all possible Java SE 8 throwable types, except those that have been
   * removed through Java 17.  Additionally, various throwable implementations
   * {@link #registerSurrogateFactory(java.lang.Class, com.aoapps.lang.ThrowableSurrogateFactory) register themselves}
   * in static blocks, while other APIs may be registered via the {@link ServiceLoader} mechanism on the
   * {@link ThrowableSurrogateFactoryInitializer} interface.</p>
   *
   * @param  cause  The cause to use for the new throwable.  This should typically be either the template itself, or
   *                should have the template somewhere in its chain of causes.
   *
   * @return  When wrapping performed, returns a new throwable of the same class as the template, but with the
   *          caller's stack trace and the given cause.  When no wrapping performed, returns the template
   *          itself.
   *
   * @see  ExecutionExceptions
   */
  @SuppressWarnings("unchecked")
  public static <Ex extends Throwable> Ex newSurrogate(Ex template, Throwable cause) {
    ThrowableSurrogateFactory<Ex> factory = (ThrowableSurrogateFactory<Ex>) surrogateFactories.get(template.getClass());
    if (factory != null) {
      Ex surrogate = factory.newSurrogate(template, cause);
      if (surrogate != null) {
        return surrogate;
      }
    }
    // No surrogate, return original
    return template;
  }

  /**
   * Attempts to create a new instance of the same class as the given template {@link Throwable}, with all important
   * state maintained, but with the stack trace of the current {@link Thread}.
   * When a new throwable is created, the template will be used as its cause,
   * possibly with additional wrapping for compatibility.
   *
   * <p>This is used to maintain exception types and states across thread boundaries, such as when an exception cause
   * is obtained from an {@link ExecutionException}.  By wrapping the template, the full stack traces of both threads
   * are maintained.  This new {@link Throwable} provides the full stack trace of the caller, while the template
   * contains the stack trace from the other thread.</p>
   *
   * <p>Only types with registered factories will perform the conversion, otherwise the given cause is returned without
   * wrapping.</p>
   *
   * <p>This current implementation has registered all possible Java SE 8 throwable types, except those that have been
   * removed through Java 17.  Additionally, various throwable implementations
   * {@link #registerSurrogateFactory(java.lang.Class, com.aoapps.lang.ThrowableSurrogateFactory) register themselves}
   * in static blocks, while other APIs may be registered via the {@link ServiceLoader} mechanism on the
   * {@link ThrowableSurrogateFactoryInitializer} interface.</p>
   *
   * @return  When wrapping performed, returns a new throwable of the same class as the template, but with the
   *          caller's stack trace and the template as a cause.  When no wrapping performed, returns the template
   *          itself.
   *
   * @see  ExecutionExceptions
   */
  @SuppressWarnings("unchecked")
  public static <Ex extends Throwable> Ex newSurrogate(Ex template) {
    return newSurrogate(template, template);
  }

  /**
   * Registers a new throwable surrogate factory for a given type.
   */
  @SuppressWarnings("unchecked")
  public static <Ex extends Throwable> void registerSurrogateFactory(
      Class<Ex> exClass,
      ThrowableSurrogateFactory<Ex> factory
  ) {
    if (surrogateFactories.putIfAbsent(exClass, factory) != null) {
      throw new IllegalStateException("Surrogate factory is already registered for " + exClass.getName());
    }
  }

  /**
   * Use for throwables that don't have a compatible cause constructor.
   */
  private static <Ex extends Throwable> Ex initCause(Ex x, Throwable cause) {
    x.initCause(cause);
    return x;
  }

  /**
   * Used by static initializer to register Java SE types, see
   * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/overview-tree.html">https://docs.oracle.com/en/java/javase/11/docs/api/overview-tree.html</a>.
   */
  // Java 12: Review list for any new exception types, bump this note up to next Java version
  @SuppressWarnings("deprecation")
  private static void registerJavaseSurrogateFactories() {
    registerSurrogateFactory(
        java.lang.Throwable.class,
        (template, cause) -> new java.lang.Throwable(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.Error.class,
        (template, cause) -> new java.lang.Error(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.annotation.AnnotationFormatError.class,
        (template, cause) -> new java.lang.annotation.AnnotationFormatError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.AssertionError.class,
        (template, cause) -> new java.lang.AssertionError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.awt.AWTError.class,
        (template, cause) -> initCause(new java.awt.AWTError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.charset.CoderMalfunctionError.class,
        // Does not accept message
        (template, cause) -> (cause instanceof Exception)
            ? new java.nio.charset.CoderMalfunctionError((Exception) cause)
            : new java.nio.charset.CoderMalfunctionError(new WrappedException(template.getMessage(), cause)));
    registerSurrogateFactory(
        javax.xml.parsers.FactoryConfigurationError.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.xml.parsers.FactoryConfigurationError((Exception) cause, template.getMessage())
            : new javax.xml.parsers.FactoryConfigurationError(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.xml.stream.FactoryConfigurationError.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.xml.stream.FactoryConfigurationError(template.getMessage(), (Exception) cause)
            : new javax.xml.stream.FactoryConfigurationError(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.io.IOError.class,
        // Does not accept message
        (template, cause) -> new java.io.IOError(cause));
    registerSurrogateFactory(
        java.lang.LinkageError.class,
        (template, cause) -> new java.lang.LinkageError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.BootstrapMethodError.class,
        (template, cause) -> new java.lang.BootstrapMethodError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.ClassCircularityError.class,
        (template, cause) -> initCause(new java.lang.ClassCircularityError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.ClassFormatError.class,
        (template, cause) -> initCause(new java.lang.ClassFormatError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.reflect.GenericSignatureFormatError.class,
        (template, cause) -> initCause(new java.lang.reflect.GenericSignatureFormatError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.UnsupportedClassVersionError.class,
        (template, cause) -> initCause(new java.lang.UnsupportedClassVersionError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.ExceptionInInitializerError.class,
        // Does not accept message
        (template, cause) -> new java.lang.ExceptionInInitializerError(cause));
    registerSurrogateFactory(
        java.lang.IncompatibleClassChangeError.class,
        (template, cause) -> initCause(new java.lang.IncompatibleClassChangeError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.AbstractMethodError.class,
        (template, cause) -> initCause(new java.lang.AbstractMethodError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.IllegalAccessError.class,
        (template, cause) -> initCause(new java.lang.IllegalAccessError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.InstantiationError.class,
        (template, cause) -> initCause(new java.lang.InstantiationError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.NoSuchFieldError.class,
        (template, cause) -> initCause(new java.lang.NoSuchFieldError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.NoSuchMethodError.class,
        (template, cause) -> initCause(new java.lang.NoSuchMethodError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.NoClassDefFoundError.class,
        (template, cause) -> initCause(new java.lang.NoClassDefFoundError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.UnsatisfiedLinkError.class,
        (template, cause) -> initCause(new java.lang.UnsatisfiedLinkError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.VerifyError.class,
        (template, cause) -> initCause(new java.lang.VerifyError(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.validation.SchemaFactoryConfigurationError.class,
        (template, cause) -> new javax.xml.validation.SchemaFactoryConfigurationError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.util.ServiceConfigurationError.class,
        (template, cause) -> new java.util.ServiceConfigurationError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.ThreadDeath.class,
        // Does not accept message
        (template, cause) -> initCause(new java.lang.ThreadDeath(), cause));
    registerSurrogateFactory(
        javax.xml.transform.TransformerFactoryConfigurationError.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.xml.transform.TransformerFactoryConfigurationError((Exception) cause, template.getMessage())
            : new javax.xml.transform.TransformerFactoryConfigurationError(new WrappedException(cause), template.getMessage()));
    // java.lang.VirtualMachineError: Abstract class
    registerSurrogateFactory(
        java.lang.InternalError.class,
        (template, cause) -> new java.lang.InternalError(template.getMessage(), cause));
    registerSurrogateFactory(
        java.util.zip.ZipError.class,
        (template, cause) -> initCause(new java.util.zip.ZipError(template.getMessage()), cause));
    // java.lang.OutOfMemoryError: Do not try to allocate more memory
    registerSurrogateFactory(
        java.lang.StackOverflowError.class,
        (template, cause) -> initCause(new java.lang.StackOverflowError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.UnknownError.class,
        (template, cause) -> initCause(new java.lang.UnknownError(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.Exception.class,
        (template, cause) -> new java.lang.Exception(template.getMessage(), cause));
    // java.security.acl.AclNotFoundException: Not in Java SE 14+
    // java.rmi.activation.ActivationException: Not in Java SE 17+
    // java.rmi.activation.UnknownGroupException: No way to set cause
    // java.rmi.activation.UnknownObjectException: No way to set cause
    registerSurrogateFactory(
        java.rmi.AlreadyBoundException.class,
        (template, cause) -> initCause(new java.rmi.AlreadyBoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.AWTException.class,
        (template, cause) -> initCause(new java.awt.AWTException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.prefs.BackingStoreException.class,
        // Does not accept message
        (template, cause) -> new java.util.prefs.BackingStoreException(cause));
    // javax.management.BadAttributeValueExpException: Has fields
    registerSurrogateFactory(
        javax.management.BadBinaryOpValueExpException.class,
        (template, cause) -> initCause(new javax.management.BadBinaryOpValueExpException(template.getExp()), cause));
    registerSurrogateFactory(
        javax.swing.text.BadLocationException.class,
        (template, cause) -> initCause(new javax.swing.text.BadLocationException(template.getMessage(), template.offsetRequested()), cause));
    // javax.management.BadStringOperationException: Has fields
    registerSurrogateFactory(
        java.util.concurrent.BrokenBarrierException.class,
        (template, cause) -> initCause(new java.util.concurrent.BrokenBarrierException(template.getMessage()), cause));
    // javax.management.BadStringOperationException: Has fields
    registerSurrogateFactory(
        javax.smartcardio.CardException.class,
        (template, cause) -> new javax.smartcardio.CardException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.smartcardio.CardNotPresentException.class,
        (template, cause) -> new javax.smartcardio.CardNotPresentException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.security.cert.CertificateException.class,
        (template, cause) -> initCause(new javax.security.cert.CertificateException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.cert.CertificateEncodingException.class,
        (template, cause) -> initCause(new javax.security.cert.CertificateEncodingException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.cert.CertificateExpiredException.class,
        (template, cause) -> initCause(new javax.security.cert.CertificateExpiredException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.cert.CertificateNotYetValidException.class,
        (template, cause) -> initCause(new javax.security.cert.CertificateNotYetValidException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.cert.CertificateParsingException.class,
        (template, cause) -> initCause(new javax.security.cert.CertificateParsingException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.CloneNotSupportedException.class,
        (template, cause) -> initCause(new java.lang.CloneNotSupportedException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.rmi.server.ServerCloneException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.server.ServerCloneException(template.getMessage(), (Exception) cause)
            : new java.rmi.server.ServerCloneException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.util.zip.DataFormatException.class,
        (template, cause) -> initCause(new java.util.zip.DataFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.datatype.DatatypeConfigurationException.class,
        (template, cause) -> new javax.xml.datatype.DatatypeConfigurationException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.security.auth.DestroyFailedException.class,
        (template, cause) -> initCause(new javax.security.auth.DestroyFailedException(template.getMessage()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        java.util.concurrent.ExecutionException.class,
        (template, cause) -> new java.util.concurrent.ExecutionException(template.getMessage(), cause));
    // javax.swing.tree.ExpandVetoException: Has fields
    registerSurrogateFactory(
        java.awt.FontFormatException.class,
        (template, cause) -> initCause(new java.awt.FontFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.GeneralSecurityException.class,
        (template, cause) -> new java.security.GeneralSecurityException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.crypto.BadPaddingException.class,
        (template, cause) -> initCause(new javax.crypto.BadPaddingException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.crypto.AEADBadTagException.class,
        (template, cause) -> initCause(new javax.crypto.AEADBadTagException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.cert.CertificateException.class,
        (template, cause) -> new java.security.cert.CertificateException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.cert.CertificateEncodingException.class,
        (template, cause) -> new java.security.cert.CertificateEncodingException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.cert.CertificateExpiredException.class,
        (template, cause) -> initCause(new java.security.cert.CertificateExpiredException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.cert.CertificateNotYetValidException.class,
        (template, cause) -> initCause(new java.security.cert.CertificateNotYetValidException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.cert.CertificateParsingException.class,
        (template, cause) -> new java.security.cert.CertificateParsingException(template.getMessage(), cause));
    // java.security.cert.CertificateRevokedException: Has fields
    registerSurrogateFactory(
        java.security.cert.CertPathBuilderException.class,
        (template, cause) -> new java.security.cert.CertPathBuilderException(template.getMessage(), cause));
    // java.security.cert.CertPathValidatorException: Has fields
    registerSurrogateFactory(
        java.security.cert.CertStoreException.class,
        (template, cause) -> new java.security.cert.CertStoreException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.cert.CRLException.class,
        (template, cause) -> new java.security.cert.CRLException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.DigestException.class,
        (template, cause) -> new java.security.DigestException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.crypto.ExemptionMechanismException.class,
        (template, cause) -> initCause(new javax.crypto.ExemptionMechanismException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.crypto.IllegalBlockSizeException.class,
        (template, cause) -> initCause(new javax.crypto.IllegalBlockSizeException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.InvalidAlgorithmParameterException.class,
        (template, cause) -> new java.security.InvalidAlgorithmParameterException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.spec.InvalidKeySpecException.class,
        (template, cause) -> new java.security.spec.InvalidKeySpecException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.spec.InvalidParameterSpecException.class,
        (template, cause) -> initCause(new java.security.spec.InvalidParameterSpecException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.KeyException.class,
        (template, cause) -> new java.security.KeyException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.InvalidKeyException.class,
        (template, cause) -> new java.security.InvalidKeyException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.KeyManagementException.class,
        (template, cause) -> new java.security.KeyManagementException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.KeyStoreException.class,
        (template, cause) -> new java.security.KeyStoreException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.security.auth.login.LoginException.class,
        (template, cause) -> initCause(new javax.security.auth.login.LoginException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.AccountException.class,
        (template, cause) -> initCause(new javax.security.auth.login.AccountException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.AccountExpiredException.class,
        (template, cause) -> initCause(new javax.security.auth.login.AccountExpiredException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.AccountLockedException.class,
        (template, cause) -> initCause(new javax.security.auth.login.AccountLockedException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.AccountNotFoundException.class,
        (template, cause) -> initCause(new javax.security.auth.login.AccountNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.CredentialException.class,
        (template, cause) -> initCause(new javax.security.auth.login.CredentialException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.CredentialExpiredException.class,
        (template, cause) -> initCause(new javax.security.auth.login.CredentialExpiredException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.CredentialNotFoundException.class,
        (template, cause) -> initCause(new javax.security.auth.login.CredentialNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.login.FailedLoginException.class,
        (template, cause) -> initCause(new javax.security.auth.login.FailedLoginException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.NoSuchAlgorithmException.class,
        (template, cause) -> new java.security.NoSuchAlgorithmException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.crypto.NoSuchPaddingException.class,
        (template, cause) -> initCause(new javax.crypto.NoSuchPaddingException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.NoSuchProviderException.class,
        (template, cause) -> initCause(new java.security.NoSuchProviderException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.crypto.ShortBufferException.class,
        (template, cause) -> initCause(new javax.crypto.ShortBufferException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.SignatureException.class,
        (template, cause) -> initCause(new java.security.SignatureException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.UnrecoverableEntryException.class,
        (template, cause) -> initCause(new java.security.UnrecoverableEntryException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.UnrecoverableKeyException.class,
        (template, cause) -> initCause(new java.security.UnrecoverableKeyException(template.getMessage()), cause));
    // org.ietf.jgss.GSSException: Has fields
    registerSurrogateFactory(
        java.lang.instrument.IllegalClassFormatException.class,
        (template, cause) -> initCause(new java.lang.instrument.IllegalClassFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.InterruptedException.class,
        (template, cause) -> initCause(new java.lang.InterruptedException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.beans.IntrospectionException.class,
        (template, cause) -> initCause(new java.beans.IntrospectionException(template.getMessage()), cause));
    // javax.management.InvalidApplicationException: Has fields
    registerSurrogateFactory(
        javax.sound.midi.InvalidMidiDataException.class,
        (template, cause) -> initCause(new javax.sound.midi.InvalidMidiDataException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.prefs.InvalidPreferencesFormatException.class,
        (template, cause) -> new java.util.prefs.InvalidPreferencesFormatException(template.getMessage(), cause));
    // javax.management.modelmbean.InvalidTargetObjectTypeException: Has fields
    registerSurrogateFactory(
        java.io.IOException.class,
        (template, cause) -> new java.io.IOException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.swing.text.ChangedCharSetException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.swing.text.ChangedCharSetException(template.getCharSetSpec(), template.keyEqualsCharSet()), cause));
    registerSurrogateFactory(
        java.nio.charset.CharacterCodingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.charset.CharacterCodingException(), cause));
    registerSurrogateFactory(
        java.nio.charset.MalformedInputException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.charset.MalformedInputException(template.getInputLength()), cause));
    registerSurrogateFactory(
        java.nio.charset.UnmappableCharacterException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.charset.UnmappableCharacterException(template.getInputLength()), cause));
    registerSurrogateFactory(
        java.io.CharConversionException.class,
        (template, cause) -> initCause(new java.io.CharConversionException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.channels.ClosedChannelException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ClosedChannelException(), cause));
    registerSurrogateFactory(
        java.nio.channels.AsynchronousCloseException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.AsynchronousCloseException(), cause));
    registerSurrogateFactory(
        java.nio.channels.ClosedByInterruptException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ClosedByInterruptException(), cause));
    registerSurrogateFactory(
        java.io.EOFException.class,
        (template, cause) -> initCause(new java.io.EOFException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.channels.FileLockInterruptionException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.FileLockInterruptionException(), cause));
    registerSurrogateFactory(
        java.io.FileNotFoundException.class,
        (template, cause) -> initCause(new java.io.FileNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.annotation.processing.FilerException.class,
        (template, cause) -> initCause(new javax.annotation.processing.FilerException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.file.FileSystemException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.FileSystemException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.nio.file.AccessDeniedException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.AccessDeniedException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.nio.file.AtomicMoveNotSupportedException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.AtomicMoveNotSupportedException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.nio.file.DirectoryNotEmptyException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.DirectoryNotEmptyException(template.getFile()), cause));
    registerSurrogateFactory(
        java.nio.file.FileAlreadyExistsException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.FileAlreadyExistsException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.nio.file.FileSystemLoopException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.FileSystemLoopException(template.getFile()), cause));
    registerSurrogateFactory(
        java.nio.file.NoSuchFileException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.NoSuchFileException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.nio.file.NotDirectoryException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.NotDirectoryException(template.getFile()), cause));
    registerSurrogateFactory(
        java.nio.file.NotLinkException.class,
        // Does not accept message, uses getReason() instead
        (template, cause) -> initCause(new java.nio.file.NotLinkException(template.getFile(), template.getOtherFile(), template.getReason()), cause));
    registerSurrogateFactory(
        java.net.HttpRetryException.class,
        (template, cause) -> initCause(new java.net.HttpRetryException(template.getMessage(), template.responseCode(), template.getLocation()), cause));
    registerSurrogateFactory(
        java.net.http.HttpTimeoutException.class,
        (template, cause) -> initCause(new java.net.http.HttpTimeoutException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.http.HttpConnectTimeoutException.class,
        (template, cause) -> initCause(new java.net.http.HttpConnectTimeoutException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.imageio.IIOException.class,
        (template, cause) -> new javax.imageio.IIOException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.imageio.metadata.IIOInvalidTreeException.class,
        (template, cause) -> new javax.imageio.metadata.IIOInvalidTreeException(template.getMessage(), cause, template.getOffendingNode()));
    registerSurrogateFactory(
        java.nio.channels.InterruptedByTimeoutException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.InterruptedByTimeoutException(), cause));
    registerSurrogateFactory(
        java.io.InterruptedIOException.class,
        (java.io.InterruptedIOException template, Throwable cause) -> {
          java.io.InterruptedIOException newEx = initCause(new java.io.InterruptedIOException(template.getMessage()), cause);
          newEx.bytesTransferred = template.bytesTransferred;
          return newEx;
        });
    registerSurrogateFactory(
        java.net.SocketTimeoutException.class,
        (java.net.SocketTimeoutException template, Throwable cause) -> {
          java.net.SocketTimeoutException newEx = initCause(new java.net.SocketTimeoutException(template.getMessage()), cause);
          newEx.bytesTransferred = template.bytesTransferred;
          return newEx;
        });
    registerSurrogateFactory(
        java.util.InvalidPropertiesFormatException.class,
        (template, cause) -> initCause(new java.util.InvalidPropertiesFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.remote.JMXProviderException.class,
        (template, cause) -> new javax.management.remote.JMXProviderException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.management.remote.JMXServerErrorException.class,
        (template, cause) -> (cause instanceof Error)
            ? new javax.management.remote.JMXServerErrorException(template.getMessage(), (Error) cause)
            : new javax.management.remote.JMXServerErrorException(template.getMessage(), new WrappedError(cause)));
    registerSurrogateFactory(
        java.net.MalformedURLException.class,
        (template, cause) -> initCause(new java.net.MalformedURLException(template.getMessage()), cause));
    // java.io.ObjectStreamException: Abstract class
    // java.io.InvalidClassException: Has fields
    registerSurrogateFactory(
        java.io.InvalidObjectException.class,
        (template, cause) -> initCause(new java.io.InvalidObjectException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.io.NotActiveException.class,
        (template, cause) -> initCause(new java.io.NotActiveException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.io.NotSerializableException.class,
        (template, cause) -> initCause(new java.io.NotSerializableException(template.getMessage()), cause));
    // java.io.OptionalDataException: Package-private constructors
    registerSurrogateFactory(
        java.io.StreamCorruptedException.class,
        (template, cause) -> initCause(new java.io.StreamCorruptedException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.io.WriteAbortedException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.io.WriteAbortedException(template.getMessage(), (Exception) cause)
            : new java.io.WriteAbortedException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.net.ProtocolException.class,
        (template, cause) -> initCause(new java.net.ProtocolException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.rmi.RemoteException.class,
        (template, cause) -> new java.rmi.RemoteException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.rmi.AccessException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.AccessException(template.getMessage(), (Exception) cause)
            : new java.rmi.AccessException(template.getMessage(), new WrappedException(cause)));
    // java.rmi.activation.ActivateFailedException: Not in Java SE 17+
    registerSurrogateFactory(
        java.rmi.ConnectException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.ConnectException(template.getMessage(), (Exception) cause)
            : new java.rmi.ConnectException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.ConnectIOException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.ConnectIOException(template.getMessage(), (Exception) cause)
            : new java.rmi.ConnectIOException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.server.ExportException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.server.ExportException(template.getMessage(), (Exception) cause)
            : new java.rmi.server.ExportException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.server.SocketSecurityException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.server.SocketSecurityException(template.getMessage(), (Exception) cause)
            : new java.rmi.server.SocketSecurityException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.MarshalException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.MarshalException(template.getMessage(), (Exception) cause)
            : new java.rmi.MarshalException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.NoSuchObjectException.class,
        (template, cause) -> initCause(new java.rmi.NoSuchObjectException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.rmi.ServerError.class,
        (template, cause) -> (cause instanceof Error)
            ? new java.rmi.ServerError(template.getMessage(), (Error) cause)
            : new java.rmi.ServerError(template.getMessage(), new WrappedError(cause)));
    registerSurrogateFactory(
        java.rmi.ServerException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.ServerException(template.getMessage(), (Exception) cause)
            : new java.rmi.ServerException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.ServerRuntimeException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.ServerRuntimeException(template.getMessage(), (Exception) cause)
            : new java.rmi.ServerRuntimeException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.server.SkeletonMismatchException.class,
        (template, cause) -> initCause(new java.rmi.server.SkeletonMismatchException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.rmi.server.SkeletonNotFoundException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.server.SkeletonNotFoundException(template.getMessage(), (Exception) cause)
            : new java.rmi.server.SkeletonNotFoundException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.StubNotFoundException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.StubNotFoundException(template.getMessage(), (Exception) cause)
            : new java.rmi.StubNotFoundException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.UnexpectedException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.UnexpectedException(template.getMessage(), (Exception) cause)
            : new java.rmi.UnexpectedException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.UnknownHostException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.UnknownHostException(template.getMessage(), (Exception) cause)
            : new java.rmi.UnknownHostException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.rmi.UnmarshalException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new java.rmi.UnmarshalException(template.getMessage(), (Exception) cause)
            : new java.rmi.UnmarshalException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        javax.security.sasl.SaslException.class,
        (template, cause) -> new javax.security.sasl.SaslException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.security.sasl.AuthenticationException.class,
        (template, cause) -> new javax.security.sasl.AuthenticationException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.net.SocketException.class,
        (template, cause) -> initCause(new java.net.SocketException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.BindException.class,
        (template, cause) -> initCause(new java.net.BindException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.ConnectException.class,
        (template, cause) -> initCause(new java.net.ConnectException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.NoRouteToHostException.class,
        (template, cause) -> initCause(new java.net.NoRouteToHostException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.PortUnreachableException.class,
        (template, cause) -> initCause(new java.net.PortUnreachableException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.net.ssl.SSLException.class,
        (template, cause) -> new javax.net.ssl.SSLException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.net.ssl.SSLHandshakeException.class,
        (template, cause) -> initCause(new javax.net.ssl.SSLHandshakeException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.net.ssl.SSLKeyException.class,
        (template, cause) -> initCause(new javax.net.ssl.SSLKeyException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.net.ssl.SSLPeerUnverifiedException.class,
        (template, cause) -> initCause(new javax.net.ssl.SSLPeerUnverifiedException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.net.ssl.SSLProtocolException.class,
        (template, cause) -> initCause(new javax.net.ssl.SSLProtocolException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.io.SyncFailedException.class,
        (template, cause) -> initCause(new java.io.SyncFailedException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.UnknownHostException.class,
        (template, cause) -> initCause(new java.net.UnknownHostException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.UnknownServiceException.class,
        (template, cause) -> initCause(new java.net.UnknownServiceException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.io.UnsupportedEncodingException.class,
        (template, cause) -> initCause(new java.io.UnsupportedEncodingException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.file.attribute.UserPrincipalNotFoundException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.attribute.UserPrincipalNotFoundException(template.getName()), cause));
    registerSurrogateFactory(
        java.io.UTFDataFormatException.class,
        (template, cause) -> initCause(new java.io.UTFDataFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.net.http.WebSocketHandshakeException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.net.http.WebSocketHandshakeException(template.getResponse()), cause));
    registerSurrogateFactory(
        java.util.zip.ZipException.class,
        (template, cause) -> initCause(new java.util.zip.ZipException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.jar.JarException.class,
        (template, cause) -> initCause(new java.util.jar.JarException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.JMException.class,
        (template, cause) -> initCause(new javax.management.JMException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.MBeanException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.management.MBeanException((Exception) cause, template.getMessage())
            : new javax.management.MBeanException(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.management.MBeanRegistrationException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.management.MBeanRegistrationException((Exception) cause, template.getMessage())
            : new javax.management.MBeanRegistrationException(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.management.openmbean.OpenDataException.class,
        (template, cause) -> initCause(new javax.management.openmbean.OpenDataException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.OperationsException.class,
        (template, cause) -> initCause(new javax.management.OperationsException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.AttributeNotFoundException.class,
        (template, cause) -> initCause(new javax.management.AttributeNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.InstanceAlreadyExistsException.class,
        (template, cause) -> initCause(new javax.management.InstanceAlreadyExistsException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.InstanceNotFoundException.class,
        (template, cause) -> initCause(new javax.management.InstanceNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.IntrospectionException.class,
        (template, cause) -> initCause(new javax.management.IntrospectionException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.InvalidAttributeValueException.class,
        (template, cause) -> initCause(new javax.management.InvalidAttributeValueException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.ListenerNotFoundException.class,
        (template, cause) -> initCause(new javax.management.ListenerNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.MalformedObjectNameException.class,
        (template, cause) -> initCause(new javax.management.MalformedObjectNameException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.NotCompliantMBeanException.class,
        (template, cause) -> initCause(new javax.management.NotCompliantMBeanException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.ServiceNotFoundException.class,
        (template, cause) -> initCause(new javax.management.ServiceNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.ReflectionException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.management.ReflectionException((Exception) cause, template.getMessage())
            : new javax.management.ReflectionException(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.management.relation.RelationException.class,
        (template, cause) -> initCause(new javax.management.relation.RelationException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.InvalidRelationIdException.class,
        (template, cause) -> initCause(new javax.management.relation.InvalidRelationIdException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.InvalidRelationServiceException.class,
        (template, cause) -> initCause(new javax.management.relation.InvalidRelationServiceException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.InvalidRelationTypeException.class,
        (template, cause) -> initCause(new javax.management.relation.InvalidRelationTypeException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.InvalidRoleInfoException.class,
        (template, cause) -> initCause(new javax.management.relation.InvalidRoleInfoException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.InvalidRoleValueException.class,
        (template, cause) -> initCause(new javax.management.relation.InvalidRoleValueException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.RelationNotFoundException.class,
        (template, cause) -> initCause(new javax.management.relation.RelationNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.RelationServiceNotRegisteredException.class,
        (template, cause) -> initCause(new javax.management.relation.RelationServiceNotRegisteredException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.RelationTypeNotFoundException.class,
        (template, cause) -> initCause(new javax.management.relation.RelationTypeNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.RoleInfoNotFoundException.class,
        (template, cause) -> initCause(new javax.management.relation.RoleInfoNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.relation.RoleNotFoundException.class,
        (template, cause) -> initCause(new javax.management.relation.RoleNotFoundException(template.getMessage()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        javax.xml.crypto.KeySelectorException.class,
        (template, cause) -> new javax.xml.crypto.KeySelectorException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.invoke.LambdaConversionException.class,
        (template, cause) -> new java.lang.invoke.LambdaConversionException(template.getMessage(), cause));
    // java.security.acl.LastOwnerException: Not in Java SE 14+
    registerSurrogateFactory(
        javax.sound.sampled.LineUnavailableException.class,
        (template, cause) -> initCause(new javax.sound.sampled.LineUnavailableException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.crypto.MarshalException.class,
        (template, cause) -> new javax.xml.crypto.MarshalException(template.getMessage(), cause));
    registerSurrogateFactory(
        javax.sound.midi.MidiUnavailableException.class,
        (template, cause) -> initCause(new javax.sound.midi.MidiUnavailableException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.datatransfer.MimeTypeParseException.class,
        (template, cause) -> initCause(new java.awt.datatransfer.MimeTypeParseException(template.getMessage()), cause));
    // javax.naming.NamingException.class: Has fields
    // javax.naming.directory.AttributeInUseException: Has fields
    // javax.naming.directory.AttributeModificationException: Has fields
    // javax.naming.CannotProceedException: Has fields
    // javax.naming.CommunicationException: Has fields
    // javax.naming.ConfigurationException: Has fields
    // javax.naming.ContextNotEmptyException: Has fields
    // javax.naming.InsufficientResourcesException: Has fields
    // javax.naming.InterruptedNamingException: Has fields
    // javax.naming.directory.InvalidAttributeIdentifierException: Has fields
    // javax.naming.directory.InvalidAttributesException: Has fields
    // javax.naming.directory.InvalidAttributeValueException: Has fields
    // javax.naming.InvalidNameException: Has fields
    // javax.naming.directory.InvalidSearchControlsException: Has fields
    // javax.naming.directory.InvalidSearchFilterException: Has fields
    // javax.naming.LimitExceededException: Has fields
    // javax.naming.SizeLimitExceededException: Has fields
    // javax.naming.TimeLimitExceededException: Has fields
    // javax.naming.LinkException: Has fields
    // javax.naming.LinkLoopException: Has fields
    // javax.naming.MalformedLinkException: Has fields
    // javax.naming.NameAlreadyBoundException: Has fields
    // javax.naming.NameNotFoundException: Has fields
    // javax.naming.NamingSecurityException: Has fields
    // javax.naming.AuthenticationException: Has fields
    // javax.naming.AuthenticationNotSupportedException: Has fields
    // javax.naming.NoPermissionException: Has fields
    // javax.naming.NoInitialContextException: Has fields
    // javax.naming.directory.NoSuchAttributeException: Has fields
    // javax.naming.NotContextException: Has fields
    // javax.naming.OperationNotSupportedException: Has fields
    // javax.naming.PartialResultException: Has fields
    // javax.naming.ReferralException: Has fields
    // javax.naming.ldap.LdapReferralException: Has fields
    // javax.naming.directory.SchemaViolationException: Has fields
    // javax.naming.ServiceUnavailableException: Has fields
    registerSurrogateFactory(
        java.awt.geom.NoninvertibleTransformException.class,
        (template, cause) -> initCause(new java.awt.geom.NoninvertibleTransformException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.rmi.NotBoundException.class,
        (template, cause) -> initCause(new java.rmi.NotBoundException(template.getMessage()), cause));
    // java.security.acl.NotOwnerException: Not in Java SE 14+
    registerSurrogateFactory(
        java.text.ParseException.class,
        (template, cause) -> initCause(new java.text.ParseException(template.getMessage(), template.getErrorOffset()), cause));
    registerSurrogateFactory(
        javax.xml.parsers.ParserConfigurationException.class,
        (template, cause) -> initCause(new javax.xml.parsers.ParserConfigurationException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.print.PrinterException.class,
        (template, cause) -> initCause(new java.awt.print.PrinterException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.print.PrinterAbortException.class,
        (template, cause) -> initCause(new java.awt.print.PrinterAbortException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.print.PrinterIOException.class,
        // Does not accept message
        (template, cause) -> (cause instanceof java.io.IOException)
            ? new java.awt.print.PrinterIOException((java.io.IOException) cause)
            : new java.awt.print.PrinterIOException(new java.io.IOException(template.getMessage(), cause)));
    registerSurrogateFactory(
        javax.print.PrintException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.print.PrintException(template.getMessage(), (Exception) cause)
            : new javax.print.PrintException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        java.security.PrivilegedActionException.class,
        // Does not accept message
        (template, cause) -> (cause instanceof Exception)
            ? new java.security.PrivilegedActionException((Exception) cause)
            : new java.security.PrivilegedActionException(new WrappedException(template.getMessage(), cause)));
    registerSurrogateFactory(
        java.beans.PropertyVetoException.class,
        (template, cause) -> initCause(new java.beans.PropertyVetoException(template.getMessage(), template.getPropertyChangeEvent()), cause));
    registerSurrogateFactory(
        java.lang.ReflectiveOperationException.class,
        (template, cause) -> new java.lang.ReflectiveOperationException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.ClassNotFoundException.class,
        (template, cause) -> new java.lang.ClassNotFoundException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.IllegalAccessException.class,
        (template, cause) -> initCause(new java.lang.IllegalAccessException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.InstantiationException.class,
        (template, cause) -> initCause(new java.lang.InstantiationException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.reflect.InvocationTargetException.class,
        (template, cause) -> new java.lang.reflect.InvocationTargetException(cause, template.getMessage()));
    registerSurrogateFactory(
        java.lang.NoSuchFieldException.class,
        (template, cause) -> initCause(new java.lang.NoSuchFieldException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.NoSuchMethodException.class,
        (template, cause) -> initCause(new java.lang.NoSuchMethodException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.RefreshFailedException.class,
        (template, cause) -> initCause(new javax.security.auth.RefreshFailedException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.RuntimeException.class,
        (template, cause) -> new java.lang.RuntimeException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.annotation.AnnotationTypeMismatchException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.lang.annotation.AnnotationTypeMismatchException(template.element(), template.foundType()), cause));
    registerSurrogateFactory(
        java.lang.ArithmeticException.class,
        (template, cause) -> initCause(new java.lang.ArithmeticException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.ArrayStoreException.class,
        (template, cause) -> initCause(new java.lang.ArrayStoreException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.BufferOverflowException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.BufferOverflowException(), cause));
    registerSurrogateFactory(
        java.nio.BufferUnderflowException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.BufferUnderflowException(), cause));
    registerSurrogateFactory(
        javax.swing.undo.CannotRedoException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.swing.undo.CannotRedoException(), cause));
    registerSurrogateFactory(
        javax.swing.undo.CannotUndoException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.swing.undo.CannotUndoException(), cause));
    registerSurrogateFactory(
        javax.xml.catalog.CatalogException.class,
        (template, cause) -> new javax.xml.catalog.CatalogException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.ClassCastException.class,
        (template, cause) -> initCause(new java.lang.ClassCastException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.color.CMMException.class,
        (template, cause) -> initCause(new java.awt.color.CMMException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.concurrent.CompletionException.class,
        (template, cause) -> new java.util.concurrent.CompletionException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.util.ConcurrentModificationException.class,
        (template, cause) -> new java.util.ConcurrentModificationException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.nio.file.DirectoryIteratorException.class,
        // Does not accept message
        (template, cause) -> (cause instanceof java.io.IOException)
            ? new java.nio.file.DirectoryIteratorException((java.io.IOException) cause)
            : new java.nio.file.DirectoryIteratorException(new java.io.IOException(template.getMessage(), cause)));
    registerSurrogateFactory(
        java.time.DateTimeException.class,
        (template, cause) -> new java.time.DateTimeException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.time.format.DateTimeParseException.class,
        (template, cause) -> new java.time.format.DateTimeParseException(template.getMessage(), template.getParsedString(), template.getErrorIndex(), cause));
    registerSurrogateFactory(
        java.time.temporal.UnsupportedTemporalTypeException.class,
        (template, cause) -> new java.time.temporal.UnsupportedTemporalTypeException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.time.zone.ZoneRulesException.class,
        (template, cause) -> new java.time.zone.ZoneRulesException(template.getMessage(), cause));
    registerSurrogateFactory(
        org.w3c.dom.DOMException.class,
        (template, cause) -> initCause(new org.w3c.dom.DOMException(template.code, template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.EmptyStackException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.EmptyStackException(), cause));
    registerSurrogateFactory(
        java.lang.EnumConstantNotPresentException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.lang.EnumConstantNotPresentException(template.enumType(), template.constantName()), cause));
    registerSurrogateFactory(
        org.w3c.dom.events.EventException.class,
        (template, cause) -> initCause(new org.w3c.dom.events.EventException(template.code, template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.file.FileSystemAlreadyExistsException.class,
        (template, cause) -> initCause(new java.nio.file.FileSystemAlreadyExistsException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.file.FileSystemNotFoundException.class,
        (template, cause) -> initCause(new java.nio.file.FileSystemNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.module.FindException.class,
        (template, cause) -> initCause(new java.lang.module.FindException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.IllegalArgumentException.class,
        (template, cause) -> new java.lang.IllegalArgumentException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.nio.channels.IllegalChannelGroupException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.IllegalChannelGroupException(), cause));
    registerSurrogateFactory(
        java.nio.charset.IllegalCharsetNameException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.charset.IllegalCharsetNameException(template.getCharsetName()), cause));
    // java.util.IllegalFormatException: Package-private constructors
    registerSurrogateFactory(
        java.util.DuplicateFormatFlagsException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.DuplicateFormatFlagsException(template.getFlags()), cause));
    registerSurrogateFactory(
        java.util.FormatFlagsConversionMismatchException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.FormatFlagsConversionMismatchException(template.getFlags(), template.getConversion()), cause));
    registerSurrogateFactory(
        java.util.IllegalFormatCodePointException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.IllegalFormatCodePointException(template.getCodePoint()), cause));
    registerSurrogateFactory(
        java.util.IllegalFormatConversionException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.IllegalFormatConversionException(template.getConversion(), template.getArgumentClass()), cause));
    registerSurrogateFactory(
        java.util.IllegalFormatFlagsException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.IllegalFormatFlagsException(template.getFlags()), cause));
    registerSurrogateFactory(
        java.util.IllegalFormatPrecisionException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.IllegalFormatPrecisionException(template.getPrecision()), cause));
    registerSurrogateFactory(
        java.util.IllegalFormatWidthException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.IllegalFormatWidthException(template.getWidth()), cause));
    registerSurrogateFactory(
        java.util.MissingFormatArgumentException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.MissingFormatArgumentException(template.getFormatSpecifier()), cause));
    registerSurrogateFactory(
        java.util.MissingFormatWidthException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.MissingFormatWidthException(template.getFormatSpecifier()), cause));
    registerSurrogateFactory(
        java.util.UnknownFormatConversionException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.UnknownFormatConversionException(template.getConversion()), cause));
    registerSurrogateFactory(
        java.util.UnknownFormatFlagsException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.UnknownFormatFlagsException(template.getFlags()), cause));
    registerSurrogateFactory(
        java.nio.channels.IllegalSelectorException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.IllegalSelectorException(), cause));
    registerSurrogateFactory(
        java.lang.IllegalThreadStateException.class,
        (template, cause) -> initCause(new java.lang.IllegalThreadStateException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.openmbean.InvalidKeyException.class,
        (template, cause) -> initCause(new javax.management.openmbean.InvalidKeyException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.openmbean.InvalidOpenTypeException.class,
        (template, cause) -> initCause(new javax.management.openmbean.InvalidOpenTypeException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.InvalidParameterException.class,
        // Java 20: Added Constructors (String, Throwable) and (Throwable) to InvalidParameterException
        (template, cause) -> initCause(new java.security.InvalidParameterException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.file.InvalidPathException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.InvalidPathException(template.getInput(), template.getReason(), template.getIndex()), cause));
    registerSurrogateFactory(
        javax.management.openmbean.KeyAlreadyExistsException.class,
        (template, cause) -> initCause(new javax.management.openmbean.KeyAlreadyExistsException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.NumberFormatException.class,
        (template, cause) -> initCause(new java.lang.NumberFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.regex.PatternSyntaxException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.regex.PatternSyntaxException(template.getDescription(), template.getPattern(), template.getIndex()), cause));
    registerSurrogateFactory(
        java.nio.file.ProviderMismatchException.class,
        (template, cause) -> initCause(new java.nio.file.ProviderMismatchException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.channels.UnresolvedAddressException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.UnresolvedAddressException(), cause));
    registerSurrogateFactory(
        java.nio.channels.UnsupportedAddressTypeException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.UnsupportedAddressTypeException(), cause));
    registerSurrogateFactory(
        java.nio.charset.UnsupportedCharsetException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.charset.UnsupportedCharsetException(template.getCharsetName()), cause));
    registerSurrogateFactory(
        java.lang.IllegalCallerException.class,
        (template, cause) -> new java.lang.IllegalCallerException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.IllegalMonitorStateException.class,
        (template, cause) -> initCause(new java.lang.IllegalMonitorStateException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.geom.IllegalPathStateException.class,
        (template, cause) -> initCause(new java.awt.geom.IllegalPathStateException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.IllegalStateException.class,
        (template, cause) -> new java.lang.IllegalStateException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.nio.channels.AcceptPendingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.AcceptPendingException(), cause));
    registerSurrogateFactory(
        java.nio.channels.AlreadyBoundException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.AlreadyBoundException(), cause));
    registerSurrogateFactory(
        java.nio.channels.AlreadyConnectedException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.AlreadyConnectedException(), cause));
    registerSurrogateFactory(
        java.util.concurrent.CancellationException.class,
        (template, cause) -> initCause(new java.util.concurrent.CancellationException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.channels.CancelledKeyException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.CancelledKeyException(), cause));
    registerSurrogateFactory(
        java.nio.file.ClosedDirectoryStreamException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.ClosedDirectoryStreamException(), cause));
    registerSurrogateFactory(
        java.nio.file.ClosedFileSystemException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.ClosedFileSystemException(), cause));
    registerSurrogateFactory(
        java.nio.channels.ClosedSelectorException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ClosedSelectorException(), cause));
    registerSurrogateFactory(
        java.nio.file.ClosedWatchServiceException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.ClosedWatchServiceException(), cause));
    registerSurrogateFactory(
        java.nio.channels.ConnectionPendingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ConnectionPendingException(), cause));
    registerSurrogateFactory(
        java.util.FormatterClosedException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.util.FormatterClosedException(), cause));
    registerSurrogateFactory(
        java.nio.channels.IllegalBlockingModeException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.IllegalBlockingModeException(), cause));
    registerSurrogateFactory(
        java.awt.IllegalComponentStateException.class,
        (template, cause) -> initCause(new java.awt.IllegalComponentStateException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.dnd.InvalidDnDOperationException.class,
        (template, cause) -> initCause(new java.awt.dnd.InvalidDnDOperationException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.InvalidMarkException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.InvalidMarkException(), cause));
    registerSurrogateFactory(
        java.nio.channels.NoConnectionPendingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.NoConnectionPendingException(), cause));
    registerSurrogateFactory(
        java.nio.channels.NonReadableChannelException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.NonReadableChannelException(), cause));
    registerSurrogateFactory(
        java.nio.channels.NonWritableChannelException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.NonWritableChannelException(), cause));
    registerSurrogateFactory(
        java.nio.channels.NotYetBoundException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.NotYetBoundException(), cause));
    registerSurrogateFactory(
        java.nio.channels.NotYetConnectedException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.NotYetConnectedException(), cause));
    registerSurrogateFactory(
        java.nio.channels.OverlappingFileLockException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.OverlappingFileLockException(), cause));
    registerSurrogateFactory(
        java.nio.channels.ReadPendingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ReadPendingException(), cause));
    registerSurrogateFactory(
        java.nio.channels.ShutdownChannelGroupException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.ShutdownChannelGroupException(), cause));
    registerSurrogateFactory(
        java.nio.channels.WritePendingException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.channels.WritePendingException(), cause));
    registerSurrogateFactory(
        java.util.IllformedLocaleException.class,
        (template, cause) -> initCause(new java.util.IllformedLocaleException(template.getMessage(), template.getErrorIndex()), cause));
    registerSurrogateFactory(
        java.awt.image.ImagingOpException.class,
        (template, cause) -> initCause(new java.awt.image.ImagingOpException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.reflect.InaccessibleObjectException.class,
        (template, cause) -> initCause(new java.lang.reflect.InaccessibleObjectException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.annotation.IncompleteAnnotationException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.lang.annotation.IncompleteAnnotationException(template.annotationType(), template.elementName()), cause));
    registerSurrogateFactory(
        java.lang.IndexOutOfBoundsException.class,
        (template, cause) -> initCause(new java.lang.IndexOutOfBoundsException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.ArrayIndexOutOfBoundsException.class,
        (template, cause) -> initCause(new java.lang.ArrayIndexOutOfBoundsException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.StringIndexOutOfBoundsException.class,
        (template, cause) -> initCause(new java.lang.StringIndexOutOfBoundsException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.module.InvalidModuleDescriptorException.class,
        (template, cause) -> initCause(new java.lang.module.InvalidModuleDescriptorException(template.getMessage()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        javax.management.JMRuntimeException.class,
        (template, cause) -> initCause(new javax.management.JMRuntimeException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.monitor.MonitorSettingException.class,
        (template, cause) -> initCause(new javax.management.monitor.MonitorSettingException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.management.RuntimeErrorException.class,
        (template, cause) -> (cause instanceof java.lang.Error)
            ? new javax.management.RuntimeErrorException((java.lang.Error) cause, template.getMessage())
            : new javax.management.RuntimeErrorException(new WrappedError(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.management.RuntimeMBeanException.class,
        (template, cause) -> (cause instanceof java.lang.RuntimeException)
            ? new javax.management.RuntimeMBeanException((java.lang.RuntimeException) cause, template.getMessage())
            : new javax.management.RuntimeMBeanException(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.management.RuntimeOperationsException.class,
        (template, cause) -> (cause instanceof java.lang.RuntimeException)
            ? new javax.management.RuntimeOperationsException((java.lang.RuntimeException) cause, template.getMessage())
            : new javax.management.RuntimeOperationsException(new WrappedException(cause), template.getMessage()));
    // Skipping netscape.javascript.*
    registerSurrogateFactory(
        java.lang.LayerInstantiationException.class,
        (template, cause) -> new java.lang.LayerInstantiationException(template.getMessage(), cause));
    registerSurrogateFactory(
        org.w3c.dom.ls.LSException.class,
        (template, cause) -> initCause(new org.w3c.dom.ls.LSException(template.code, template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.reflect.MalformedParameterizedTypeException.class,
        (template, cause) -> initCause(new java.lang.reflect.MalformedParameterizedTypeException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.reflect.MalformedParametersException.class,
        (template, cause) -> initCause(new java.lang.reflect.MalformedParametersException(template.getMessage()), cause));
    // javax.lang.model.type.MirroredTypesException: Has fields
    // javax.lang.model.type.MirroredTypeException: Has fields
    registerSurrogateFactory(
        java.util.MissingResourceException.class,
        (template, cause) -> initCause(new java.util.MissingResourceException(template.getMessage(), template.getClassName(), template.getKey()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        java.lang.NegativeArraySizeException.class,
        (template, cause) -> initCause(new java.lang.NegativeArraySizeException(template.getMessage()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        java.util.NoSuchElementException.class,
        (template, cause) -> initCause(new java.util.NoSuchElementException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.InputMismatchException.class,
        (template, cause) -> initCause(new java.util.InputMismatchException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.crypto.NoSuchMechanismException.class,
        (template, cause) -> new javax.xml.crypto.NoSuchMechanismException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.NullPointerException.class,
        (template, cause) -> initCause(new java.lang.NullPointerException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.color.ProfileDataException.class,
        (template, cause) -> initCause(new java.awt.color.ProfileDataException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.security.ProviderException.class,
        (template, cause) -> new java.security.ProviderException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.nio.file.ProviderNotFoundException.class,
        (template, cause) -> initCause(new java.nio.file.ProviderNotFoundException(template.getMessage()), cause));
    registerSurrogateFactory(
        org.w3c.dom.ranges.RangeException.class,
        (template, cause) -> initCause(new org.w3c.dom.ranges.RangeException(template.code, template.getMessage()), cause));
    registerSurrogateFactory(
        java.awt.image.RasterFormatException.class,
        (template, cause) -> initCause(new java.awt.image.RasterFormatException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.concurrent.RejectedExecutionException.class,
        (template, cause) -> new java.util.concurrent.RejectedExecutionException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.module.ResolutionException.class,
        (template, cause) -> new java.lang.module.ResolutionException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.SecurityException.class,
        (template, cause) -> new java.lang.SecurityException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.security.AccessControlException.class,
        (template, cause) -> initCause(new java.security.AccessControlException(template.getMessage(), template.getPermission()), cause));
    // java.rmi.RMISecurityException: deprecated no replacement
    // Skipping jdk.*
    registerSurrogateFactory(
        java.lang.TypeNotPresentException.class,
        // Does not accept message
        (template, cause) -> new java.lang.TypeNotPresentException(template.typeName(), cause));
    registerSurrogateFactory(
        java.io.UncheckedIOException.class,
        (template, cause) -> (cause instanceof java.io.IOException)
            ? new java.io.UncheckedIOException(template.getMessage(), (java.io.IOException) cause)
            : new java.io.UncheckedIOException(template.getMessage(), new java.io.IOException(template.getMessage(), cause)));
    registerSurrogateFactory(
        java.lang.reflect.UndeclaredThrowableException.class,
        (template, cause) -> new java.lang.reflect.UndeclaredThrowableException(cause, template.getMessage()));
    // javax.lang.model.UnknownEntityException: Protected constructor
    registerSurrogateFactory(
        javax.lang.model.element.UnknownAnnotationValueException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.lang.model.element.UnknownAnnotationValueException(template.getUnknownAnnotationValue(), template.getArgument()), cause));
    registerSurrogateFactory(
        javax.lang.model.element.UnknownDirectiveException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.lang.model.element.UnknownDirectiveException(template.getUnknownDirective(), template.getArgument()), cause));
    registerSurrogateFactory(
        javax.lang.model.element.UnknownElementException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.lang.model.element.UnknownElementException(template.getUnknownElement(), template.getArgument()), cause));
    registerSurrogateFactory(
        javax.lang.model.type.UnknownTypeException.class,
        // Does not accept message
        (template, cause) -> initCause(new javax.lang.model.type.UnknownTypeException(template.getUnknownType(), template.getArgument()), cause));
    // Skipping jdk.*
    registerSurrogateFactory(
        javax.print.attribute.UnmodifiableSetException.class,
        (template, cause) -> initCause(new javax.print.attribute.UnmodifiableSetException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.lang.UnsupportedOperationException.class,
        (template, cause) -> new java.lang.UnsupportedOperationException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.awt.HeadlessException.class,
        (template, cause) -> initCause(new java.awt.HeadlessException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.nio.ReadOnlyBufferException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.ReadOnlyBufferException(), cause));
    registerSurrogateFactory(
        java.nio.file.ReadOnlyFileSystemException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.nio.file.ReadOnlyFileSystemException(), cause));
    registerSurrogateFactory(
        java.lang.invoke.WrongMethodTypeException.class,
        (template, cause) -> initCause(new java.lang.invoke.WrongMethodTypeException(template.getMessage()), cause));
    registerSurrogateFactory(
        org.xml.sax.SAXException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new org.xml.sax.SAXException(template.getMessage(), (Exception) cause)
            : new org.xml.sax.SAXException(template.getMessage(), new WrappedException(cause)));
    registerSurrogateFactory(
        org.xml.sax.SAXNotRecognizedException.class,
        (template, cause) -> initCause(new org.xml.sax.SAXNotRecognizedException(template.getMessage()), cause));
    registerSurrogateFactory(
        org.xml.sax.SAXNotSupportedException.class,
        (template, cause) -> initCause(new org.xml.sax.SAXNotSupportedException(template.getMessage()), cause));
    // org.xml.sax.SAXParseException: Has fields
    // javax.script.ScriptException: Has fields
    registerSurrogateFactory(
        java.rmi.server.ServerNotActiveException.class,
        (template, cause) -> initCause(new java.rmi.server.ServerNotActiveException(template.getMessage()), cause));
    // javax.xml.soap.SOAPException: Not in Java SE 11, don't add dependency on jaxb
    registerSurrogateFactory(
        java.sql.SQLException.class,
        (template, cause) -> new java.sql.SQLException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.BatchUpdateException.class,
        (java.sql.BatchUpdateException template, Throwable cause) -> {
          int[] updateCounts = template.getUpdateCounts();
          if (updateCounts != null) {
            return new java.sql.BatchUpdateException(
                template.getMessage(),
                template.getSQLState(),
                template.getErrorCode(),
                updateCounts,
                cause
            );
          }
          return new java.sql.BatchUpdateException(
              template.getMessage(),
              template.getSQLState(),
              template.getErrorCode(),
              template.getLargeUpdateCounts(),
              cause
          );
        });
    registerSurrogateFactory(
        javax.sql.rowset.RowSetWarning.class,
        (template, cause) -> initCause(new javax.sql.rowset.RowSetWarning(template.getMessage(), template.getSQLState(), template.getErrorCode()), cause));
    registerSurrogateFactory(
        javax.sql.rowset.serial.SerialException.class,
        (template, cause) -> initCause(new javax.sql.rowset.serial.SerialException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.sql.SQLClientInfoException.class,
        (template, cause) -> new java.sql.SQLClientInfoException(template.getMessage(), template.getSQLState(), template.getErrorCode(), template.getFailedProperties(), cause));
    registerSurrogateFactory(
        java.sql.SQLNonTransientException.class,
        (template, cause) -> new java.sql.SQLNonTransientException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLDataException.class,
        (template, cause) -> new java.sql.SQLDataException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLFeatureNotSupportedException.class,
        (template, cause) -> new java.sql.SQLFeatureNotSupportedException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLIntegrityConstraintViolationException.class,
        (template, cause) -> new java.sql.SQLIntegrityConstraintViolationException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLInvalidAuthorizationSpecException.class,
        (template, cause) -> new java.sql.SQLInvalidAuthorizationSpecException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLNonTransientConnectionException.class,
        (template, cause) -> new java.sql.SQLNonTransientConnectionException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLSyntaxErrorException.class,
        (template, cause) -> new java.sql.SQLSyntaxErrorException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLRecoverableException.class,
        (template, cause) -> new java.sql.SQLRecoverableException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLTransientException.class,
        (template, cause) -> new java.sql.SQLTransientException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLTimeoutException.class,
        (template, cause) -> new java.sql.SQLTimeoutException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLTransactionRollbackException.class,
        (template, cause) -> new java.sql.SQLTransactionRollbackException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLTransientConnectionException.class,
        (template, cause) -> new java.sql.SQLTransientConnectionException(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.SQLWarning.class,
        (template, cause) -> new java.sql.SQLWarning(template.getMessage(), template.getSQLState(), template.getErrorCode(), cause));
    registerSurrogateFactory(
        java.sql.DataTruncation.class,
        // Does not accept message
        (template, cause) -> new java.sql.DataTruncation(template.getIndex(), template.getParameter(), template.getRead(), template.getDataSize(), template.getTransferSize(), cause));
    registerSurrogateFactory(
        javax.sql.rowset.spi.SyncFactoryException.class,
        (template, cause) -> initCause(new javax.sql.rowset.spi.SyncFactoryException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.sql.rowset.spi.SyncProviderException.class,
        (javax.sql.rowset.spi.SyncProviderException template, Throwable cause) -> {
          javax.sql.rowset.spi.SyncProviderException newEx = initCause(new javax.sql.rowset.spi.SyncProviderException(template.getMessage()), cause);
          newEx.setSyncResolver(template.getSyncResolver());
          return newEx;
        });
    registerSurrogateFactory(
        java.lang.invoke.StringConcatException.class,
        (template, cause) -> new java.lang.invoke.StringConcatException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.util.concurrent.TimeoutException.class,
        (template, cause) -> initCause(new java.util.concurrent.TimeoutException(template.getMessage()), cause));
    registerSurrogateFactory(
        java.util.TooManyListenersException.class,
        (template, cause) -> initCause(new java.util.TooManyListenersException(template.getMessage()), cause));
    // javax.xml.transform.TransformerException: Has fields
    // javax.xml.transform.TransformerConfigurationException: Has fields
    registerSurrogateFactory(
        javax.xml.crypto.dsig.TransformException.class,
        (template, cause) -> new javax.xml.crypto.dsig.TransformException(template.getMessage(), cause));
    registerSurrogateFactory(
        java.lang.instrument.UnmodifiableClassException.class,
        (template, cause) -> initCause(new java.lang.instrument.UnmodifiableClassException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.sound.sampled.UnsupportedAudioFileException.class,
        (template, cause) -> initCause(new javax.sound.sampled.UnsupportedAudioFileException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.security.auth.callback.UnsupportedCallbackException.class,
        (template, cause) -> initCause(new javax.security.auth.callback.UnsupportedCallbackException(template.getCallback(), template.getMessage()), cause));
    // java.awt.datatransfer.UnsupportedFlavorException: No DataFlavor to pass to constructor
    registerSurrogateFactory(
        javax.swing.UnsupportedLookAndFeelException.class,
        (template, cause) -> initCause(new javax.swing.UnsupportedLookAndFeelException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.crypto.URIReferenceException.class,
        (template, cause) -> new javax.xml.crypto.URIReferenceException(template.getMessage(), cause, template.getURIReference()));
    registerSurrogateFactory(
        java.net.URISyntaxException.class,
        // Does not accept message
        (template, cause) -> initCause(new java.net.URISyntaxException(template.getInput(), template.getReason(), template.getIndex()), cause));
    registerSurrogateFactory(
        javax.transaction.xa.XAException.class, (javax.transaction.xa.XAException template, Throwable cause) -> {
          javax.transaction.xa.XAException newEx = initCause(new javax.transaction.xa.XAException(template.getMessage()), cause);
          newEx.errorCode = template.errorCode;
          return newEx;
        });
    registerSurrogateFactory(
        javax.management.modelmbean.XMLParseException.class,
        (template, cause) -> (cause instanceof Exception)
            ? new javax.management.modelmbean.XMLParseException((Exception) cause, template.getMessage())
            : new javax.management.modelmbean.XMLParseException(new WrappedException(cause), template.getMessage()));
    registerSurrogateFactory(
        javax.xml.crypto.dsig.XMLSignatureException.class,
        (template, cause) -> new javax.xml.crypto.dsig.XMLSignatureException(template.getMessage(), cause));
    // javax.xml.stream.XMLStreamException: Has fields
    registerSurrogateFactory(
        javax.xml.xpath.XPathException.class,
        (template, cause) -> initCause(new javax.xml.xpath.XPathException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.xpath.XPathExpressionException.class,
        (template, cause) -> initCause(new javax.xml.xpath.XPathExpressionException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.xpath.XPathFunctionException.class,
        (template, cause) -> initCause(new javax.xml.xpath.XPathFunctionException(template.getMessage()), cause));
    registerSurrogateFactory(
        javax.xml.xpath.XPathFactoryConfigurationException.class,
        (template, cause) -> initCause(new javax.xml.xpath.XPathFactoryConfigurationException(template.getMessage()), cause));
  }

  static {
    registerJavaseSurrogateFactories();
    // TODO: Java 9: Maintain modularity? Could also possibly be optional module deps, with one service loader per module?
    for (ThrowableSurrogateFactoryInitializer initializer : ServiceLoader.load(ThrowableSurrogateFactoryInitializer.class)) {
      initializer.run();
    }
  }
}
