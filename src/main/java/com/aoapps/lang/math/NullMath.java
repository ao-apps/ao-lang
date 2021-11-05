/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2015, 2016, 2017, 2021  AO Industries, Inc.
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
package com.aoapps.lang.math;

/**
 * Math routines that allow null.
 *
 * @author  AO Industries, Inc.
 */
public abstract class NullMath {

	/** Make no instances. */
	private NullMath() {throw new AssertionError();}

	/**
	 * Adds two integers together while allowing null.
	 *
	 * @return  the sum or null when both arguments are null
	 */
	public static Integer add(Integer i1, Integer i2) {
		if(i1 == null) {
			if(i2 == null) {
				return null;
			} else {
				return i2;
			}
		} else {
			if(i2 == null) {
				return i1;
			} else {
				return i1 + i2;
			}
		}
	}

	/**
	 * Subtractions one integers from another while allowing null.
	 *
	 * @return  the difference or null when both arguments are null
	 */
	public static Integer sub(Integer i1, Integer i2) {
		if(i1 == null) {
			if(i2 == null) {
				return null;
			} else {
				return -i2;
			}
		} else {
			if(i2 == null) {
				return i1;
			} else {
				return i1 - i2;
			}
		}
	}
}
