/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2023  AO Industries, Inc.
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

package com.aoapps.lang.security.acl;

import java.security.Principal;
import java.util.Enumeration;

/**
 * Compatibility with <code>java.security.acl.Group</code> removed since Java 14.
 *
 * @author  AO Industries, Inc.
 */
public interface Group extends Principal {

  /**
   * Adds the specified member to the group.
   *
   * @param user the principal to add to this group.
   *
   * @return true if the member was successfully added,
   *         false if the principal was already a member.
   */
  public boolean addMember(Principal user);

  /**
   * Removes the specified member from the group.
   *
   * @param user the principal to remove from this group.
   *
   * @return true if the principal was removed, or
   *         false if the principal was not a member.
   */
  public boolean removeMember(Principal user);

  /**
   * Returns true if the passed principal is a member of the group.
   * This method does a recursive search, so if a principal belongs to a
   * group which is a member of this group, true is returned.
   *
   * @param member the principal whose membership is to be checked.
   *
   * @return true if the principal is a member of this group,
   *         false otherwise.
   */
  public boolean isMember(Principal member);

  /**
   * Returns an enumeration of the members in the group.
   * The returned objects can be instances of either Principal
   * or Group (which is a subclass of Principal).
   *
   * @return an enumeration of the group members.
   */
  public Enumeration<? extends Principal> members();
}
