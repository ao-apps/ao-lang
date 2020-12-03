/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2020  AO Industries, Inc.
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

import com.aoindustries.i18n.Resources;
import com.aoindustries.lang.EmptyArrays;
import com.aoindustries.lang.Throwables;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Extends <code>SQLException</code> to provide exceptions with user locale error messages.
 *
 * @author  AO Industries, Inc.
 */
public class LocalizedSQLException extends SQLException {

	private static final long serialVersionUID = 2L;

	/**
	 * @deprecated  Please use {@link #resources} directly.
	 */
	@Deprecated
	protected final com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor;
	protected final Resources resources;
	protected final String key;
	protected final Serializable[] args;

	public LocalizedSQLException(String SQLState, int vendorCode, Resources resources, String key) {
		super(resources.getMessage(key), SQLState, vendorCode);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, int, com.aoindustries.i18n.Resources, java.lang.String)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String SQLState, int vendorCode, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		this(SQLState, vendorCode, (Resources)accessor, key);
	}

	public LocalizedSQLException(String SQLState, int vendorCode, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), SQLState, vendorCode);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, int, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String SQLState, int vendorCode, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this(SQLState, vendorCode, (Resources)accessor, key, args);
	}

	public LocalizedSQLException(String SQLState, Resources resources, String key) {
		super(resources.getMessage(key), SQLState);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, com.aoindustries.i18n.Resources, java.lang.String)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String SQLState, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		this(SQLState, (Resources)accessor, key);
	}

	public LocalizedSQLException(String SQLState, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), SQLState);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String SQLState, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this(SQLState, (Resources)accessor, key, args);
	}

	/**
	 * @deprecated  Please provide SQLSTATE to {@link #LocalizedSQLException(java.lang.String, com.aoindustries.i18n.Resources, java.lang.String)}
	 */
	@Deprecated
	public LocalizedSQLException(com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		super(accessor.getMessage(key));
		this.accessor = accessor;
		this.resources = (Resources)accessor;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please provide SQLSTATE to {@link #LocalizedSQLException(java.lang.String, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String)}
	 */
	@Deprecated
	public LocalizedSQLException(Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		super(accessor.getMessage(key), cause);
		this.accessor = accessor;
		this.resources = (Resources)accessor;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please provide SQLSTATE to {@link #LocalizedSQLException(java.lang.String, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)}
	 */
	@Deprecated
	public LocalizedSQLException(com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		super(accessor.getMessage(key, (Object[])args));
		this.accessor = accessor;
		this.resources = (Resources)accessor;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please provide SQLSTATE to {@link #LocalizedSQLException(java.lang.String, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)}
	 */
	@Deprecated
	public LocalizedSQLException(Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		super(accessor.getMessage(key, (Object[])args), cause);
		this.accessor = accessor;
		this.resources = (Resources)accessor;
		this.key = key;
		this.args = args;
	}

	public LocalizedSQLException(String sqlState, Throwable cause, Resources resources, String key) {
		super(resources.getMessage(key), sqlState, cause);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String sqlState, Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		this(sqlState, cause, (Resources)accessor, key);
	}

	public LocalizedSQLException(String sqlState, Throwable cause, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), sqlState, cause);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String sqlState, Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this(sqlState, cause, (Resources)accessor, key, args);
	}

	public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, Resources resources, String key) {
		super(resources.getMessage(key), sqlState, vendorCode, cause);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = EmptyArrays.EMPTY_SERIALIZABLE_ARRAY;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, int, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key) {
		this(sqlState, vendorCode, cause, (Resources)accessor, key);
	}

	public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, Resources resources, String key, Serializable... args) {
		super(resources.getMessage(key, (Object[])args), sqlState, vendorCode, cause);
		this.accessor = resources;
		this.resources = resources;
		this.key = key;
		this.args = args;
	}

	/**
	 * @deprecated  Please use {@link #LocalizedSQLException(java.lang.String, int, java.lang.Throwable, com.aoindustries.i18n.Resources, java.lang.String, java.io.Serializable...)} directly.
	 */
	@Deprecated
	public LocalizedSQLException(String sqlState, int vendorCode, Throwable cause, com.aoindustries.util.i18n.ApplicationResourcesAccessor accessor, String key, Serializable... args) {
		this(sqlState, vendorCode, cause, (Resources)accessor, key, args);
	}

	@Override
	public String getLocalizedMessage() {
		return resources.getMessage(key, (Object[])args);
	}

	static {
		Throwables.registerSurrogateFactory(LocalizedSQLException.class, (template, cause) ->
			new LocalizedSQLException(
				template.getSQLState(),
				template.getErrorCode(),
				cause,
				template.resources,
				template.key,
				template.args
			)
		);
	}
}
