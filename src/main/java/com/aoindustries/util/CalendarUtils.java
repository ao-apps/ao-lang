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
	 * Formats a date in "YYYY-MM-DD" format.
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
	 * Formats a date in "YYYY-MM-DD" format.
	 */
	public static void formatDate(Calendar cal, Appendable out) throws IOException {
		if(cal != null) {
			Calendar gcal;
			if(UnmodifiableCalendar.isInstanceOf(cal, GregorianCalendar.class)) {
				gcal = cal;
			} else {
				gcal = new GregorianCalendar(cal.getTimeZone());
				gcal.setTimeInMillis(cal.getTimeInMillis());
			}
			// Year
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
	 * Gets the date from the "YYYY-MM-DD" format in the given time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months and days like "1976-1-9".
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
	 */
	public static GregorianCalendar parseDate(String yyyy_mm_dd, TimeZone timeZone) throws IllegalArgumentException {
		if(yyyy_mm_dd == null) return null;

		final int year, month, day;

		yyyy_mm_dd = yyyy_mm_dd.trim();
		int pos1 = yyyy_mm_dd.indexOf('-', 1); // Start search at second character to allow negative years: -1000-01-23
		if(pos1 == -1) throw new IllegalArgumentException("Invalid date: " + yyyy_mm_dd);
		int pos2 = yyyy_mm_dd.indexOf('-', pos1 + 1);
		if(pos2 == -1) throw new IllegalArgumentException("Invalid date: " + yyyy_mm_dd);
		year = Integer.parseInt(yyyy_mm_dd.substring(0, pos1).trim());
		month = Integer.parseInt(yyyy_mm_dd.substring(pos1 + 1, pos2).trim());
		day = Integer.parseInt(yyyy_mm_dd.substring(pos2 + 1).trim());

		GregorianCalendar gcal = timeZone == null ? new GregorianCalendar() : new GregorianCalendar(timeZone);
		gcal.set(Calendar.YEAR, year);
		if(month < 1 || month > 12) throw new IllegalArgumentException("Invalid month: " + yyyy_mm_dd);
		gcal.set(Calendar.MONTH, month - 1);
		gcal.set(Calendar.DATE, 1);
		if(day < 1 || day > gcal.getActualMaximum(Calendar.DATE)) throw new IllegalArgumentException("Invalid day of month: " + yyyy_mm_dd);
		gcal.set(Calendar.DATE, day);
		gcal.set(Calendar.HOUR_OF_DAY, 0);
		gcal.set(Calendar.MINUTE, 0);
		gcal.set(Calendar.SECOND, 0);
		gcal.set(Calendar.MILLISECOND, 0);
		return gcal;
	}

	/**
	 * Gets the date from the "YYYY-MM-DD" format in the default time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months and days like "1976-1-9".
	 */
	public static GregorianCalendar parseDate(String yyyy_mm_dd) throws IllegalArgumentException {
		return parseDate(yyyy_mm_dd, null);
	}

	/**
	 * Formats a time in "HH:MM:SS" format.
	 *
	 * @return  the formatted time or {@code null} if the parameter is {@code null}
	 */
	public static String formatTime(Calendar cal) {
		if(cal == null) return null;
		try {
			StringBuilder result = new StringBuilder("HH:MM:SS".length());
			formatTime(cal, result);
			return result.toString();
		} catch(IOException e) {
			throw new AssertionError("IOException should never occur on StringBuilder", e);
		}
	}

	/**
	 * Formats a time in "HH:MM:SS" format.
	 */
	public static void formatTime(Calendar cal, Appendable out) throws IOException {
		if(cal != null) {
			Calendar gcal;
			if(UnmodifiableCalendar.isInstanceOf(cal, GregorianCalendar.class)) {
				gcal = cal;
			} else {
				gcal = new GregorianCalendar(cal.getTimeZone());
				gcal.setTimeInMillis(cal.getTimeInMillis());
			}
			// Hour
			int hour = gcal.get(Calendar.HOUR_OF_DAY);
			if(hour < 10) out.append('0');
			out.append(Integer.toString(hour));
			out.append(':');
			// Minute
			int minute = gcal.get(Calendar.MINUTE);
			if(minute < 10) out.append('0');
			out.append(Integer.toString(minute));
			out.append(':');
			// Second
			int second = gcal.get(Calendar.SECOND);
			if(second < 10) out.append('0');
			out.append(Integer.toString(second));
		}
	}

	/**
	 * Formats a date and time in "YYYY-MM-DD HH:MM:SS" format.
	 *
	 * @return  the formatted date and time or {@code null} if the parameter is {@code null}
	 */
	public static String formatDateTime(Calendar cal) {
		if(cal == null) return null;
		try {
			StringBuilder result = new StringBuilder("YYYY-MM-DD HH:MM:SS".length());
			formatDateTime(cal, result);
			return result.toString();
		} catch(IOException e) {
			throw new AssertionError("IOException should never occur on StringBuilder", e);
		}
	}

	/**
	 * Formats a date and time in "YYYY-MM-DD HH:MM:SS" format.
	 */
	public static void formatDateTime(Calendar cal, Appendable out) throws IOException {
		if(cal != null) {
			formatDate(cal, out);
			out.append(' ');
			formatTime(cal, out);
		}
	}

	public static interface DateTimeProducer<T> {
		/**
		 * @param gcal  Has the full millisecond precision set
		 * @param nanos  The fully parsed nanoseconds
		 */
		T createDateTime(GregorianCalendar gcal, int nanos);
	}

	/**
	 * Gets the date and time from the "YYYY-MM-DD[ HH:MM[:SS[.nnnnnnnnn]]]" format in the given time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months, days, hours, minutes, and millis like "1976-1-9 1:2:3.1".
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
	 */
	public static <T> T parseDateTime(String dateTime, TimeZone timeZone, DateTimeProducer<T> producer) throws IllegalArgumentException {
		if(dateTime == null) return null;

		final int year, month, day, hour, minute, second, nanos;

		dateTime = dateTime.trim();
		int pos1 = dateTime.indexOf('-', 1); // Start search at second character to allow negative years: -1000-01-23
		if(pos1 == -1) throw new IllegalArgumentException("Invalid datetime: " + dateTime);
		int pos2 = dateTime.indexOf('-', pos1 + 1);
		if(pos2 == -1) throw new IllegalArgumentException("Invalid datetime: " + dateTime);
		year = Integer.parseInt(dateTime.substring(0, pos1).trim());
		month = Integer.parseInt(dateTime.substring(pos1 + 1, pos2).trim());
		int pos3 = dateTime.indexOf(' ', pos2 + 1);
		if(pos3 == -1) {
			day = Integer.parseInt(dateTime.substring(pos2 + 1).trim());
			hour = minute = second = nanos = 0;
		} else {
			day = Integer.parseInt(dateTime.substring(pos2 + 1, pos3).trim());
			int pos4 = dateTime.indexOf(':', pos3 + 1);
			if(pos4 == -1) throw new IllegalArgumentException("Invalid datetime: " + dateTime);
			hour = Integer.parseInt(dateTime.substring(pos3 + 1, pos4).trim());
			int pos5 = dateTime.indexOf(':', pos4 + 1);
			if(pos5 == -1) {
				minute = Integer.parseInt(dateTime.substring(pos4 + 1).trim());
				second = nanos = 0;
			} else {
				minute = Integer.parseInt(dateTime.substring(pos4 + 1, pos5).trim());
				int pos6 = dateTime.indexOf('.', pos5 + 1);
				if(pos6 == -1) {
					String secondString = dateTime.substring(pos5 + 1).trim();
					second = secondString.isEmpty() ? 0 : Integer.parseInt(secondString);
					nanos = 0;
				} else {
					second = Integer.parseInt(dateTime.substring(pos5 + 1, pos6).trim());
					String nanosString = dateTime.substring(pos6 + 1).trim();
					int len = nanosString.length();
					if(len == 0) {
						nanos = 0;
					} else if(len == 1) {
						nanos = 100000000 * Integer.parseInt(nanosString);
					} else if(len == 2) {
						nanos = 10000000 * Integer.parseInt(nanosString);
					} else if(len == 3) {
						nanos = 1000000 * Integer.parseInt(nanosString);
					} else if(len == 4) {
						nanos = 100000 * Integer.parseInt(nanosString);
					} else if(len == 5) {
						nanos = 10000 * Integer.parseInt(nanosString);
					} else if(len == 6) {
						nanos = 1000 * Integer.parseInt(nanosString);
					} else if(len == 7) {
						nanos = 100 * Integer.parseInt(nanosString);
					} else if(len == 8) {
						nanos = 10 * Integer.parseInt(nanosString);
					} else {
						nanos = Integer.parseInt(nanosString);
					}
				}
			}
		}

		GregorianCalendar gcal = timeZone == null ? new GregorianCalendar() : new GregorianCalendar(timeZone);
		gcal.set(Calendar.YEAR, year);
		if(month < 1 || month > 12) throw new IllegalArgumentException("Invalid month: " + dateTime);
		gcal.set(Calendar.MONTH, month - 1);
		gcal.set(Calendar.DATE, 1);
		if(day < 1 || day > gcal.getActualMaximum(Calendar.DATE)) throw new IllegalArgumentException("Invalid day of month: " + dateTime);
		if(hour < 0 || hour > 23) throw new IllegalArgumentException("Invalid hour: " + dateTime);
		if(minute < 0 || minute > 59) throw new IllegalArgumentException("Invalid minute: " + dateTime);
		if(second < 0 || second > 59) throw new IllegalArgumentException("Invalid second: " + dateTime);
		if(nanos < 0 || nanos > 999999999) throw new IllegalArgumentException("Invalid nanos: " + dateTime);
		gcal.set(Calendar.DATE, day);
		gcal.set(Calendar.HOUR_OF_DAY, hour);
		gcal.set(Calendar.MINUTE, minute);
		gcal.set(Calendar.SECOND, second);
		gcal.set(Calendar.MILLISECOND, nanos / 1000000);
		return producer.createDateTime(gcal, nanos);
	}

	/**
	 * Gets the date and time from the "YYYY-MM-DD[ HH:MM[:SS[.mmm]]]" format in the given time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months, days, hours, minutes, and millis like "1976-1-9 1:2:3.1".
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
	 */
	public static GregorianCalendar parseDateTime(String dateTime, TimeZone timeZone) throws IllegalArgumentException {
		return parseDateTime(
			dateTime,
			timeZone,
			new DateTimeProducer<GregorianCalendar>() {
				@Override
				public GregorianCalendar createDateTime(GregorianCalendar gcal, int nanos) {
					if((nanos % 1000000) != 0) throw new IllegalArgumentException("Only millisecond precision supported: nanos: " + nanos);
					return gcal;
				}
			}
		);
	}

	/**
	 * Gets the date and time from the "YYYY-MM-DD[ HH:MM[:SS[.mmm]]]" format in the default time zone or {@code null} if the parameter is {@code null}.
	 * Allows negative years like "-344-01-23".
	 * Allows shorter months, days, hours, minutes, and millis like "1976-1-9 1:2:3.1".
	 */
	public static GregorianCalendar parseDateTime(String dateTime) throws IllegalArgumentException {
		return parseDateTime(dateTime, null);
	}

	/**
	 * Gets today's date in the given time zone.  Hour, minute, second, and millisecond are all set to zero.
	 *
	 * @param timeZone  The time zone to use or {@code null} to use the default time zone
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
	 */
	public static GregorianCalendar getToday() {
		return getToday(null);
	}
}
