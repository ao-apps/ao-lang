/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2014, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.lang.reflect;

import com.aoapps.lang.EmptyArrays;
import com.aoapps.lang.Throwables;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Simplifies access to a reflection method.
 *
 * @author  AO Industries, Inc.
 */
public abstract class Methods {

	/** Make no instances. */
	private Methods() {throw new AssertionError();}

	/**
	 * Invokes the provided method on the given object.
	 * This is convenient, but not so fast.  Where repeated calls will be made to the method,
	 * us the full reflection API.
	 */
	public static <T> T invoke(Class<T> returnType, Object target, String methodName) throws ReflectionException {
		return invoke(returnType, target, methodName, EmptyArrays.EMPTY_CLASS_ARRAY, EmptyArrays.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Invokes the provided method on the given object.
	 * This is convenient, but not so fast.  Where repeated calls will be made to the method,
	 * us the full reflection API.
	 */
	public static <T> T invoke(Class<T> returnType, Object target, String methodName, Class<?> parameterType, Object parameterValue) throws ReflectionException {
		return invoke(returnType, target, methodName, new Class<?>[] {parameterType}, new Object[] {parameterValue});
	}

	/**
	 * Invokes the provided method on the given object.
	 * This is convenient, but not so fast.  Where repeated calls will be made to the method,
	 * us the full reflection API.
	 */
	// TODO: varargs?
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public static <T> T invoke(Class<T> returnType, Object target, String methodName, Class<?>[] parameterTypes, Object[] parameterValues) throws ReflectionException {
		try {
			try {
				Method method = target.getClass().getMethod(methodName, parameterTypes);
				Object result = method.invoke(target, parameterValues);
				return returnType.cast(result);
			} catch(InvocationTargetException e) {
				// Unwrap cause for more direct stack traces
				Throwable cause = e.getCause();
				throw (cause == null) ? e : cause;
			}
		} catch(Throwable t) {
			throw Throwables.wrap(t, ReflectionException.class, ReflectionException::new);
		}
	}
}
