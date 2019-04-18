/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2016, 2017, 2019  AO Industries, Inc.
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
package com.aoindustries.util.i18n;

import com.aoindustries.text.MessageFormatFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides a simplified interface for obtaining localized and formatted values
 * from a <code>ResourceBundle</code>.  It is designed to be compatible with the
 * use of JSTL classes and taglibs.
 *
 * @author  AO Industries, Inc.
 */
public class ApplicationResourcesAccessor implements Serializable {

	private static final long serialVersionUID = -8735217773587095120L;

	private static final ConcurrentMap<String,ApplicationResourcesAccessor> accessors = new ConcurrentHashMap<>();

	public static ApplicationResourcesAccessor getInstance(String baseName) {
		ApplicationResourcesAccessor existing = accessors.get(baseName);
		if(existing==null) {
			ApplicationResourcesAccessor newAccessor = new ApplicationResourcesAccessor(baseName);
			existing = accessors.putIfAbsent(baseName, newAccessor);
			if(existing==null) existing = newAccessor;
		}
		return existing;
	}

	/**
	 * Listeners may be registered to be notified as messages are looked-up.
	 * This is a hook used for in-context translation tools.
	 */
	public static interface Listener {
		/**
		 * Called during {@link #getMessage(java.util.Locale, java.lang.String, java.lang.Object...)} when a lookup is performed and the key found.
		 *
		 * @param  resource  The value received from the {@link ResourceBundle}.
		 *                   {@code null} when the lookup failed.
		 * @param  result    The result, possibly including any message substitutions.
		 *                   This will always be a unique String instance per call, allowing
		 *                   listeners to match individual strings to their lookup by identity.
		 */
		void onGetMessage(ApplicationResourcesAccessor accessor, Locale locale, String key, Object[] args, String resource, String result);
	}

	/**
	 * The registered listeners.  This list will always be recreated in-full
	 * when updated, so synchronization not needed while iterating the list
	 * of listeners.
	 */
	private static class ListenersLock {}
	private static final ListenersLock listenersLock = new ListenersLock();
	private static List<Listener> listeners;

	/**
	 * Adds a listener.  Does not check for duplicates.  If a listener is added
	 * more than once, it will simply by called multiple times.
	 */
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

	final private String baseName;

	private ApplicationResourcesAccessor(String baseName) {
		this.baseName = baseName;
	}

	private Object readResolve() {
		return getInstance(baseName);
	}

	/**
	 * Gets the baseName being accessed by this accessor.
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * Gets the bundle for the provided locale.
	 */
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle(baseName, locale);
	}

	/**
	 * <p>
	 * Gets the message.
	 * If missing, will generate a struts-like value including the locale and key.
	 * </p>
	 * <p>
	 * Gets the message in the current thread's locale.
	 * </p>
	 *
	 * @see ThreadLocale
	 */
	public String getMessage(String key) {
		return getMessage(ThreadLocale.get(), key);
	}

	/**
	 * <p>
	 * Gets the message.
	 * If missing, will generate a struts-like value including the locale and key.
	 * </p>
	 */
	public String getMessage(Locale locale, String key) {
		String string = null;
		try {
			string = getResourceBundle(locale).getString(key);
		} catch(MissingResourceException err) {
			// string remains null
		}
		if(string==null) return "???"+locale.toString()+"."+key+"???";
		return string;
	}

	/**
	 * <p>
	 * Gets the message.
	 * If missing, will generate a struts-like value including the locale and key.
	 * </p>
	 * <p>
	 * Substitutes arguments in the text where it finds {0}, {1}, {2}, ...
	 * </p>
	 * <p>
	 * Gets the message in the current thread's locale.
	 * </p>
	 *
	 * @see ThreadLocale
	 * @see  #getMessage(String,Locale,String)
	 */
	public String getMessage(String key, Object... args) {
		return getMessage(ThreadLocale.get(), key, args);
	}

	/**
	 * <p>
	 * Gets the message.
	 * If missing, will generate a struts-like value including the locale and key.
	 * </p>
	 * <p>
	 * Substitutes arguments in the text where it finds {0}, {1}, {2}, ...
	 * </p>
	 *
	 * @see  #getMessage(String,Locale,String)
	 */
	public String getMessage(Locale locale, String key, Object... args) {
		String resource = null;
		try {
			resource = getResourceBundle(locale).getString(key);
		} catch(MissingResourceException err) {
			// string remains null
		}
		// It is rare that the identity of a String object matters, but for correct resource bundle lookup hooks,
		// newString must always be a unique String object instance per lookup.
		String result;
		if(resource == null) {
			return "???" + locale.toString() + '.' + key + "???";
		} else if(args.length == 0) {
			// NOTE: Make a new string instance always, since string identity is used to know how it was looked-up
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
			for(Listener l : myListeners) l.onGetMessage(this, locale, key, args, resource, result);
		}
		// Return result
		return result;
	}
}
