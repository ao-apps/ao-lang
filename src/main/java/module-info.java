/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2021  AO Industries, Inc.
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
module com.aoapps.lang {
	exports com.aoapps.lang;
	exports com.aoapps.lang.concurrent;
	exports com.aoapps.lang.dto;
	exports com.aoapps.lang.exception;
	exports com.aoapps.lang.function;
	exports com.aoapps.lang.i18n;
	exports com.aoapps.lang.io;
	exports com.aoapps.lang.io.function;
	exports com.aoapps.lang.math;
	exports com.aoapps.lang.nio.charset;
	exports com.aoapps.lang.reflect;
	exports com.aoapps.lang.sql;
	exports com.aoapps.lang.text;
	exports com.aoapps.lang.time;
	exports com.aoapps.lang.util;
	exports com.aoapps.lang.validation;
	exports com.aoapps.lang.xml;
	exports com.aoapps.lang.zip;
	uses com.aoapps.lang.CoercionOptimizerInitializer;
	uses com.aoapps.lang.ThrowableSurrogateFactoryInitializer;
	// Javadoc-only
	requires static org.apache.commons.codec; // <groupId>commons-codec</groupId><artifactId>commons-codec</artifactId>
	requires static org.apache.commons.io; // <groupId>commons-io</groupId><artifactId>commons-io</artifactId>
	requires static org.apache.commons.lang3; // <groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId>
	// Java SE
	requires java.compiler;
	requires java.desktop;
	requires java.instrument;
	requires java.logging;
	requires java.management;
	requires java.prefs;
	requires java.rmi;
	requires java.security.sasl;
	requires java.sql;
	requires java.sql.rowset;
	requires java.xml;
	requires java.xml.crypto;
}
