/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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
package com.aoapps.lang;

import java.io.IOError;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.LoginException;

/**
 * POSIX-compatible process exit values.
 *
 * From /usr/include/sysexits.h:
 *
 * <pre>
 *  SYSEXITS.H -- Exit status codes for system programs.
 *
 *	This include file attempts to categorize possible error
 *	exit statuses for system programs, notably delivermail
 *	and the Berkeley network.
 *
 *	Error numbers begin at EX__BASE to reduce the possibility of
 *	clashing with other exit statuses that random programs may
 *	already return.  The meaning of the codes is approximately
 *	as follows:
 *
 *	EX_USAGE -- The command was used incorrectly, e.g., with
 *		the wrong number of arguments, a bad flag, a bad
 *		syntax in a parameter, or whatever.
 *	EX_DATAERR -- The input data was incorrect in some way.
 *		This should only be used for user's data &amp; not
 *		system files.
 *	EX_NOINPUT -- An input file (not a system file) did not
 *		exist or was not readable.  This could also include
 *		errors like "No message" to a mailer (if it cared
 *		to catch it).
 *	EX_NOUSER -- The user specified did not exist.  This might
 *		be used for mail addresses or remote logins.
 *	EX_NOHOST -- The host specified did not exist.  This is used
 *		in mail addresses or network requests.
 *	EX_UNAVAILABLE -- A service is unavailable.  This can occur
 *		if a support program or file does not exist.  This
 *		can also be used as a catchall message when something
 *		you wanted to do doesn't work, but you don't know
 *		why.
 *	EX_SOFTWARE -- An internal software error has been detected.
 *		This should be limited to non-operating system related
 *		errors as possible.
 *	EX_OSERR -- An operating system error has been detected.
 *		This is intended to be used for such things as "cannot
 *		fork", "cannot create pipe", or the like.  It includes
 *		things like getuid returning a user that does not
 *		exist in the passwd file.
 *	EX_OSFILE -- Some system file (e.g., /etc/passwd, /etc/utmp,
 *		etc.) does not exist, cannot be opened, or has some
 *		sort of error (e.g., syntax error).
 *	EX_CANTCREAT -- A (user specified) output file cannot be
 *		created.
 *	EX_IOERR -- An error occurred while doing I/O on some file.
 *	EX_TEMPFAIL -- temporary failure, indicating something that
 *		is not really an error.  In sendmail, this means
 *		that a mailer (e.g.) could not create a connection,
 *		and the request should be reattempted later.
 *	EX_PROTOCOL -- the remote system returned something that
 *		was "not possible" during a protocol exchange.
 *	EX_NOPERM -- You did not have sufficient permission to
 *		perform the operation.  This is not intended for
 *		file system problems, which should use NOINPUT or
 *		CANTCREAT, but rather for higher level permissions.
 * </pre>
 */
public final class SysExits {

	/** Make no instances. */
	private SysExits() {throw new AssertionError();}

	/** successful termination */
	public static final int EX_OK = 0;

	/** base value for error messages */
	public static final int EX__BASE = 64;

	/** command line usage error */
	public static final int EX_USAGE = 64;

	/** data format error */
	public static final int EX_DATAERR = 65;

	/** cannot open input */
	public static final int EX_NOINPUT = 66;

	/** addressee unknown */
	public static final int EX_NOUSER = 67;

	/** host name unknown */
	public static final int EX_NOHOST = 68;

	/** service unavailable */
	public static final int EX_UNAVAILABLE = 69;

	/** internal software error */
	public static final int EX_SOFTWARE = 70;

	/** system error (e.g., can't fork) */
	public static final int EX_OSERR = 71;

	/** critical OS file missing */
	public static final int EX_OSFILE = 72;

	/** can't create (user) output file */
	public static final int EX_CANTCREAT = 73;

	/** input/output error */
	public static final int EX_IOERR = 74;

	/** temp failure; user is invited to retry */
	public static final int EX_TEMPFAIL = 75;

	/** remote error in protocol */
	public static final int EX_PROTOCOL = 76;

	/** permission denied */
	public static final int EX_NOPERM = 77;

	/** configuration error */
	public static final int EX_CONFIG = 78;

	/* maximum listed value */
	public static final int EX__MAX = 78;

	/**
	 * Gets a sysexit value for common exception types.
	 *
	 * @return  When {@code t} is null, returns {@link #EX_OK}, otherwise returns a non-zero sys exit best matching the
	 *          given throwable.
	 */
	// TODO: Add more as-needed.  This is just off the top of my head.
	public static int getSysExit(Throwable t) {
		if(t == null) {
			return EX_OK;
		}
		if(
			t instanceof UnknownHostException
			|| t instanceof java.rmi.UnknownHostException
		) {
			return EX_NOHOST;
		}
		if(
			t instanceof IOError
			|| t instanceof IOException
			|| t instanceof UncheckedIOException
		) {
			return EX_IOERR;
		}
		if(t instanceof SQLException) {
			return EX_DATAERR;
		}
		if(t instanceof AccountNotFoundException) {
			return EX_NOUSER;
		}
		if(t instanceof RemoteException) {
			return EX_PROTOCOL;
		}
		if(
			t instanceof LoginException
			|| t instanceof SecurityException
		) {
			return EX_NOPERM;
		}
		return EX_SOFTWARE;
	}
}
