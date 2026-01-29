/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2016, 2017, 2019, 2020, 2021, 2022, 2024, 2025  AO Industries, Inc.
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

package com.aoapps.lang.util;

import com.aoapps.lang.exception.ExtraInfo;
import com.aoapps.lang.exception.WrappedExceptions;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.Permission;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

/**
 * Prints errors with more detail than a standard printStackTrace() call.  Is also able to
 * capture the error into a <code>String</code>.
 *
 * <p>TODO: Avoid repetitive sequences of stack traces to reduce total output length.</p>
 *
 * <p>TODO: Make an extensible way to register additional error printer features, and
 * automatically load them via {@link ServiceLoader}.  Maybe spin this off to a
 * microproject if we go this far.</p>
 *
 * @author  AO Industries, Inc.
 */
// TODO: Move to com.aoapps.lang.Throwables?
// TODO: Support SQLClientInfoException
// TODO: Support MessagingException.getNextException?
public final class ErrorPrinter {

  /** Make no instances. */
  private ErrorPrinter() {
    throw new AssertionError();
  }

  private static final String EOL = System.lineSeparator();

  private static class IdentityKey {

    private final Throwable throwable;

    private IdentityKey(Throwable throwable) {
      this.throwable = throwable;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(throwable);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
      return this == obj;
    }
  }

  private static final Map<IdentityKey, List<String>> statements = new WeakHashMap<>();

  /**
   * Adds a new mapping between a throwable and the statement that caused it.
   *
   * @param  sql  The SQL statement that caused the exception.
   */
  public static void addSql(Throwable t, String sql) {
    if (t != null && sql != null) {
      IdentityKey key = new IdentityKey(t);
      synchronized (statements) {
        List<String> causes = statements.get(key);
        if (causes == null) {
          statements.put(key, causes = new ArrayList<>());
        }
        causes.add(sql);
      }
    }
  }

  /**
   * Adds a new mapping between a throwable and the statement that caused it.
   *
   * @param  pstmt  The SQL statement that caused the exception.
   *                This must provide the SQL statement from {@link PreparedStatement}{@code .toString()},
   *                which the PostgreSQL JDBC driver does.
   */
  public static void addSql(Throwable t, PreparedStatement pstmt) {
    if (t != null && pstmt != null) {
      addSql(t, pstmt.toString());
    }
  }

  /**
   * Adds a new mapping between a throwable and the statement that caused it.
   *
   * @param  sql  The SQL statement that caused the exception.
   *
   * @deprecated  Please use {@link ErrorPrinter#addSql(java.lang.Throwable, java.lang.String)} instead.
   */
  // TODO: Remove in 6.0.0 release
  @Deprecated
  public static void addSQL(Throwable t, String sql) {
    addSql(t, sql);
  }

  /**
   * Adds a new mapping between a throwable and the statement that caused it.
   *
   * @param  pstmt  The SQL statement that caused the exception.
   *                This must provide the SQL statement from {@link PreparedStatement}{@code .toString()},
   *                which the PostgreSQL JDBC driver does.
   *
   * @deprecated  Please use {@link ErrorPrinter#addSql(java.lang.Throwable, java.sql.PreparedStatement)} instead.
   */
  // TODO: Remove in 6.0.0 release
  @Deprecated
  public static void addSQL(Throwable t, PreparedStatement pstmt) {
    addSql(t, pstmt);
  }

  /**
   * Gets the mappings between the given throwable and any statements that caused it.
   *
   * @return  The SQL statements that caused the exception or an empty list when none.
   */
  public static List<String> getSql(Throwable t) {
    if (t == null) {
      return Collections.emptyList();
    } else {
      IdentityKey key = new IdentityKey(t);
      synchronized (statements) {
        List<String> causes = statements.get(key);
        return (causes == null)
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(causes));
      }
    }
  }

  /**
   * Gets the mappings between the given throwable and any statements that caused it.
   *
   * @return  The SQL statements that caused the exception or an empty list when none.
   *
   * @deprecated  Please use {@link ErrorPrinter#getSql(java.lang.Throwable)} instead.
   */
  // TODO: Remove in 6.0.0 release
  @Deprecated
  public static List<String> getSQL(Throwable t) {
    return getSql(t);
  }

  /**
   * @deprecated  Please use {@link ErrorPrinter#printStackTraces(java.lang.Throwable, java.lang.Appendable)} with {@link System#err}
   */
  @Deprecated
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void printStackTraces(Throwable t) {
    printStackTraces(t, System.err, (Object[]) null);
  }

  /**
   * @deprecated  Please use {@link ErrorPrinter#printStackTraces(java.lang.Throwable, java.lang.Appendable, java.lang.Object...)} with {@link System#err}
   */
  @Deprecated
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void printStackTraces(Throwable t, Object... extraInfo) {
    printStackTraces(t, System.err, extraInfo);
  }

  public static void printStackTraces(Throwable t, Appendable out) {
    printStackTraces(t, out, (Object[]) null);
  }

  private static void appendln(Appendable out) {
    try {
      out.append(EOL);
    } catch (IOException err) {
      // Ignored
    }
  }

  private static void append(String s, Appendable out) {
    try {
      out.append(s);
    } catch (IOException err) {
      // Ignored
    }
  }

  private static void appendln(String s, Appendable out) {
    try {
      out.append(s);
      out.append(EOL);
    } catch (IOException err) {
      // Ignored
    }
  }

  private static void append(char ch, Appendable out) {
    try {
      out.append(ch);
    } catch (IOException err) {
      // Ignored
    }
  }

  private static void append(Object obj, Appendable out) {
    append(obj == null ? "null" : obj.toString(), out);
  }

  private static void appendln(Object obj, Appendable out) {
    appendln(obj == null ? "null" : obj.toString(), out);
  }

  /**
   * Prints a detailed error report, including all stack traces, to the provided out.
   * Synchronizes on out to make sure concurrently reported errors will not be mixed.
   * If out is {@link Flushable}, will flush the output.
   */
  public static void printStackTraces(Throwable thrown, Appendable out, Object... extraInfo) {

    synchronized (out) {
      appendln(out);
      appendln("**************************", out);
      appendln("* BEGIN EXCEPTION REPORT *", out);
      appendln("**************************", out);
      appendln(out);
      appendln("    Time ", out);
      append("        ", out);
      try {
        appendln(new java.util.Date(System.currentTimeMillis()).toString(), out);
      } catch (Exception err) {
        append("Unable to display date: ", out);
        appendln(err.toString(), out);
      }

      // Extra info
      if (extraInfo != null && extraInfo.length > 0) {
        appendln("    Extra Information", out);
        for (Object ei : extraInfo) {
          append("        ", out);
          appendln(ei, out);
        }
      }

      // Threads
      appendln("    Threading", out);
      Thread thread = Thread.currentThread();
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
        // Java 19: java.lang.ThreadGroup Is Degraded, see https://bugs.openjdk.org/browse/JDK-8284161
        ThreadGroup tg = thread.getThreadGroup();
        while (tg != null) {
          final String name = tg.getName();
          final String classname = tg.getClass().getName();
          final int maxPriority = tg.getMaxPriority();
          appendln("        ThreadGroup", out);
          append("            Name........: ", out);
          appendln(name, out);
          append("            Class.......: ", out);
          appendln(classname, out);
          append("            Max Priority: ", out);
          appendln(maxPriority, out);
          tg = tg.getParent();
        }
      } catch (SecurityException err) {
        append("Unable to print all Thread Groups: ", out);
        appendln(err.toString(), out);
      }

      appendln("    Exceptions", out);
      if (thrown == null) {
        appendln("        No exceptions", out);
      } else {
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
        if (out instanceof Flushable) {
          ((Flushable) out).flush();
        }
      } catch (IOException err) {
        // Ignored
      }
    }
  }

  private static boolean isClosed(Throwable thrown, List<Throwable> closed) {
    for (Throwable t : closed) {
      if (t == thrown) {
        return true;
      }
    }
    return false;
  }

  private static void indent(Appendable out, int indent) {
    for (int c = 0; c < indent; c++) {
      append(' ', out);
    }
  }

  @FunctionalInterface
  public static interface CustomMessageHandler {
    static void printMessage(Appendable out, int indent, String label, String message) {
      if (label != null || message != null) {
        indent(out, indent);
        if (label != null) {
          append(label, out);
        }
        if (message == null) {
          assert label != null;
          appendln("null", out);
        } else {
          message = message.trim();
          int messageLen = message.length();
          for (int c = 0; c < messageLen; c++) {
            char ch = message.charAt(c);
            if (ch == '\n') {
              appendln(out);
              indent(out, indent + (label == null ? 0 : label.length()));
            } else if (ch != '\r') {
              append(ch, out);
            }
          }
          appendln(out);
        }
      }
    }

    void printCustomMessages(Throwable thrown, Appendable out, int indent);
  }

  private static final List<CustomMessageHandler> customMessageHandlers = new ArrayList<>();

  public static void addCustomMessageHandler(CustomMessageHandler handler) {
    synchronized (customMessageHandlers) {
      customMessageHandlers.add(handler);
    }
  }

  /**
   * The expected number of characters before the colon in a label.
   */
  private static final int LABEL_WIDTH = 18;

  private static void printThrowables(Throwable thrown, Appendable out, int indent, List<Throwable> closed) {
    indent(out, indent);
    appendln(thrown.getClass().getName(), out);
    String message = thrown.getMessage();
    if (message != null) {
      CustomMessageHandler.printMessage(out, indent + 4, "Message...........: ", message);
    }
    String localizedMessage = thrown.getLocalizedMessage();
    if (localizedMessage != null && !localizedMessage.equals(message)) {
      CustomMessageHandler.printMessage(out, indent + 4, "Localized Message.: ", localizedMessage);
    }
    synchronized (customMessageHandlers) {
      for (CustomMessageHandler handler : customMessageHandlers) {
        handler.printCustomMessages(thrown, out, indent + 4);
      }
    }
    if (thrown instanceof ExtraInfo) {
      Object[] extraInfo = ((ExtraInfo) thrown).getExtraInfo();
      if (extraInfo != null && extraInfo.length > 0) {
        indent(out, indent + 4);
        appendln("Extra Information", out);
        for (Object wi : extraInfo) {
          indent(out, indent + 8);
          appendln(wi, out);
        }
      }
    }
    if (thrown instanceof SQLException) {
      SQLException sql = (SQLException) thrown;
      indent(out, indent + 4);
      append("SQL Error Code....: ", out);
      appendln(sql.getErrorCode(), out);
      indent(out, indent + 4);
      append("SQL State.........: ", out);
      appendln(sql.getSQLState(), out);
    } else if (thrown instanceof AccessControlException) {
      try {
        AccessControlException ace = (AccessControlException) thrown;
        Permission permission = ace.getPermission();
        indent(out, indent + 4);
        append("Permission........: ", out);
        appendln(permission, out);
        if (permission != null) {
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
      } catch (SecurityException err) {
        appendln("Permission........: Unable to get permission details: ", out);
        append(err.toString(), out);
      }
    }
    // SQL Statements (not within SQLException, since they can be associated with any throwable)
    {
      List<String> causes = getSql(thrown);
      int size = causes.size();
      if (size != 0) {
        final String labelPre = "SQL Statement";
        StringBuilder label = new StringBuilder(labelPre);
        for (int i = 0; i < size; i++) {
          label.setLength(labelPre.length());
          if (size != 1) {
            label.append(" #").append(i + 1);
          }
          while (label.length() < LABEL_WIDTH) {
            label.append('.');
          }
          label.append(": ");
          CustomMessageHandler.printMessage(out, indent + 4, label.toString(), causes.get(i));
        }
      }
    }
    indent(out, indent + 4);
    appendln("Stack Trace", out);
    StackTraceElement[] stack = thrown.getStackTrace();
    for (StackTraceElement ste : stack) {
      indent(out, indent + 8);
      append("at ", out);
      appendln(ste.toString(), out);
    }
    if (thrown instanceof WrappedExceptions) {
      for (Throwable cause : ((WrappedExceptions) thrown).getCauses()) {
        if (!isClosed(cause, closed)) {
          closed.add(cause);
          indent(out, indent + 4);
          appendln("Caused By", out);
          printThrowables(cause, out, indent + 8, closed);
        }
      }
    } else {
      Throwable cause = thrown.getCause();
      if (cause != null) {
        if (!isClosed(cause, closed)) {
          closed.add(cause);
          indent(out, indent + 4);
          appendln("Caused By", out);
          printThrowables(cause, out, indent + 8, closed);
        }
      }
    }
    // Uses reflection avoid binding to JspException directly.
    try {
      Class<?> clazz = thrown.getClass();
      if (isSubclass(clazz, "javax.servlet.jsp.JspException")) {
        Method method = clazz.getMethod("getRootCause");
        Throwable rootCause = (Throwable) method.invoke(thrown);
        if (rootCause != null) {
          if (!isClosed(rootCause, closed)) {
            closed.add(rootCause);
            indent(out, indent + 4);
            appendln("Caused By", out);
            printThrowables(rootCause, out, indent + 8, closed);
          }
        }
      }
    } catch (
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
      Class<?> clazz = thrown.getClass();
      if (isSubclass(clazz, "javax.servlet.ServletException")) {
        Method method = clazz.getMethod("getRootCause");
        Throwable rootCause = (Throwable) method.invoke(thrown);
        if (rootCause != null) {
          if (!isClosed(rootCause, closed)) {
            closed.add(rootCause);
            indent(out, indent + 4);
            appendln("Caused By", out);
            printThrowables(rootCause, out, indent + 8, closed);
          }
        }
      }
    } catch (
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
    for (Throwable suppressed : thrown.getSuppressed()) {
      if (!isClosed(suppressed, closed)) {
        closed.add(suppressed);
        indent(out, indent + 4);
        appendln("Suppressed", out);
        printThrowables(suppressed, out, indent + 8, closed);
      }
    }
    if (thrown instanceof SQLException) {
      SQLException nextSqlException = ((SQLException) thrown).getNextException();
      if (nextSqlException != null) {
        List<SQLException> nextSqlExceptions = new ArrayList<>();
        do {
          if (!isClosed(nextSqlException, closed)) {
            nextSqlExceptions.add(nextSqlException);
          }
        } while ((nextSqlException = nextSqlException.getNextException()) != null);
        closed.addAll(nextSqlExceptions);
        for (SQLException next : nextSqlExceptions) {
          printThrowables(next, out, indent, closed);
        }
      }
    }
  }

  private static boolean isSubclass(Class<?> clazz, String classname) {
    while (clazz != null) {
      if (clazz.getName().equals(classname)) {
        return true;
      }
      clazz = clazz.getSuperclass();
    }
    return false;
  }

  /**
   * Gets the entire exception report as a <code>String</code>.  This is not
   * as efficient as directly writing the report due to the extra buffering.
   */
  public static String getStackTraces(Throwable t) {
    return getStackTraces(t, (Object[]) null);
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
