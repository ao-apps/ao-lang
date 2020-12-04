/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.i18n;

import com.aoindustries.text.MessageFormatFactory;
import com.aoindustries.util.i18n.ThreadLocale;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides a simplified interface for obtaining localized and formatted values
 * from a {@link ResourceBundle}.  This is designed to be compatible with the
 * use of JSTL classes and taglibs.
 *
 * @author  AO Industries, Inc.
 */
@SuppressWarnings("deprecation")
public class Resources extends com.aoindustries.util.i18n.ApplicationResourcesAccessor {

	private static final long serialVersionUID = 1L;

	/**
	 * Note: If ao-collections ever a dependency, could use it's constant empty object array.
	 */
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private static final class Key {

		private final String baseName;
		private final String prefix;

		private Key(String baseName, String prefix) {
			this.baseName = Objects.requireNonNull(baseName);
			if(prefix != null && prefix.isEmpty()) throw new IllegalArgumentException();
			this.prefix = prefix;
		}

		@Override
		public int hashCode() {
			return baseName.hashCode() * 31 + Objects.hashCode(prefix);
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Key)) return false;
			Key other = (Key)obj;
			return
				baseName.equals(other.baseName)
				&& Objects.equals(prefix, other.prefix);
		}
	}

	private static final ConcurrentMap<Key,Resources> resources = new ConcurrentHashMap<>();

	/**
	 * Accesses the resources with the given base name and prefix.
	 *
	 * @see  #getResources(java.lang.Class)
	 * @see  #getResources(java.lang.Package)
	 * @see  #getResources(java.lang.Package, java.lang.String)
	 * @see  #getResources(java.lang.Package, java.lang.String, java.lang.String)
	 *
	 * @deprecated  Please use one of the class- or package-relative techniques in locating the resource bundle, as they
	 *              will correctly locate the resources after packages are renamed by code obfuscation.
	 */
	@Deprecated
	public static Resources getResources(String baseName, String prefix) {
		if(prefix != null && prefix.isEmpty()) prefix = null;
		Key key = new Key(baseName, prefix);
		Resources existing = resources.get(key);
		if(existing == null) {
			Resources newResources = new Resources(baseName, prefix);
			existing = resources.putIfAbsent(key, newResources);
			if(existing == null) existing = newResources;
		}
		return existing;
	}

	/**
	 * Accesses the resources with the given base name and no prefix.
	 *
	 * @see  #getResources(java.lang.Class)
	 * @see  #getResources(java.lang.Package)
	 * @see  #getResources(java.lang.Package, java.lang.String)
	 * @see  #getResources(java.lang.Package, java.lang.String, java.lang.String)
	 *
	 * @deprecated  Please use one of the class- or package-relative techniques in locating the resource bundle, as they
	 *              will correctly locate the resources after packages are renamed by code obfuscation.
	 */
	@Deprecated
	public static Resources getResources(String baseName) {
		return getResources(baseName, null);
	}

	/**
	 * Accesses the resources in the given package with the given name and prefix.
	 * The base name is derived as {@code pack.getName() + '.' + name}.
	 * <p>
	 * Note: Being accessed relative to the package, the resources can still be correctly located after
	 * packages are renamed by code obfuscation.
	 * </p>
	 *
	 * @param  name  The name of the resource within the package, when {@code null} defaults to
	 *               {@code "ApplicationResources"}.
	 */
	public static Resources getResources(Package pack, String name, String prefix) {
		return getResources(
			(name == null) ? (pack.getName() + ".ApplicationResources") : (pack.getName() + '.' + name),
			prefix
		);
	}

	/**
	 * Accesses the resources in the given package with the given name and no prefix.
	 * The base name is derived as {@code pack.getName() + '.' + name}.
	 * <p>
	 * Note: Being accessed relative to the package, the resources can still be correctly located after
	 * packages are renamed by code obfuscation.
	 * </p>
	 *
	 * @param  name  The name of the resource within the package, when {@code null} defaults to
	 *               {@code "ApplicationResources"}.
	 *
	 * @see  #getResources(java.lang.Package, java.lang.String, java.lang.String)
	 */
	public static Resources getResources(Package pack, String name) {
		return getResources(pack, name, null);
	}

	/**
	 * Accesses the resources in the given package named {@code "ApplicationResources"}.
	 * The base name is derived as {@code pack.getName() + ".ApplicationResources"}.
	 * <p>
	 * Note: Being accessed relative to the package, the resources can still be correctly located after
	 * packages are renamed by code obfuscation.
	 * </p>
	 *
	 * @see  #getResources(java.lang.Package, java.lang.String)
	 * @see  #getResources(java.lang.Package, java.lang.String, java.lang.String)
	 */
	public static Resources getResources(Package pack) {
		return getResources(pack, null, null);
	}

	/**
	 * Accesses the resources in the same package as the given class named {@code "ApplicationResources"} with the
	 * {@linkplain Class#getSimpleName() class name} as the prefix.
	 * <p>
	 * Note: Being accessed relative to the package, the resources may still be correctly located after
	 * packages are renamed by code obfuscation.  However, if classes are also renamed, the prefix will change and the
	 * build system must also alter the contents of the underlying <code>*.properties</code> files correspondingly.
	 * </p>
	 * <p>
	 * When rewriting the contents of the underlying properties files is not possible, it may be best either use
	 * hard-coded prefix (may leak original class name, thus thwarting obfuscation a bit) or use a per-class
	 * properties file (tedious, also requires build system coordination).
	 * </p>
	 *
	 * @param  clazz  The base name is derived as {@code clazz.getPackage().getName() + ".ApplicationResources"}.
	 *                The prefix is obtained from {@link Class#getSimpleName()}.
	 *                <p>
	 *                This class is used for determining the package and prefix only.  It will typically be the class
	 *                that is using the resource, not the class that implements {@link ResourceBundle}.
	 *                </p>
	 *
	 * @see  #getResources(java.lang.Package, java.lang.String, java.lang.String)
	 */
	public static Resources getResources(Class<?> clazz) {
		// Java 9: getPackageName()
		return getResources(clazz.getPackage(), null, clazz.getSimpleName());
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
		 * @param  resource  The value received from the {@link ResourceBundle}.
		 *                   {@code null} when the lookup failed.
		 * @param  args      The set of arguments, may be an empty array, never {@code null}.
		 * @param  result    The result, possibly including any message substitutions.
		 *                   This will always be a unique String instance per call, allowing
		 *                   listeners to match individual strings to their lookup by identity.
		 */
		void onGetMessage(Resources resources, Locale locale, String key, Object[] args, String resource, String result);
	}

	/**
	 * The registered listeners.  This list will always be recreated in-full
	 * when updated, so synchronization not needed while iterating the list
	 * of listeners.
	 */
	// TODO: Replace with CopyOnWriteArrayList to avoid locking?
	private static class ListenersLock {}
	private static final ListenersLock listenersLock = new ListenersLock();
	private static List<Listener> listeners;

	/**
	 * Adds a listener.  Does not check for duplicates.  If a listener is added
	 * more than once, it will simply by called multiple times.
	 */
	@SuppressWarnings("overloads") // TODO: Remove once the base class ApplicationResourcesAccessor is eliminated
	public static void addListener(Listener listener) {
		synchronized(listenersLock) {
			List<Listener> newListeners;
			if(listeners == null) {
				newListeners = Collections.singletonList(listener);
			} else {
				newListeners = new ArrayList<>(listeners.size() + 1);
				newListeners.addAll(listeners);
				newListeners.add(listener);
			}
			listeners = newListeners;
		}
	}

	/**
	 * Removes all occurrences of the provided listener.
	 */
	@SuppressWarnings("overloads") // TODO: Remove once the base class ApplicationResourcesAccessor is eliminated
	public static void removeListener(Listener listener) {
		synchronized(listenersLock) {
			if(listeners != null) {
				ArrayList<Listener> newListeners = new ArrayList<>(listeners.size()-1);
				for(Listener l : listeners) {
					if(l != listener) newListeners.add(l);
				}
				if(newListeners.isEmpty()) {
					listeners = null;
				} else if(newListeners.size() == 1) {
					listeners = Collections.singletonList(newListeners.get(0));
				} else {
					newListeners.trimToSize();
					listeners = newListeners;
				}
			}
		}
	}

	private final String baseName;
	private final String prefix;

	private Resources(String baseName, String prefix) {
		this.baseName = Objects.requireNonNull(baseName);
		if(prefix != null && prefix.isEmpty()) throw new IllegalArgumentException();
		this.prefix = prefix;
	}

	private Object readResolve() {
		return getResources(baseName, prefix);
	}

	/**
	 * Gets the baseName being accessed by these resources.
	 */
	@Override
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
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle(baseName, locale);
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
	 * <p>
	 * Gets the message with the given key in the provided locale,
	 * optionally {@link MessageFormat#format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) message-formatted}.
	 * If missing, will generate a Struts-like value including the locale and (prefix + key).
	 * </p>
	 * <p>
	 * Substitutes arguments in the text where it finds {0}, {1}, {2}, …
	 * Message formatting is not performed when {@code args} is {@code null} or empty.
	 * </p>
	 *
	 * @param  key  This will be combined with any {@link #getPrefix() prefix}
	 */
	@Override
	@SuppressWarnings("RedundantStringConstructorCall")
	public String getMessage(Locale locale, String key, Object... args) {
		if(args == null) args = EMPTY_OBJECT_ARRAY;
		if(prefix != null) key = prefix + key;
		String resource = null;
		try {
			resource = getResourceBundle(locale).getString(key);
		} catch(MissingResourceException err) {
			// resource remains null
		}
		if(resource == null) {
			return "???" + locale.toString() + '.' + key + "???";
		} else {
			// It is rare that the identity of a String object matters, but for correct resource bundle lookup hooks,
			// newString must always be a unique String object instance per lookup.
			String result;
			if(args.length == 0) {
				// NOTE: Make a new string instance always, since string identity is used to know how it was looked-up
				//
				// Should we check if translation is activated before making this new instance?
				//     Checking thread-local might not be much faster than making the string instance, since this String
				//     constructor only copies a few fields and not the underlying array.
				result = new String(resource);
			} else {
				// newString is a new string due to StringBuffer...toString
				result = MessageFormatFactory.getMessageFormat(resource, locale).format(args, new StringBuffer(resource.length()<<1), null).toString();
			}
			// Call any listeners
			List<Listener> myListeners;
			synchronized(listenersLock) {
				myListeners = listeners;
			}
			if(myListeners != null) {
				for(Listener l : myListeners) {
					l.onGetMessage(this, locale, key, args, resource, result);
				}
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
	@Override
	public String getMessage(Locale locale, String key) {
		return getMessage(locale, key, EMPTY_OBJECT_ARRAY);
	}

	/**
	 * <p>
	 * Gets the message with the given key in the {@linkplain ThreadLocale#get() current thread's locale},
	 * optionally {@link MessageFormat#format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition) message-formatted}.
	 * If missing, will generate a Struts-like value including the locale and (prefix + key).
	 * </p>
	 * <p>
	 * Substitutes arguments in the text where it finds {0}, {1}, {2}, …
	 * Message formatting is not performed when {@code args} is {@code null} or empty.
	 * </p>
	 *
	 * @param  key  This will be combined with any {@link #getPrefix() prefix}
	 *
	 * @see  ThreadLocale
	 * @see  #getMessage(java.util.Locale, java.lang.String, java.lang.Object...)
	 */
	@Override
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
	@Override
	public String getMessage(String key) {
		return getMessage(ThreadLocale.get(), key, EMPTY_OBJECT_ARRAY);
	}
}
