/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.io;

/**
 * Some content-type constants for use by various projects.
 */
// Related to LocaleFilter.java
// Related to NoSessionFilter.java
// Related to SessionResponseWrapper.java
// Related to LastModifiedServlet.java
// Related to ao-mime-mappings/…/web-fragment.xml
// Is ContentType.java
// Related to MimeType.java
public final class ContentType {

	/** Make no instances. */
	private ContentType() {throw new AssertionError();}

	/**
	 * A CSS stylesheet.
	 */
	public static final String CSS = "text/css";

	/**
	 * A GIF image.
	 */
	public static final String GIF = "image/gif";

	/**
	 * GZIP compressed data.
	 */
	public static final String GZIP = "application/gzip";

	/**
	 * An ECMA script.
	 */
	public static final String ECMASCRIPT = "application/ecmascript";

	/**
	 * An ECMA script (old).
	 *
	 * @deprecated  Please use {@link #ECMASCRIPT}
	 */
	@Deprecated
	public static final String ECMASCRIPT_OLD = "text/ecmascript";

	/**
	 * Form data that may include <code>&lt;input type="file"&gt;</code>.
	 */
	public static final String FORM_DATA = "multipart/form-data";

	/**
	 * A JavaScript.
	 */
	public static final String JAVASCRIPT = "application/javascript";

	/**
	 * A JavaScript (old).
	 *
	 * @deprecated  Please use {@link #JAVASCRIPT}
	 */
	@Deprecated
	public static final String JAVASCRIPT_OLD = "text/javascript";

	/**
	 * A JPEG image.
	 */
	public static final String JPEG = "image/jpeg";

	/**
	 * A JSON structure.
	 */
	public static final String JSON = "application/json";

	/**
	 * A JSON linked data.
	 */
	public static final String LD_JSON = "application/ld+json";

	/**
	 * An HTML document.
	 */
	public static final String HTML = "text/html";

	/**
	 * The MySQL <code>mysql</code> command line.
	 */
	public static final String MYSQL = "text/x-mysql";

	/**
	 * A PNG image.
	 */
	public static final String PNG = "image/png";

	/**
	 * A WebP image.
	 */
	public static final String WEBP = "image/webp";

	/**
	 * The PostgreSQL <code>psql</code> command line.
	 */
	public static final String PSQL = "text/x-psql";

	/**
	 * A Bourne shell script.
	 */
	public static final String SH = "text/x-sh";

	/**
	 * A plaintext document.
	 */
	public static final String TEXT = "text/plain";

	/**
	 * Pseudo content type for a URL.
	 */
	public static final String URL = "text/url";

	/**
	 * URL-encoded form data.
	 */
	public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	/**
	 * An XHTML document.
	 */
	public static final String XHTML = "application/xhtml+xml";

	/**
	 * Pseudo content type for an XHTML attribute.
	 */
	public static final String XHTML_ATTRIBUTE = "application/xhtml+xml+attribute";

	/**
	 * An XML document.
	 */
	public static final String XML = "application/xml";

	/**
	 * An XML document (old).
	 *
	 * @deprecated  Please use {@link #XML}
	 */
	@Deprecated
	public static final String XML_OLD = "text/xml";

	/**
	 * See <a href="https://stackoverflow.com/a/40893366">css - MIME type as text/plain for Sass stylesheet in chrome - Stack Overflow</a>
	 */
	public static final String SASS = "text/x-sass";

	/**
	 * See <a href="https://stackoverflow.com/a/40893366">css - MIME type as text/plain for Sass stylesheet in chrome - Stack Overflow</a>
	 */
	public static final String SCSS = "text/x-scss";
}
