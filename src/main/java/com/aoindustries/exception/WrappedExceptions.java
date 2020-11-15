/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2013, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.exception;

import com.aoindustries.lang.Throwables;
import com.aoindustries.util.ErrorPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps multiple exceptions into one.  {@link ErrorPrinter} will unwrap each exception
 * into a single exception report.  Any exception provided more than once will
 * only be stored once.  The first exception is also passed to {@link Throwable#initCause(java.lang.Throwable)}.
 * Any {@code null} exception is ignored.
 *
 * @author  AO Industries, Inc.
 */
public class WrappedExceptions extends RuntimeException {

	private static final long serialVersionUID = -3938902089134728394L;

	/**
	 * Gets an unmodifiable, unique set of exceptions.
	 */
	private static List<Throwable> getUniqueCauses(Throwable ... causes) {
		int len = causes.length;
		List<Throwable> uniqueCauses = new ArrayList<>(len);
		for(Throwable cause : causes) {
			if(cause!=null && !uniqueCauses.contains(cause)) uniqueCauses.add(cause);
		}
		// return AoCollections.optimalUnmodifiableList(uniqueCauses);
		return Collections.unmodifiableList(uniqueCauses);
	}

	private final List<Throwable> causes;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public WrappedExceptions(Throwable ... causes) {
		super();
		this.causes = getUniqueCauses(causes);
		if(!this.causes.isEmpty()) initCause(this.causes.get(0));
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public WrappedExceptions(String message, Throwable ... causes) {
		super(message);
		this.causes = getUniqueCauses(causes);
		if(!this.causes.isEmpty()) initCause(this.causes.get(0));
	}

	/**
	 * Gets the unmodifiable list of causes.  The first cause is also
	 * the value returned from getCause();
	 *
	 * @return  No defensive copy
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public List<Throwable> getCauses() {
		return causes;
	}

	static {
		Throwables.registerSurrogateFactory(WrappedExceptions.class, (template, cause) ->
			new WrappedExceptions(template.getMessage(), cause)
		);
	}
}
