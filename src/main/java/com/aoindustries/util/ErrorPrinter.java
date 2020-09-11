/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.util;

import com.aoindustries.exception.WrappedException;
import com.aoindustries.exception.WrappedExceptions;
import com.aoindustries.sql.WrappedSQLException;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.Permission;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Prints errors with more detail than a standard printStackTrace() call.  Is also able to
 * capture the error into a <code>String</code>.
 * <p>
 * TODO: Avoid repetitive sequences of stack traces to reduce total output length.
 * </p>
 * <p>
 * TODO: Make an extensible way to register additional error printer features, and
 * automatically load them via {@link ServiceLoader}.  Maybe spin this off to a
 * microproject if we go this far.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
// TODO: Move to com.aoindustries.lang.Throwables?
// TODO: Support SQLClientInfoException
// TODO: Support MessagingException.getNextException?
public class ErrorPrinter {

	private static final String EOL = System.lineSeparator();

	private ErrorPrinter() {}

	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public static void printStackTraces(Throwable T) {
		printStackTraces(T, System.err, (Object[])null);
	}

	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public static void printStackTraces(Throwable T, Object... extraInfo) {
		printStackTraces(T, System.err, extraInfo);
	}

	public static void printStackTraces(Throwable T, Appendable out) {
		printStackTraces(T, out, (Object[])null);
	}

	private static void appendln(Appendable out) {
		try {
			out.append(EOL);
		} catch(IOException err) {
			// Ignored
		}
	}

	private static void append(String S, Appendable out) {
		try {
			out.append(S);
		} catch(IOException err) {
			// Ignored
		}
	}

	private static void appendln(String S, Appendable out) {
		try {
			out.append(S);
			out.append(EOL);
		} catch(IOException err) {
			// Ignored
		}
	}

	private static void append(char ch, Appendable out) {
		try {
			out.append(ch);
		} catch(IOException err) {
			// Ignored
		}
	}

	private static void append(Object O, Appendable out) {
		append(O==null ? "null" : O.toString(), out);
	}

	private static void appendln(Object O, Appendable out) {
		appendln(O==null ? "null" : O.toString(), out);
	}

	/**
	 * Prints a detailed error report, including all stack traces, to the provided out.
	 * Synchronizes on out to make sure concurrently reported errors will not be mixed.
	 * If out is {@link Flushable}, will flush the output.
	 */
	public static void printStackTraces(Throwable thrown, Appendable out, Object... extraInfo) {

		synchronized(out) {
			appendln(out);
			appendln("**************************", out);
			appendln("* BEGIN EXCEPTION REPORT *", out);
			appendln("**************************", out);
			appendln(out);
			appendln("    Time ", out);
			append("        ", out);
			try {
				appendln(new java.util.Date(System.currentTimeMillis()).toString(), out);
			} catch(Exception err) {
				append("Unable to display date: ", out); appendln(err.toString(), out);
			}

			// Extra info
			if(extraInfo!=null && extraInfo.length>0) {
				appendln("    Extra Information", out);
				for (Object ei : extraInfo) {
					append("        ", out);
					appendln(ei, out);
				}
			}

			// Threads
			appendln("    Threading", out);
			Thread thread=Thread.currentThread();
			appendln("        Thread", out);
			append("            ID..........: ", out);
			appendln(Long.toString(thread.getId()), out);
			append("            Name........: ", out);
			appendln(thread.getName(), out);
			append("            Daemon......: ", out);
			appendln(Boolean.toString(thread.isDaemon()), out);
			append("            Class.......: ", out);
			appendln(thread.getClass().getName(), out);
			append("            Priority....: ", out);
			appendln(thread.getPriority(), out);
			try {
				ThreadGroup TG=thread.getThreadGroup();
				while(TG!=null) {
					String name=TG.getName();
					String classname=TG.getClass().getName();
					int maxPriority=TG.getMaxPriority();
					appendln("        ThreadGroup", out);
					append("            Name........: ", out); appendln(name, out);
					append("            Class.......: ", out); appendln(classname, out);
					append("            Max Priority: ", out); appendln(maxPriority, out);
					TG=TG.getParent();
				}
			} catch(SecurityException err) {
				append("Unable to print all Thread Groups: ", out); appendln(err.toString(), out);
			}

			appendln("    Exceptions", out);
			if(thrown==null) appendln("        No exceptions", out);
			else {
				List<Throwable> closed = new ArrayList<>();
				closed.add(thrown);
				printThrowables(thrown, out, 8, closed);
			}

			// End Report
			appendln(out);
			appendln("**************************", out);
			appendln("*  END EXCEPTION REPORT  *", out);
			appendln("**************************", out);

			// Flush output
			try {
				if(out instanceof Flushable) ((Flushable)out).flush();
			} catch(IOException err) {
				// Ignored
			}
		}
	}

	private static boolean isClosed(Throwable thrown, List<Throwable> closed) {
		for(Throwable T : closed) {
			if(T==thrown) return true;
		}
		return false;
	}

	private static void indent(Appendable out, int indent) {
		for(int c = 0; c < indent; c++) {
			append(' ', out);
		}
	}

	private static void printThrowables(Throwable thrown, Appendable out, int indent, List<Throwable> closed) {
		indent(out, indent);
		appendln(thrown.getClass().getName(), out);
		printMessage(out, indent+4, "Message...........: ", thrown.getMessage());
		printMessage(out, indent+4, "Localized Message.: ", thrown.getLocalizedMessage());
		if(thrown instanceof SQLException) {
			SQLException sql=(SQLException)thrown;
			if(sql instanceof WrappedSQLException) printMessage(out, indent+4, "SQL Statement.....: ", ((WrappedSQLException)sql).getSqlString());
			indent(out, indent + 4);
			append("SQL Error Code....: ", out);
			appendln(sql.getErrorCode(), out);
			indent(out, indent + 4);
			append("SQL State.........: ", out);
			appendln(sql.getSQLState(), out);
		} else if(thrown instanceof WrappedException) {
			WrappedException wrapped=(WrappedException)thrown;
			Object[] wrappedInfo=wrapped.getExtraInfo();
			if(wrappedInfo!=null && wrappedInfo.length>0) {
				indent(out, indent + 4);
				appendln("Extra Information", out);
				for (Object wi : wrappedInfo) {
					indent(out, indent + 8);
					appendln(wi, out);
				}
			}
		} else if(thrown instanceof AccessControlException) {
			try {
				AccessControlException ace = (AccessControlException)thrown;
				Permission permission = ace.getPermission();
				indent(out, indent + 4);
				append("Permission........: ", out);
				appendln(permission, out);
				if(permission!=null) {
					indent(out, indent + 4);
					append("Permission Class..: ", out);
					appendln(permission.getClass().getName(), out);

					indent(out, indent + 4);
					append("Permission Name...: ", out);
					appendln(permission.getName(), out);

					indent(out, indent + 4);
					append("Permission Actions: ", out);
					appendln(permission.getActions(), out);
				}
			} catch(SecurityException err) {
				appendln("Permission........: Unable to get permission details: ", out); append(err.toString(), out);
			}
		}
		indent(out, indent + 4);
		appendln("Stack Trace", out);
		StackTraceElement[] stack=thrown.getStackTrace();
		for (StackTraceElement ste : stack) {
			indent(out, indent + 8);
			append("at ", out);
			appendln(ste.toString(), out);
		}
		if(thrown instanceof WrappedExceptions) {
			for(Throwable cause : ((WrappedExceptions)thrown).getCauses()) {
				if(!isClosed(cause, closed)) {
					closed.add(cause);
					indent(out, indent + 4);
					appendln("Caused By", out);
					printThrowables(cause, out, indent + 8, closed);
				}
			}
		} else {
			Throwable cause=thrown.getCause();
			if(cause!=null) {
				if(!isClosed(cause, closed)) {
					closed.add(cause);
					indent(out, indent + 4);
					appendln("Caused By", out);
					printThrowables(cause, out, indent + 8, closed);
				}
			}
		}
		// Uses reflection avoid binding to JspException directly.
		try {
			Class<?> clazz=thrown.getClass();
			if(isSubclass(clazz, "javax.servlet.jsp.JspException")) {
				Method method=clazz.getMethod("getRootCause");
				Throwable rootCause=(Throwable)method.invoke(thrown);
				if(rootCause!=null) {
					if(!isClosed(rootCause, closed)) {
						closed.add(rootCause);
						indent(out, indent + 4);
						appendln("Caused By", out);
						printThrowables(rootCause, out, indent + 8, closed);
					}
				}
			}
		} catch(
			// OK, future versions of JspException might not have getRootCause
			NoSuchMethodException
			// OK, future versions of JspException could make it private
			| IllegalAccessException
			// Ignored because we are dealing with one exception at a time
			// Afterall, this is the exception handling code
			| InvocationTargetException
			ignored
		) {
			// Do nothing
		}
		// Uses reflection avoid binding to ServletException directly.
		try {
			Class<?> clazz=thrown.getClass();
			if(isSubclass(clazz, "javax.servlet.ServletException")) {
				Method method=clazz.getMethod("getRootCause");
				Throwable rootCause=(Throwable)method.invoke(thrown);
				if(rootCause!=null) {
					if(!isClosed(rootCause, closed)) {
						closed.add(rootCause);
						indent(out, indent + 4);
						appendln("Caused By", out);
						printThrowables(rootCause, out, indent + 8, closed);
					}
				}
			}
		} catch(
			// OK, future versions of ServletException might not have getRootCause
			NoSuchMethodException
			// OK, future versions of ServletException could make it private
			| IllegalAccessException
			// Ignored because we are dealing with one exception at a time
			// Afterall, this is the exception handling code
			| InvocationTargetException
			ignored
		) {
			// Do nothing
		}
		for(Throwable suppressed : thrown.getSuppressed()) {
			if(!isClosed(suppressed, closed)) {
				closed.add(suppressed);
				indent(out, indent + 4);
				appendln("Suppressed", out);
				printThrowables(suppressed, out, indent + 8, closed);
			}
		}
		if(thrown instanceof SQLException) {
			SQLException nextSQL = ((SQLException)thrown).getNextException();
			if(nextSQL != null) {
				List<SQLException> nextSQLs = new ArrayList<>();
				do {
					if(!isClosed(nextSQL, closed)) {
						nextSQLs.add(nextSQL);
					}
				} while ((nextSQL = nextSQL.getNextException()) != null);
				closed.addAll(nextSQLs);
				for(SQLException next : nextSQLs) {
					printThrowables(next, out, indent, closed);
				}
			}
		}
	}

	private static void printMessage(Appendable out, int indent, String label, String message) {
		indent(out, indent);
		append(label, out);
		if(message==null) {
			appendln("null", out);
		} else {
			message=message.trim();
			int messageLen=message.length();
			for(int c=0;c<messageLen;c++) {
				char ch=message.charAt(c);
				if(ch=='\n') {
					int lineIndent = indent + label.length();
					appendln(out);
					indent(out, lineIndent);
				} else if(ch!='\r') append(ch, out);
			}
			appendln(out);
		}
	}

	private static boolean isSubclass(Class<?> clazz, String classname) {
		while(clazz!=null) {
			if(clazz.getName().equals(classname)) return true;
			clazz=clazz.getSuperclass();
		}
		return false;
	}

	/**
	 * Gets the entire exception report as a <code>String</code>.  This is not
	 * as efficient as directly writing the report due to the extra buffering.
	 */
	public static String getStackTraces(Throwable T) {
		return getStackTraces(T, (Object[])null);
	}

	/**
	 * Gets the entire exception report as a <code>String</code>.  This is not
	 * as efficient as directly writing the report due to the extra buffering.
	 */
	public static String getStackTraces(Throwable thrown, Object... extraInfo) {
		StringBuilder out = new StringBuilder();
		printStackTraces(thrown, out, extraInfo);
		return out.toString();
	}
}
