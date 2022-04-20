/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.attribute;

import com.aoapps.lang.function.BiFunctionE;
import com.aoapps.lang.function.FunctionE;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * An attribute has scope, context, and name and is used for value access.
 *
 * @see  Context
 * @see  Scope
 *
 * @author  AO Industries, Inc.
 */
public abstract class Attribute<C, T> {

  protected final String name;

  protected Attribute(String name) {
    this.name = name;
  }

  /**
   * Gets the context for this attribute.
   *
   * @return  the context or {@code null} when none
   */
  public abstract Context<C> getContext();

  /**
   * Gets the attribute name.
   */
  public String getName() {
    return name;
  }

  /**
   * A backup value from before attribute initialization,
   * which must be {@link #close() closed} to restore the old value.
   * This is best used in try-with-resources.
   */
  public abstract static class OldValue implements AutoCloseable {

    private final Object oldValue;

    protected OldValue(Object oldValue) {
      this.oldValue = oldValue;
    }

    /**
     * Gets the old value of the attribute, which will be restored on {@link #close()}.
     * This can be of any type, since this is used to backup arbitrary values before new scopes.
     *
     * @return  the old attribute value or {@code null} when none
     */
    public Object getOldValue() {
      return oldValue;
    }

    /**
     * Restores the old value of the attribute.
     */
    @Override
    public abstract void close();
  }

  /**
   * Initializes this attribute,
   * returning a backup value, which must be {@link OldValue#close() closed} to restore the old value.
   * This is best used in try-with-resources.
   */
  public abstract OldValue init(T value);

  /**
   * Much like {@link Map#compute(java.lang.Object, java.util.function.BiFunction)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#compute(java.lang.Object, java.util.function.BiFunction)
   */
  public abstract <Ex extends Throwable> T compute(
    BiFunctionE<? super String, ? super T, ? extends T, ? extends Ex> remappingFunction
  ) throws Ex;

  /**
   * Much like {@link Map#computeIfAbsent(java.lang.Object, java.util.function.Function)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#computeIfAbsent(java.lang.Object, java.util.function.Function)
   */
  public abstract <Ex extends Throwable> T computeIfAbsent(
    FunctionE<? super String, ? extends T, ? extends Ex> mappingFunction
  ) throws Ex;

  /**
   * Much like {@link Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)
   */
  public abstract <Ex extends Throwable> T computeIfPresent(
    BiFunctionE<? super String, ? super T, ? extends T, ? extends Ex> remappingFunction
  ) throws Ex;

  /**
   * Gets the value of this attribute.
   *
   * @return  {@link #getContext()} may be {@code null}, which will return {@code null}
   */
  public abstract T get();

  /**
   * Much like {@link Map#getOrDefault(java.lang.Object, java.lang.Object)},
   * but for this attribute.
   *
   * @return  {@link #getContext()} may be {@code null}, which will return {@code defaultValue}
   *
   * @see  Map#getOrDefault(java.lang.Object, java.lang.Object)
   */
  public abstract T getOrDefault(T defaultValue);

  /**
   * Much like {@link Map#merge(java.lang.Object, java.lang.Object, java.util.function.BiFunction)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#merge(java.lang.Object, java.lang.Object, java.util.function.BiFunction)
   */
  public abstract <Ex extends Throwable> T merge(
    T value,
    BiFunctionE<? super T, ? super T, ? extends T, ? extends Ex> remappingFunction
  ) throws Ex;

  /**
   * Removes the value from this attribute.
   * <p>
   * {@link #getContext()} may be {@code null}, which will skip removal
   * </p>
   */
  public abstract void remove();

  /**
   * Much like {@link Map#remove(java.lang.Object, java.lang.Object)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#remove(java.lang.Object, java.lang.Object)
   */
  public abstract boolean remove(T value);

  /**
   * Much like {@link Map#replace(java.lang.Object, java.lang.Object)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#replace(java.lang.Object, java.lang.Object)
   */
  public abstract T replace(T value);

  /**
   * Much like {@link Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public abstract boolean replace(T oldValue, T newValue);

  /**
   * Sets the value of this attribute.
   */
  public abstract void set(T value);

  /**
   * Much like {@link Map#putIfAbsent(java.lang.Object, java.lang.Object)},
   * but for this attribute.
   * Synchronizes on context to ensure atomic operation.
   *
   * @see  Map#putIfAbsent(java.lang.Object, java.lang.Object)
   */
  public abstract T setIfAbsent(T value);

  // <editor-fold desc="Name">
  /**
   * A name without any specific scope or context.
   * <p>
   * {@link Attribute}: Has name, still needs scope or context.
   * </p>
   */
  public static class Name<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String name;

    protected Name(String name) {
      this.name = name;
    }

    /**
     * Gets the attribute Name.
     */
    public String getName() {
      return name;
    }

    // <editor-fold desc="Scope">
    /**
     * Supports scope attributes in extensible scopes, loaded via {@link ServiceLoader#load(java.lang.Class)} on each
     * access to {@link #scope(java.lang.Class)}.
     *
     * @see  Name#scope(java.lang.Class)
     */
    @FunctionalInterface
    public static interface ScopeFactory<C, T> {
      /**
       * Gets the scope attribute for the given context type and name or {@code null} if not handled by this factory.
       */
      Scope.Attribute<C, T> attribute(Class<?> contextType, String name);
    }

    /**
     * {@link Attribute}: Uses the given scope (located by content type) and this name, still needs context.
     * <p>
     * <strong>Performance:</strong> This loads {@link ScopeFactory} via {@link ServiceLoader#load(java.lang.Class)} on
     * every call.  It is strongly encouraged to use subclass-specific implementations where performance is critical.
     * </p>
     */
    public <C> Scope.Attribute<C, T> scope(Class<C> contextType) {
      @SuppressWarnings("unchecked")
      ServiceLoader<ScopeFactory<C, T>> loader = (ServiceLoader)ServiceLoader.load(ContextFactory.class);
      Iterator<ScopeFactory<C, T>> iter = loader.iterator();
      while (iter.hasNext()) {
        Scope.Attribute<C, T> attribute = iter.next().attribute(contextType, name);
        if (attribute != null) {
          return attribute;
        }
      }
      throw new IllegalArgumentException(
        "No factory registered for scope for context type \""
        + (contextType == null ? "null" : contextType.toGenericString()) + "\""
      );
    }

    /**
     * {@link Attribute}: Uses the given scope and this name, still needs context.
     */
    public <C> Scope.Attribute<C, T> scope(Scope<C> scope) {
      return scope.attribute(name);
    }
    // </editor-fold>

    // <editor-fold desc="Context">
    /**
     * Supports attributes in extensible contexts, loaded via {@link ServiceLoader#load(java.lang.Class)} on each
     * access to {@link #context(java.lang.Object)}.
     *
     * @see  Name#context(java.lang.Object)
     */
    @FunctionalInterface
    public static interface ContextFactory<C, T> {
      /**
       * Gets the attribute for the given context and name or {@code null} if not handled by this factory.
       */
      Attribute<C, T> attribute(Object context, String name);
    }

    /**
     * {@link Attribute}: Uses the given context and this name.
     * <p>
     * <strong>Performance:</strong> This loads {@link ContextFactory} via {@link ServiceLoader#load(java.lang.Class)} on
     * every call.  It is strongly encouraged to use subclass-specific implementations where performance is critical.
     * </p>
     */
    public <C> Attribute<C, T> context(C context) {
      @SuppressWarnings("unchecked")
      ServiceLoader<ContextFactory<C, T>> loader = (ServiceLoader)ServiceLoader.load(ContextFactory.class);
      Iterator<ContextFactory<C, T>> iter = loader.iterator();
      while (iter.hasNext()) {
        Attribute<C, T> attribute = iter.next().attribute(context, name);
        if (attribute != null) {
          return attribute;
        }
      }
      throw new IllegalArgumentException(
        "No factory registered for context of type \""
        + (context == null ? "null" : context.getClass().toGenericString()) + "\": " + context
      );
    }
    // </editor-fold>
  }

  /**
   * {@link Attribute}: Uses the given name, still needs scope or context.
   */
  public static <T> Name<T> attribute(String name) {
    return new Name<>(name);
  }
  // </editor-fold>
}
