/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2013, 2016, 2017, 2019  AO Industries, Inc.
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

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Calendar utilities.
 *
 * @author  AO Industries, Inc.
 */
public class CalendarUtils {

	private CalendarUtils() {
	}

	/**
	 * Gets the date from the YYYY-MM-DD format in the given time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months and days like "1976-1-9".
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
	 *
	 * @see  #parseDate(java.lang.String)
	 */
	public static GregorianCalendar parseDate(String yyyy_mm_dd, TimeZone timeZone) throws IllegalArgumentException {
		if(yyyy_mm_dd == null) return null;
		int pos1 = yyyy_mm_dd.indexOf('-', 1); // Start search at second character to allow negative years: -1000-01-23
		if(pos1 == -1) throw new IllegalArgumentException("Invalid date: "  +yyyy_mm_dd);
		int pos2 = yyyy_mm_dd.indexOf('-', pos1 + 1);
		if(pos2 == -1) throw new IllegalArgumentException("Invalid date: " + yyyy_mm_dd);
		int year = Integer.parseInt(yyyy_mm_dd.substring(0, pos1));
		int month = Integer.parseInt(yyyy_mm_dd.substring(pos1 + 1, pos2));
		if(month < 1 || month > 12) throw new IllegalArgumentException("Invalid month: " + yyyy_mm_dd);
		int day = Integer.parseInt(yyyy_mm_dd.substring(pos2 + 1));
		GregorianCalendar gcal = timeZone == null ? new GregorianCalendar() : new GregorianCalendar(timeZone);
		gcal.set(Calendar.YEAR, year);
		gcal.set(Calendar.MONTH, month - 1);
		if(day < 1 || day > gcal.getActualMaximum(Calendar.DATE)) throw new IllegalArgumentException("Invalid day of month: " + yyyy_mm_dd);
		gcal.set(Calendar.DATE, day);
		gcal.set(Calendar.HOUR_OF_DAY, 0);
		gcal.set(Calendar.MINUTE, 0);
		gcal.set(Calendar.SECOND, 0);
		gcal.set(Calendar.MILLISECOND, 0);
		return gcal;
	}

	/**
	 * Gets the date from the YYYY-MM-DD format in the default time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months and days like "1976-1-9".
	 *
	 * @see  #parseDate(java.lang.String, java.util.TimeZone)
	 */
	public static GregorianCalendar parseDate(String yyyy_mm_dd) throws IllegalArgumentException {
		return parseDate(yyyy_mm_dd, null);
	}

	/**
	 * Formats a date in YYYY-MM-DD format.
	 *
	 * @return  the formatted date or {@code null} if the parameter is {@code null}
	 */
	public static String formatDate(Calendar cal) {
		if(cal == null) return null;
		try {
			StringBuilder result = new StringBuilder("YYYY-MM-DD".length());
			formatDate(cal, result);
			return result.toString();
		} catch(IOException e) {
			throw new AssertionError("IOException should never occur on StringBuilder", e);
		}
	}

	/**
	 * Formats a date in YYYY-MM-DD format.
	 */
	public static void formatDate(Calendar cal, Appendable out) throws IOException {
		if(cal != null) {
			GregorianCalendar gcal;
			if(cal instanceof GregorianCalendar) gcal = (GregorianCalendar)cal;
			else gcal = new GregorianCalendar(cal.getTimeZone());
			// year
			out.append(Integer.toString(gcal.get(Calendar.YEAR)));
			out.append('-');
			// Month
			int month = gcal.get(Calendar.MONTH) + 1;
			if(month < 10) out.append('0');
			out.append(Integer.toString(month));
			out.append('-');
			// Day
			int day = gcal.get(Calendar.DAY_OF_MONTH);
			if(day < 10) out.append('0');
			out.append(Integer.toString(day));
		}
	}

	/**
	 * Gets today's date in the given time zone.  Hour, minute, second, and millisecond are all set to zero.
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
	 *
	 * @see  #getToday()
	 */
	public static GregorianCalendar getToday(TimeZone timeZone) {
		GregorianCalendar gcal = timeZone == null ? new GregorianCalendar() : new GregorianCalendar(timeZone);
		gcal.set(Calendar.HOUR_OF_DAY, 0);
		gcal.set(Calendar.MINUTE, 0);
		gcal.set(Calendar.SECOND, 0);
		gcal.set(Calendar.MILLISECOND, 0);
		return gcal;
	}

	/**
	 * Gets today's date in the default time zone.  Hour, minute, second, and millisecond are all set to zero.
	 *
	 * @see  #getToday(java.util.TimeZone)
	 */
	public static GregorianCalendar getToday() {
		return getToday(null);
	}
}
