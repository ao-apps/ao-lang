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
package com.aoindustries.lang;

/**
 * Utilities for working with {@link Throwable},
 */
final public class Throwables {

	/**
	 * Make no instances.
	 */
	private Throwables() {}

	/**
	 * Adds a suppressed exception, unless already in the list of suppressed exceptions.
	 * <p>
	 * When {@code suppressed} is a {@link ThreadDeath} and {@code t1} is not itself a {@link ThreadDeath},
	 * {@code suppressed} will be returned instead, with {@code t1} added to it as suppressed.
	 * This is to maintain the precedence of {@link ThreadDeath} for fail-fast behavior.
	 * </p>
	 *
	 * @param  t1  The throwable to add to.  When {@code null}, {@code suppressed} is returned instead.
	 * @param  suppressed  The suppressed throwable, skipped when {@code null}
	 *
	 * @return  {@code t1} when not null, otherwise {@code suppressed}.
	 */
	public static Throwable addSuppressed(Throwable t1, Throwable suppressed) {
		if(suppressed != null) {
			if(t1 == null) {
				t1 = suppressed;
			} else {
				if(
					suppressed instanceof ThreadDeath
					&& !(t1 instanceof ThreadDeath)
				) {
					// Swap order to maintain fail-fast ThreadDeath
					Throwable t = t1;
					t1 = suppressed;
					suppressed = t;
				}
				boolean found = false;
				for(Throwable t : t1.getSuppressed()) {
					if(t == suppressed) {
						found = true;
						break;
					}
				}
				if(!found) {
					t1.addSuppressed(suppressed);
				}
			}
		}
		return t1;
	}
}
