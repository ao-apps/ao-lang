/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2018, 2020  AO Industries, Inc.
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
package com.aoindustries.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Wraps a <code>SQLException</code> to include its source SQL statement.
 *
 * @author  AO Industries, Inc.
 */
public class WrappedSQLException extends SQLException {

	private static final long serialVersionUID = 1884080138318429559L;

	final private String sqlString;

	// TODO: Deprecate in favor of a static wrapper method that will not wrap exceptions that are already WrappedSQLException
	public WrappedSQLException(
		SQLException initCause,
		PreparedStatement pstmt
	) {
		this(initCause, pstmt.toString());
	}

	public WrappedSQLException(
		SQLException initCause,
		String sqlString
	) {
		super(
			initCause.getMessage() + "\nSQL:\n" + sqlString,
			initCause.getSQLState(),
			initCause.getErrorCode(),
			initCause
		);
		this.sqlString = sqlString;
	}

	public String getSqlString() {
		return sqlString;
	}
}
