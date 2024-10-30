/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

package com.aoapps.lang.i18n;

import com.aoapps.lang.function.SerializableBiFunction;
import com.aoapps.lang.text.MessageFormatFactory;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a simplified interface for obtaining localized and formatted values
 * from a {@link ResourceBundle}.  This is designed to be compatible with the
 * use of JSTL classes and taglibs.
 *
 * @author  AO Industries, Inc.
 */
public class Resources implements Serializable {

  private static final Logger logger = Logger.getLogger(Resources.class.getName());

  /**
   * Note: If ao-collections ever a dependency, could use its constant empty object array.
   */
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  /**
   * The default sub-package that should contain resource bundles.
   * Sub-packages are used so that resource bundles may be opened without exposing the entire package to reflection.
   */
  public static final String DEFAULT_SUBPACKAGE = "i18n";

  /**
   * The default bundle name.
   */
  public static final String DEFAULT_NAME = "ApplicationResources";

  /**
   * Accesses the resources with the given base name and prefix.
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   *
   * @deprecated  Please use one of the class- or package-relative techniques in locating the resource bundle, as they
   *              will correctly locate the resources after packages are renamed by code obfuscation.
   *              <ul>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Class)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String, java.lang.String)}</li>
   *              </ul>
   */
  @Deprecated
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, String baseName, String prefix) {
    return new Resources(bundleAccessor, baseName, prefix);
  }

  /**
   * Accesses the resources with the given base name and prefix.
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.String, java.lang.String)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(String baseName, String prefix) {
    return getResources((SerializableBiFunction<String, Locale, ResourceBundle>) null, baseName, prefix);
  }

  /**
   * Accesses the resources with the given base name and no prefix.
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   *
   * @deprecated  Please use one of the class- or package-relative techniques in locating the resource bundle, as they
   *              will correctly locate the resources after packages are renamed by code obfuscation.
   *              <ul>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Class)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String)}</li>
   *              <li>{@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String, java.lang.String)}</li>
   *              </ul>
   */
  @Deprecated
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, String baseName) {
    return getResources(bundleAccessor, baseName, null);
  }

  /**
   * Accesses the resources with the given base name and no prefix.
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.String)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(String baseName) {
    return getResources((SerializableBiFunction<String, Locale, ResourceBundle>) null, baseName, null);
  }

  /**
   * Accesses the resources in the given package (or sub-package) with the given name and prefix.
   * The base name is derived as {@code pack.getName() + '.' + name}.
   *
   * <p>By default, resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   *
   * @param  name  The name of the resource within the package, when {@code null} defaults to
   *               {@code DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   */
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, Package pack, String name, String prefix) {
    return getResources(
        bundleAccessor,
        (name == null) ? (pack.getName() + "." + DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME) : (pack.getName() + '.' + name),
        prefix
    );
  }

  /**
   * Accesses the resources in the given package (or sub-package) with the given name and prefix.
   * The base name is derived as {@code pack.getName() + '.' + name}.
   *
   * <p>By default, resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @param  name  The name of the resource within the package, when {@code null} defaults to
   *               {@code DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String, java.lang.String)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(Package pack, String name, String prefix) {
    return getResources(null, pack, name, prefix);
  }

  /**
   * Accesses the resources in the given package (or sub-package) with the given name and no prefix.
   * The base name is derived as {@code pack.getName() + '.' + name}.
   *
   * <p>By default, resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   *
   * @param  name  The name of the resource within the package, when {@code null} defaults to
   *               {@code DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   */
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, Package pack, String name) {
    return getResources(bundleAccessor, pack, name, null);
  }

  /**
   * Accesses the resources in the given package (or sub-package) with the given name and no prefix.
   * The base name is derived as {@code pack.getName() + '.' + name}.
   *
   * <p>By default, resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @param  name  The name of the resource within the package, when {@code null} defaults to
   *               {@code DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package, java.lang.String)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(Package pack, String name) {
    return getResources(null, pack, name, null);
  }

  /**
   * Accesses the resources in the {@link #DEFAULT_SUBPACKAGE} sub-package of the given package named {@link #DEFAULT_NAME}.
   * The base name is derived as {@code pack.getName() + "." + DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * <p>Resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   */
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, Package pack) {
    return getResources(bundleAccessor, pack, null, null);
  }

  /**
   * Accesses the resources in the {@link #DEFAULT_SUBPACKAGE} sub-package of the given package named {@link #DEFAULT_NAME}.
   * The base name is derived as {@code pack.getName() + "." + DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME} (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * <p>Resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources can still be correctly located after
   * packages are renamed by code obfuscation.</p>
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Package)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(Package pack) {
    return getResources(pack, null, null);
  }

  /**
   * Accesses the resources in the {@link #DEFAULT_SUBPACKAGE} sub-package of the given class named
   * {@link #DEFAULT_NAME} with {@code clazz.getSimpleName() + '.'} as the prefix.
   * The base name is derived as {@code clazz.getPackage().getName() + "." + DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME}
   * (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * <p>Resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources may still be correctly located after
   * packages are renamed by code obfuscation.  However, if classes are also renamed, the prefix will change and the
   * build system must also alter the contents of the underlying <code>*.properties</code> files correspondingly.</p>
   *
   * <p>When rewriting the contents of the underlying properties files is not possible, it may be best either use
   * hard-coded prefix (may leak original class name, thus thwarting obfuscation a bit) or use a per-class
   * properties file (tedious, also requires build system coordination).</p>
   *
   * @param  bundleAccessor  Bi-function lookup for bundle, which will typically be the static method reference
   *                         <code>ResourceBundle::getBundle</code>.
   *
   *                         <p>As of Java 9, bundle access is affected by module descriptors.  To access the bundle with
   *                         caller permissions, pass a small lambda that performs the bundle access.  This will
   *                         typically be the static method reference <code>ResourceBundle::getBundle</code>, but may
   *                         be of any arbitrary complexity.  The bundle accessor is invoked for every message lookup,
   *                         so the implementation should take care to perform well.</p>
   *
   *                         <p>When {@code null}, the bundle is looked-up via a direct call to
   *                         {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires
   *                         <code>opens …;</code> in <code>module-info.java</code>.</p>
   *
   * @param  clazz  This class is used for determining the package and prefix only.  It will typically be the
   *                class that is using the resource, not the class that implements {@link ResourceBundle}.
   */
  public static Resources getResources(SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor, Class<?> clazz) {
    return getResources(bundleAccessor, clazz.getPackage(), null, clazz.getSimpleName() + '.');
  }

  /**
   * Accesses the resources in the {@link #DEFAULT_SUBPACKAGE} sub-package of the given class named
   * {@link #DEFAULT_NAME} with {@code clazz.getSimpleName() + '.'} as the prefix.
   * The base name is derived as {@code clazz.getPackage().getName() + "." + DEFAULT_SUBPACKAGE + "." + DEFAULT_NAME}
   * (to be within {@link #DEFAULT_SUBPACKAGE} sub-package).
   *
   * <p>Resources are expected to be in a sub-package named {@link #DEFAULT_SUBPACKAGE}.  A sub-package is
   * used because the module system does not allow opening for reflection separately from opening for resources.  By
   * using a separate sub-package, the resource bundles may be opened up without exposing the entire package to
   * reflection.</p>
   *
   * <p>Note: Being accessed relative to the package, the resources may still be correctly located after
   * packages are renamed by code obfuscation.  However, if classes are also renamed, the prefix will change and the
   * build system must also alter the contents of the underlying <code>*.properties</code> files correspondingly.</p>
   *
   * <p>When rewriting the contents of the underlying properties files is not possible, it may be best either use
   * hard-coded prefix (may leak original class name, thus thwarting obfuscation a bit) or use a per-class
   * properties file (tedious, also requires build system coordination).</p>
   *
   * @param  clazz  This class is used for determining the package and prefix only.  It will typically be the
   *                class that is using the resource, not the class that implements {@link ResourceBundle}.
   *
   * @deprecated  Please use {@link #getResources(com.aoapps.lang.function.SerializableBiFunction, java.lang.Class)} instead.
   *
   *              <p>As of Java 9, bundle access is affected by module descriptors.  The bundle is looked-up via a direct call to
   *              {@link ResourceBundle#getBundle(java.lang.String, java.util.Locale)}, which requires <code>opens …;</code> in
   *              <code>module-info.java</code>.</p>
   */
  @Deprecated
  public static Resources getResources(Class<?> clazz) {
    return getResources(null, clazz.getPackage(), null, clazz.getSimpleName() + '.');
  }

  /**
   * Listeners may be registered to be notified as messages are looked-up.
   * This is a hook used for in-context translation tools.
   */
  @FunctionalInterface
  public static interface Listener {
    /**
     * Called during {@link #getMessage(java.util.Locale, java.lang.String, java.lang.Object...)} when a lookup is performed and the key found.
     *
     * @param  resource  The value received from the {@link ResourceBundle} or {@code null} when the lookup failed.
     * @param  args      The set of arguments, may be an empty array, never {@code null}.
     * @param  result    The result, possibly including any message substitutions.
     *                   This will always be a unique String instance per call, allowing
     *                   listeners to match individual strings to their lookup by identity.
     */
    void onGetMessage(Resources resources, Locale locale, String key, Object[] args, String resource, String result);
  }

  /**
   * The registered listeners.
   */
  private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Adds a listener.  Does not check for duplicates.  If a listener is added
   * more than once, it will simply by called multiple times.
   */
  public static void addListener(Listener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  /**
   * Removes all occurrences of the provided listener.
   */
  public static void removeListener(Listener listener) {
    Iterator<Listener> iter = listeners.iterator();
    while (iter.hasNext()) {
      if (iter.next() == listener) {
        iter.remove();
      }
    }
  }

  private static final long serialVersionUID = 2L;

  private final SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor;
  private final String baseName;
  private final String prefix;

  private Resources(
      SerializableBiFunction<String, Locale, ResourceBundle> bundleAccessor,
      String baseName,
      String prefix
  ) {
    this.bundleAccessor = bundleAccessor;
    this.baseName = Objects.requireNonNull(baseName);
    if (prefix != null && prefix.isEmpty()) {
      prefix = null;
    }
    this.prefix = prefix;
  }

  /**
   * Gets the baseName being accessed by these resources.
   */
  public String getBaseName() {
    return baseName;
  }

  /**
   * Gets the prefix applied to all keys directly used on these resources or an empty string when there is no prefix.
   */
  public String getPrefix() {
    return (prefix == null) ? "" : prefix;
  }

  /**
   * Gets the bundle for the provided locale.
   * Direct use of this bundle will not have any {@linkplain #getPrefix() prefix} applied.
   */
  public ResourceBundle getResourceBundle(Locale locale) {
    return (bundleAccessor != null)
        ? bundleAccessor.apply(baseName, locale)
        : ResourceBundle.getBundle(baseName, locale);
  }

  /**
   * Gets the bundle for the {@linkplain ThreadLocale#get() current thread's locale}.
   * Direct use of this bundle will not have any {@linkplain #getPrefix() prefix} applied.
   *
   * @see  ThreadLocale
   * @see  #getResourceBundle(java.util.Locale)
   */
  public ResourceBundle getResourceBundle() {
    return getResourceBundle(ThreadLocale.get());
  }

  /**
   * Gets the message with the given key in the provided locale,
   * optionally {@link MessageFormat#format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) message-formatted}.
   * If missing, will generate a Struts-like value including the locale and (prefix + key).
   *
   * <p>Substitutes arguments in the text where it finds {0}, {1}, {2}, …
   * Message formatting is not performed when {@code args} is {@code null} or empty.</p>
   *
   * @param  key  This will be combined with any {@link #getPrefix() prefix}
   */
  @SuppressWarnings("RedundantStringConstructorCall")
  public String getMessage(Locale locale, String key, Object... args) {
    if (args == null) {
      args = EMPTY_OBJECT_ARRAY;
    }
    if (prefix != null) {
      key = prefix + key;
    }
    String resource = null;
    try {
      resource = getResourceBundle(locale).getString(key);
      if (resource == null && logger.isLoggable(Level.FINE)) {
        logger.fine(
            "Bundle lookup failed: baseName = \"" + baseName + "\", locale = \"" + locale + "\", key = \"" + key + '"'
        );
      }
    } catch (MissingResourceException err) {
      if (logger.isLoggable(Level.FINER)) {
        logger.log(
            Level.FINER,
            "Bundle lookup failed: baseName = \"" + baseName + "\", locale = \"" + locale + "\", key = \"" + key + '"',
            err
        );
      } else if (logger.isLoggable(Level.FINE)) {
        logger.fine(
            "Bundle lookup failed: baseName = \"" + baseName + "\", locale = \"" + locale + "\", key = \"" + key + "\" (enable FINER logging for stack trace)"
        );
      }
      // resource remains null
    }
    if (resource == null) {
      return "???" + locale.toString() + '.' + key + "???";
    } else {
      // It is rare that the identity of a String object matters, but for correct resource bundle lookup hooks,
      // newString must always be a unique String object instance per lookup.
      String result;
      if (args.length == 0) {
        // NOTE: Make a new string instance always, since string identity is used to know how it was looked-up
        //
        // Should we check if translation is activated before making this new instance?
        //     Checking thread-local might not be much faster than making the string instance, since this String
        //     constructor only copies a few fields and not the underlying array.
        result = new String(resource);
      } else {
        // newString is a new string due to StringBuffer...toString
        result = MessageFormatFactory.getMessageFormat(resource, locale).format(args, new StringBuffer(resource.length() << 1), null).toString();
      }
      // Call any listeners
      for (Listener l : listeners) {
        l.onGetMessage(this, locale, key, args, resource, result);
      }
      // Return result
      return result;
    }
  }

  /**
   * Gets the message with the given key in the provided locale.
   * If missing, will generate a Struts-like value including the locale and (prefix + key).
   *
   * @param  key  This will be combined with any {@link #getPrefix() prefix}
   */
  public String getMessage(Locale locale, String key) {
    return getMessage(locale, key, EMPTY_OBJECT_ARRAY);
  }

  /**
   * Gets the message with the given key in the {@linkplain ThreadLocale#get() current thread's locale},
   * optionally {@link MessageFormat#format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) message-formatted}.
   * If missing, will generate a Struts-like value including the locale and (prefix + key).
   *
   * <p>Substitutes arguments in the text where it finds {0}, {1}, {2}, …
   * Message formatting is not performed when {@code args} is {@code null} or empty.</p>
   *
   * @param  key  This will be combined with any {@link #getPrefix() prefix}
   *
   * @see  ThreadLocale
   * @see  #getMessage(java.util.Locale, java.lang.String, java.lang.Object...)
   */
  public String getMessage(String key, Object... args) {
    return getMessage(ThreadLocale.get(), key, args);
  }

  /**
   * Gets the message with the given key in the {@linkplain ThreadLocale#get() current thread's locale}.
   * If missing, will generate a Struts-like value including the locale and (prefix + key).
   *
   * @param  key  This will be combined with any {@link #getPrefix() prefix}
   *
   * @see  ThreadLocale
   * @see  #getMessage(java.util.Locale, java.lang.String)
   */
  public String getMessage(String key) {
    return getMessage(ThreadLocale.get(), key, EMPTY_OBJECT_ARRAY);
  }
}
