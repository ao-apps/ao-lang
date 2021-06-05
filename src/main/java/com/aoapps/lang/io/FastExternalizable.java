/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2011, 2016, 2017, 2021  AO Industries, Inc.
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
package com.aoapps.lang.io;

import java.io.Externalizable;

/**
 * A FastExternalizable object does not have any loops in its object graph,
 * or is able to restore the loops itself upon deserialization.
 *
 * FastExternalizable-aware containers may use this assumption to perform more
 * efficient serialization.
 *
 * @author  AO Industries, Inc.
 */
public interface FastExternalizable extends Externalizable {

	/**
	 * Gets the serialVersionUID for this object.
	 * Since FastExternalizable-aware containers are not special JVM constructs, they
	 * do not have access to the private and protected fields.
	 * It is assumed that two separate instances of the same class will always
	 * return the same serialVersionUID.
	 */
	long getSerialVersionUID();
}
