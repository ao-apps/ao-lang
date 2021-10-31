/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2018, 2019, 2020, 2021  AO Industries, Inc.
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

import com.aoapps.lang.io.Encoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * @author  AO Industries, Inc.
 */
public final class Strings {

	/**
	 * Make no instances.
	 */
	private Strings() {
	}

	private static final String[] MONTHS = {
		"Jan",
		"Feb",
		"Mar",
		"Apr",
		"May",
		"Jun",
		"Jul",
		"Aug",
		"Sep",
		"Oct",
		"Nov",
		"Dec"
	};

	/**
	 * @deprecated  This method is not locale-aware, is no longer used, and will be removed.
	 */
	@Deprecated
	public static String getMonth(int month) {
		return MONTHS[month];
	}

	// TODO: This list is probably not very complete from a Unicode perspective.
	// TDOO: What does the Character class offer?
	private static final char[] wordWrapChars = { ' ', '\t', '-', '=', ',', ';' };

	private static final String lineSeparator = System.lineSeparator();

	/**
	 * Joins the string representation of objects on the provided delimiter.
	 * The iteration will be performed twice.  Once to compute the total length
	 * of the resulting string, and the second to build the result.
	 *
	 * @throws ConcurrentModificationException if iteration is not consistent between passes
	 *
	 * @see  #join(java.lang.Iterable, java.lang.String, java.lang.Appendable)
	 * @see  #join(java.lang.Object[], java.lang.String)
	 */
	public static String join(Iterable<?> objects, String delimiter) throws ConcurrentModificationException {
		int delimiterLength = delimiter.length();
		// Find total length
		int totalLength = 0;
		boolean didOne = false;
		for(Object obj : objects) {
			if(didOne) totalLength += delimiterLength;
			else didOne = true;
			totalLength += String.valueOf(obj).length();
		}
		// Build result
		StringBuilder sb = new StringBuilder(totalLength);
		didOne = false;
		for(Object obj : objects) {
			if(didOne) sb.append(delimiter);
			else didOne = true;
			sb.append(obj);
		}
		if(totalLength!=sb.length()) throw new ConcurrentModificationException();
		return sb.toString();
	}

	/**
	 * Joins the string representation of objects on the provided delimiter.
	 *
	 * @see  #join(java.lang.Iterable, java.lang.String)
	 * @see  #join(java.lang.Object[], java.lang.String, java.lang.Appendable)
	 */
	public static <A extends Appendable> A join(Iterable<?> objects, String delimiter, A out) throws IOException {
		boolean didOne = false;
		for(Object obj : objects) {
			if(didOne) out.append(delimiter);
			else didOne = true;
			out.append(String.valueOf(obj));
		}
		return out;
	}

	/**
	 * Joins the string representation of objects on the provided delimiter.
	 * The iteration will be performed twice.  Once to compute the total length
	 * of the resulting string, and the second to build the result.
	 *
	 * @throws ConcurrentModificationException if iteration is not consistent between passes
	 *
	 * @see  #join(java.lang.Object[], java.lang.String, java.lang.Appendable)
	 * @see  #join(java.lang.Iterable, java.lang.String)
	 */
	public static String join(Object[] objects, String delimiter) throws ConcurrentModificationException {
		int delimiterLength = delimiter.length();
		// Find total length
		int totalLength = 0;
		boolean didOne = false;
		for(Object obj : objects) {
			if(didOne) totalLength += delimiterLength;
			else didOne = true;
			totalLength += String.valueOf(obj).length();
		}
		// Build result
		StringBuilder sb = new StringBuilder(totalLength);
		didOne = false;
		for(Object obj : objects) {
			if(didOne) sb.append(delimiter);
			else didOne = true;
			sb.append(obj);
		}
		if(totalLength!=sb.length()) throw new ConcurrentModificationException();
		return sb.toString();
	}

	/**
	 * Joins the string representation of objects on the provided delimiter.
	 *
	 * @see  #join(java.lang.Object[], java.lang.String)
	 * @see  #join(java.lang.Iterable, java.lang.String, java.lang.Appendable)
	 */
	public static <A extends Appendable> A join(Object[] objects, String delimiter, A out) throws IOException {
		boolean didOne = false;
		for(Object obj : objects) {
			if(didOne) out.append(delimiter);
			else didOne = true;
			out.append(String.valueOf(obj));
		}
		return out;
	}

	public static boolean containsIgnoreCase(String line, String word) {
		int word_len=word.length();
		int line_len=line.length();
		int end_pos=line_len-word_len;
		Loop:
		for(int c=0;c<=end_pos;c++) {
			for(int d=0;d<word_len;d++) {
				char ch1=line.charAt(c+d);
				char ch2=word.charAt(d);
				if(ch1>='A'&&ch1<='Z') ch1+='a'-'A';
				if(ch2>='A'&&ch2<='Z') ch2+='a'-'A';
				if(ch1!=ch2) continue Loop;
			}
			return true;
		}
		return false;
	}

	/**
	 * Counts how many times a word appears in a line.  Case insensitive matching.
	 */
	@SuppressWarnings("AssignmentToForLoopParameter")
	public static int countOccurrences(byte[] buff, int len, String word) {
		int wordlen=word.length();
		int end=len-wordlen;
		int count=0;
		Loop:
		for(int c=0;c<=end;c++) {
			for(int d=0;d<wordlen;d++) {
				char ch1=(char)buff[c+d];
				if(ch1<='Z' && ch1>='A') ch1+='a'-'A';
				char ch2=word.charAt(d);
				if(ch2<='Z' && ch2>='A') ch2+='a'-'A';
				if(ch1!=ch2) continue Loop;
			}
			c += (wordlen - 1);
			count++;
		}
		return count;
	}

	/**
	 * Counts how many times a word appears in a line.  Case insensitive matching.
	 */
	@SuppressWarnings("AssignmentToForLoopParameter")
	public static int countOccurrences(byte[] buff, String word) {
		int wordlen=word.length();
		int end=buff.length-wordlen;
		int count=0;
		Loop:
		for(int c=0;c<=end;c++) {
			for(int d=0;d<wordlen;d++) {
				char ch1=(char)buff[c+d];
				if(ch1<='Z' && ch1>='A') ch1+='a'-'A';
				char ch2=word.charAt(d);
				if(ch2<='Z' && ch2>='A') ch2+='a'-'A';
				if(ch1!=ch2) continue Loop;
			}
			c += (wordlen - 1);
			count++;
		}
		return count;
	}

	/**
	 * Counts how many times a word appears in a line.  Case insensitive matching.
	 */
	@SuppressWarnings("AssignmentToForLoopParameter")
	public static int countOccurrences(String line, String word) {
		int wordlen=word.length();
		int end=line.length()-wordlen;
		int count=0;
		Loop:
		for(int c=0;c<=end;c++) {
			for(int d=0;d<wordlen;d++) {
				char ch1=line.charAt(c+d);
				if(ch1<='Z' && ch1>='A') ch1+='a'-'A';
				char ch2=word.charAt(d);
				if(ch2<='Z' && ch2>='A') ch2+='a'-'A';
				if(ch1!=ch2) continue Loop;
			}
			c += (wordlen - 1);
			count++;
		}
		return count;
	}

	public static String getTimeLengthString(long time) {
		StringBuilder sb = new StringBuilder();
		if(time < 0) {
			sb.append('-');
			time = -time;
		}

		long days = time / 86400000;
		time -= days * 86400000;
		int hours = (int)(time / 3600000);
		time -= hours * 3600000;
		int minutes = (int)(time / 60000);
		time -= minutes * 60000;
		int seconds = (int)(time / 1000);
		time -= seconds * 1000;
		if(days == 0) {
			if(hours == 0) {
				if(minutes == 0) {
					if(seconds == 0) {
						if(time == 0) sb.append("0 minutes");
						else sb.append(time).append(time == 1 ? " millisecond" : " milliseconds");
					} else sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
				} else sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
			} else {
				if(minutes == 0) sb.append(hours).append(hours == 1 ? " hour" : " hours");
				else sb.append(hours).append(hours == 1 ? " hour and " : " hours and ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
			}
		} else {
			if(hours == 0) {
				if(minutes == 0) sb.append(days).append(days == 1 ? " day" : " days");
				else sb.append(days).append(days == 1 ? " day and " : " days and ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
			} else {
				if(minutes == 0) sb.append(days).append(days == 1 ? " day and " : " days and ").append(hours).append(hours == 1 ? " hour" : " hours");
				else sb.append(days).append(days == 1 ? " day, " : " days, ").append(hours).append(hours == 1 ? " hour and " : " hours and ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
			}
		}
		return sb.toString();
	}

	public static String getDecimalTimeLengthString(long time) {
		return getDecimalTimeLengthString(time, true);
	}

	public static String getDecimalTimeLengthString(long time, boolean alwaysShowMillis) {
		StringBuilder sb = new StringBuilder();
		if(time < 0) {
			sb.append('-');
			time = -time;
		}

		long days = time / 86400000;
		time -= days * 86400000;
		int hours = (int)(time / 3600000);
		time -= hours * 3600000;
		int minutes = (int)(time / 60000);
		time -= minutes * 60000;
		int seconds = (int)(time / 1000);
		time -= seconds * 1000;

		if(days > 0) sb.append(days).append(days == 1 ? " day, " : " days, ");
		sb.append(hours).append(':');
		if(minutes < 10) sb.append('0');
		sb.append(minutes).append(':');
		if(seconds < 10) sb.append('0');
		sb.append(seconds);
		if(alwaysShowMillis || time != 0) {
			sb.append('.');
			if(time < 10) sb.append("00");
			else if(time < 100) sb.append('0');
			sb.append(time);
		}
		return sb.toString();
	}

	/**
	 * Finds the first occurrence of any of the supplied characters
	 *
	 * @param  s  the <code>String</code> to search
	 * @param  chars  the characters to look for
	 *
	 * @return  the index of the first occurrence of <code>-1</code> if none found
	 */
	public static int indexOf(String s, char[] chars) {
		return indexOf(s, chars, 0);
	}

	/**
	 * Finds the first occurrence of any of the supplied characters starting at the specified index.
	 *
	 * @param  s  the <code>String</code> to search
	 * @param  chars  the characters to look for
	 * @param  start  the starting index.
	 *
	 * @return  the index of the first occurrence of <code>-1</code> if none found
	 */
	public static int indexOf(String s, char[] chars, int start) {
		int sLen = s.length();
		int cLen = chars.length;
		for(int c = start; c < sLen; c++) {
			char ch = s.charAt(c);
			for(int d = 0; d < cLen; d++) {
				if(ch == chars[d]) return c;
			}
		}
		return -1;
	}

	/**
	 * Finds the first occurrence of any of the supplied characters
	 *
	 * @param  s  the <code>String</code> to search
	 * @param  chars  the characters to look for
	 *
	 * @return  the index of the first occurrence of <code>-1</code> if none found
	 */
	public static int indexOf(String s, BitSet chars) {
		return indexOf(s, chars, 0);
	}

	/**
	 * Finds the first occurrence of any of the supplied characters starting at the specified index.
	 *
	 * @param  s  the <code>String</code> to search
	 * @param  chars  the characters to look for
	 * @param  start  the starting index.
	 *
	 * @return  the index of the first occurrence of <code>-1</code> if none found
	 */
	public static int indexOf(String s, BitSet chars, int start) {
		for(int c = start, len = s.length(); c < len; c++) {
			if(chars.get(s.charAt(c))) return c;
		}
		return -1;
	}

	/**
	 * Replaces all occurrences of a character with a String.
	 * Please consider the variant with the {@link Appendable} for higher performance.
	 */
	public static String replace(String string, char ch, String replacement) {
		int pos = string.indexOf(ch);
		if (pos == -1) return string;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 16);
		int lastpos = 0;
		do {
			sb.append(string, lastpos, pos).append(replacement);
			lastpos = pos + 1;
			pos = string.indexOf(ch, lastpos);
		} while (pos != -1);
		if(lastpos<len) sb.append(string, lastpos, len);
		return sb.toString();
	}

	/**
	 * Replaces all occurrences of a String with a String.
	 * Please consider the variant with the {@link Appendable} for higher performance.
	 */
	public static String replace(String string, String find, String replacement) {
		int pos = string.indexOf(find);
		if (pos == -1) return string;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 16);
		int lastpos = 0;
		int findLen = find.length();
		do {
			sb.append(string, lastpos, pos).append(replacement);
			lastpos = pos + findLen;
			pos = string.indexOf(find, lastpos);
		} while (pos != -1);
		if(lastpos<len) sb.append(string, lastpos, len);
		return sb.toString();
	}

	/**
	 * Replaces all occurrences of a character with a String, appends the replacement
	 * to <code>out</code>.
	 */
	public static void replace(String string, char find, String replacement, Appendable out) throws IOException {
		int pos = string.indexOf(find);
		if (pos == -1) {
			out.append(string);
		} else {
			int lastpos = 0;
			do {
				out.append(string, lastpos, pos).append(replacement);
				lastpos = pos + 1;
				pos = string.indexOf(find, lastpos);
			} while (pos != -1);
			int len = string.length();
			if(lastpos<len) out.append(string, lastpos, len);
		}
	}

	/**
	 * Replaces all occurrences of a String with a String, appends the replacement
	 * to <code>out</code>.
	 */
	public static void replace(String string, String find, String replacement, Appendable out) throws IOException {
		int pos = string.indexOf(find);
		if (pos == -1) {
			out.append(string);
		} else {
			int lastpos = 0;
			int findLen = find.length();
			do {
				out.append(string, lastpos, pos).append(replacement);
				lastpos = pos + findLen;
				pos = string.indexOf(find, lastpos);
			} while (pos != -1);
			int len = string.length();
			if(lastpos<len) out.append(string, lastpos, len);
		}
	}

	/**
	 * Replaces all occurrences of a character with a String, appends the replacement
	 * to <code>out</code>.
	 */
	public static void replace(String string, char find, String replacement, Appendable out, Encoder encoder) throws IOException {
		if(encoder == null) {
			replace(string, find, replacement, out);
		} else {
			int pos = string.indexOf(find);
			if (pos == -1) {
				encoder.append(string, out);
			} else {
				int lastpos = 0;
				do {
					encoder.append(string, lastpos, pos, out).append(replacement, out);
					lastpos = pos + 1;
					pos = string.indexOf(find, lastpos);
				} while (pos != -1);
				int len = string.length();
				if(lastpos<len) encoder.append(string, lastpos, len, out);
			}
		}
	}

	/**
	 * Replaces all occurrences of a String with a String, appends the replacement
	 * to <code>out</code>.
	 */
	public static void replace(String string, String find, String replacement, Appendable out, Encoder encoder) throws IOException {
		if(encoder == null) {
			replace(string, find, replacement, out);
		} else {
			int pos = string.indexOf(find);
			if (pos == -1) {
				encoder.append(string, out);
			} else {
				int lastpos = 0;
				int findLen = find.length();
				do {
					encoder.append(string, lastpos, pos, out).append(replacement, out);
					lastpos = pos + findLen;
					pos = string.indexOf(find, lastpos);
				} while (pos != -1);
				int len = string.length();
				if(lastpos<len) encoder.append(string, lastpos, len, out);
			}
		}
	}

	/**
	 * Replaces all occurrences of a String with a String.
	 */
	public static void replace(StringBuffer sb, String find, String replacement) {
		int pos = 0;
		while(pos<sb.length()) {
			pos = sb.indexOf(find, pos);
			if(pos==-1) break;
			sb.replace(pos, pos+find.length(), replacement);
			pos += replacement.length();
		}
	}

	/**
	 * Replaces all occurrences of a String with a String.
	 */
	public static void replace(StringBuilder sb, String find, String replacement) {
		int pos = 0;
		while(pos<sb.length()) {
			pos = sb.indexOf(find, pos);
			if(pos==-1) break;
			sb.replace(pos, pos+find.length(), replacement);
			pos += replacement.length();
		}
	}

	/**
	 * Splits a String into lines on any '\n' characters.  Also removes any ending '\r' characters if present
	 */
	public static List<String> splitLines(String s) {
		List<String> v = new ArrayList<>();
		int start = 0;
		int pos;
		while((pos = s.indexOf('\n', start)) != -1) {
			String line;
			if(pos > start && s.charAt(pos - 1) == '\r') line = s.substring(start, pos - 1);
			else line = s.substring(start, pos);
			v.add(line);
			start = pos + 1;
		}
		int slen = s.length();
		if(start < slen) {
			// Ignore any trailing '\r'
			if(s.charAt(slen - 1) == '\r') slen--;
			String line = s.substring(start, slen);
			v.add(line);
		}
		return v;
	}

	/**
	 * Splits a <code>String</code> into a <code>String[]</code>.
	 *
	 * @see  #isWhitespace(int)
	 */
	// TODO: Just return List<String> instead or processing in two passes.
	public static String[] split(String line) {
		int len = line.length();
		int wordCount = 0;
		int pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos > start) wordCount++;
		}

		String[] words = new String[wordCount];

		int wordPos = 0;
		pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos>start) words[wordPos++]=line.substring(start, pos);
		}

		return words;
	}

	/**
	 * Splits a <code>String</code> into a <code>String[]</code>.
	 *
	 * @see  #isWhitespace(int)
	 *
	 * @deprecated  It is highly unlikely this method is still used
	 */
	@Deprecated
	public static int split(String line, char[][][] buff) {
		int len = line.length();
		int wordCount = 0;
		int pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos>start) wordCount++;
		}

		char[][] words = buff[0];
		if(words == null || words.length < wordCount) buff[0] = words = new char[wordCount][];

		int wordPos = 0;
		pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos > start) {
				char[] tch = words[wordPos++] = new char[pos - start];
				line.getChars(start, pos, tch, 0);
			}
		}

		return wordCount;
	}

	/**
	 * Splits a <code>String</code> into a <code>String[]</code>.
	 *
	 * @see  #isWhitespace(int)
	 *
	 * @deprecated  It is highly unlikely this method is still used
	 */
	@Deprecated
	public static int split(String line, String[][] buff) {
		int len = line.length();
		int wordCount = 0;
		int pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos > start) wordCount++;
		}

		String[] words = buff[0];
		if(words == null || words.length < wordCount) buff[0] = words = new String[wordCount];

		int wordPos = 0;
		pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& !isWhitespace(cp = line.codePointAt(pos))
			) {
				pos += Character.charCount(cp);
			}
			if(pos > start) words[wordPos++] = line.substring(start, pos);
		}

		return wordCount;
	}

	/**
	 * Splits a string on the given delimiter.
	 * Does include all empty elements on the split.
	 *
	 * @return  the modifiable list from the split
	 */
	// TODO: Deprecate in favor of a codepoint version
	public static List<String> split(String line, char delim) {
		return split(line, 0, line.length(), delim, new ArrayList<>());
	}

	/**
	 * Splits a string on the given delimiter.
	 * Does include all empty elements on the split.
	 *
	 * @param  words  the words will be added to this collection.
	 *
	 * @return  the collection provided in words parameter
	 */
	// TODO: Deprecate in favor of a codepoint version
	public static <C extends Collection<String>> C split(String line, char delim, C words) {
		return split(line, 0, line.length(), delim, words);
	}

	/**
	 * Splits a string on the given delimiter over the given range.
	 * Does include all empty elements on the split.
	 *
	 * @return  the modifiable list from the split
	 */
	// TODO: Deprecate in favor of a codepoint version
	public static List<String> split(String line, int begin, int end, char delim) {
		return split(line, begin, end, delim, new ArrayList<>());
	}

	/**
	 * Splits a string on the given delimiter over the given range.
	 * Does include all empty elements on the split.
	 *
	 * @param  words  the words will be added to this collection.
	 *
	 * @return  the collection provided in words parameter
	 */
	// TODO: Deprecate in favor of a codepoint version
	public static <C extends Collection<String>> C split(String line, int begin, int end, char delim, C words) {
		int pos = begin;
		while (pos < end) {
			int start = pos;
			pos = line.indexOf(delim, pos);
			if(pos == -1 || pos > end) pos = end;
			words.add(line.substring(start, pos));
			pos++;
		}
		// If ending in a delimeter, add the empty string
		if(end>begin && line.charAt(end-1)==delim) words.add("");
		return words;
	}

	public static List<String> split(String line, String delim) {
		int delimLen = delim.length();
		if(delimLen == 0) throw new IllegalArgumentException("Delimiter may not be empty");
		List<String> words = new ArrayList<>();
		int len = line.length();
		int pos = 0;
		while (pos < len) {
			int start = pos;
			pos = line.indexOf(delim, pos);
			if (pos == -1) {
				words.add(line.substring(start, len));
				pos = len;
			} else {
				words.add(line.substring(start, pos));
				pos += delimLen;
			}
		}
		// If ending in a delimeter, add the empty string
		if(len >= delimLen && line.endsWith(delim)) words.add("");

		return words;
	}

	/**
	 * Splits a string into multiple words on either whitespace or commas.
	 *
	 * @return  The list of non-empty strings.
	 *
	 * @see  #isWhitespace(int)
	 */
	public static List<String> splitCommaSpace(String line) {
		List<String> words = new ArrayList<>();
		int len = line.length();
		int pos = 0;
		while(pos < len) {
			// Skip past blank space
			int cp;
			while(
				pos < len
				&& (
					(cp = line.codePointAt(pos)) == ','
					|| isWhitespace(cp)
				)
			) {
				pos += Character.charCount(cp);
			}
			int start = pos;
			// Skip to the next blank space
			while(
				pos < len
				&& (cp = line.codePointAt(pos)) != ','
				&& !isWhitespace(cp)
			) {
				pos += Character.charCount(cp);
			}
			if(pos > start) words.add(line.substring(start, pos));
		}
		return words;
	}

	/**
	 * Word wraps a <code>String</code> to be no longer than the provided number of characters wide.
	 *
	 * @deprecated  Use new version with Appendable for higher performance
	 */
	@Deprecated
	public static String wordWrap(String string, int width) {
		// Leave room for two word wrap characters every width / 2 characters, on average.
		int inputLength = string.length();
		int estimatedLines = 2 * inputLength / width;
		int initialLength = inputLength + estimatedLines * 2;
		try {
			StringBuilder buffer = new StringBuilder(initialLength);
			wordWrap(string, width, buffer);
			return buffer.toString();
		} catch(IOException e) {
			throw new AssertionError("Should not get IOException from StringBuilder", e);
		}
	}

	/**
	 * Word wraps a <code>String</code> to be no longer than the provided number of characters wide.
	 *
	 * TODO: Make this more efficient by eliminating the internal use of substring.
	 */
	public static void wordWrap(String string, int width, Appendable out) throws IOException {
		width++;
		boolean useCR = false;
		do {
			int pos = string.indexOf('\n');
			if (!useCR && pos > 0 && string.charAt(pos - 1) == '\r') useCR = true;
			int linelength = pos == -1 ? string.length() : pos + 1;
			if ((pos==-1?linelength-1:pos) <= width) {
				// No wrap required
				out.append(string, 0, linelength);
				string = string.substring(linelength);
			} else {
				// Word wrap required

				// Search for the beginning of the first word that is past the <code>width</code> column
				// The wrap character must be on the same line as the outputted line.
				int lastBreakChar = 0;

				for (int c = 0; c < width; c++) {
					// Check to see if it is a break character
					char ch = string.charAt(c);
					boolean isBreak = false;
					for (int d = 0; d < wordWrapChars.length; d++) {
						if (ch == wordWrapChars[d]) {
							isBreak = true;
							break;
						}
					}
					if (isBreak) lastBreakChar = c + 1;
				}

				// If no break has been found, keep searching until a break is found
				if (lastBreakChar == 0) {
					for (int c = width; c < linelength; c++) {
						char ch = string.charAt(c);
						boolean isBreak = false;
						for (int d = 0; d < wordWrapChars.length; d++) {
							if (ch == wordWrapChars[d]) {
								isBreak = true;
								break;
							}
						}
						if (isBreak) {
							lastBreakChar = c + 1;
							break;
						}
					}
				}

				if (lastBreakChar == 0) {
					// Take the whole line
					out.append(string, 0, linelength);
					string = string.substring(linelength);
				} else {
					// Break out the section
					out.append(string, 0, lastBreakChar);
					if(useCR) out.append("\r\n");
					else out.append('\n');
					string = string.substring(lastBreakChar);
				}
			}
		} while (string.length() > 0);
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * Gets the hexadecimal character for the low-order four bits of the provided int.
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static char getHexChar(int v) {
		return hexChars[v & 0xf];
	}

	/**
	 * Converts one hex digit to an integer
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static int getHex(char ch) throws IllegalArgumentException {
		switch(ch) {
			case '0': return 0x00;
			case '1': return 0x01;
			case '2': return 0x02;
			case '3': return 0x03;
			case '4': return 0x04;
			case '5': return 0x05;
			case '6': return 0x06;
			case '7': return 0x07;
			case '8': return 0x08;
			case '9': return 0x09;
			case 'a': case 'A': return 0x0a;
			case 'b': case 'B': return 0x0b;
			case 'c': case 'C': return 0x0c;
			case 'd': case 'D': return 0x0d;
			case 'e': case 'E': return 0x0e;
			case 'f': case 'F': return 0x0f;
			default: throw new IllegalArgumentException("Invalid hex character: "+ch);
		}
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static void convertToHex(byte[] bytes, Appendable out) throws IOException {
		if(bytes != null) {
			int len = bytes.length;
			for(int c = 0; c < len; c++) {
				int b = bytes[c];
				out.append(getHexChar(b >> 4));
				out.append(getHexChar(b));
			}
		}
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static String convertToHex(byte[] bytes) {
		if(bytes == null) return null;
		int len = bytes.length;
		StringBuilder sb = new StringBuilder(len * 2);
		try {
			convertToHex(bytes, sb);
		} catch(IOException e) {
			throw new AssertionError(e);
		}
		return sb.toString();
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static byte[] convertByteArrayFromHex(char[] hex) {
		int hexLen = hex.length;
		if((hexLen&1) != 0) throw new IllegalArgumentException("Uneven number of characters: " + hexLen);
		byte[] result = new byte[hexLen / 2];
		int resultPos = 0;
		int hexPos = 0;
		while(hexPos < hexLen) {
			int h = getHex(hex[hexPos++]);
			int l = getHex(hex[hexPos++]);
			result[resultPos++] = (byte)(
				(h<<4) | l
			);
		}
		return result;
	}

	/**
	 * Converts an int to a full 8-character hex code.
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static void convertToHex(int value, Appendable out) throws IOException {
		out.append(getHexChar(value >>> 28));
		out.append(getHexChar(value >>> 24));
		out.append(getHexChar(value >>> 20));
		out.append(getHexChar(value >>> 16));
		out.append(getHexChar(value >>> 12));
		out.append(getHexChar(value >>> 8));
		out.append(getHexChar(value >>> 4));
		out.append(getHexChar(value));
	}

	/**
	 * Converts an int to a full 8-character hex code.
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static String convertToHex(int value) {
		StringBuilder sb = new StringBuilder(8);
		try {
			convertToHex(value, sb);
		} catch(IOException e) {
			throw new AssertionError(e);
		}
		return sb.toString();
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static int convertIntArrayFromHex(char[] hex) {
		int hexLen = hex.length;
		if(hexLen < 8) throw new IllegalArgumentException("Too few characters: " + hexLen);
		return
			(getHex(hex[0]) << 28)
			| (getHex(hex[1]) << 24)
			| (getHex(hex[2]) << 20)
			| (getHex(hex[3]) << 16)
			| (getHex(hex[4]) << 12)
			| (getHex(hex[5]) << 8)
			| (getHex(hex[6]) << 4)
			| (getHex(hex[7]))
		;
	}

	/**
	 * Converts a long integer to a full 16-character hex code.
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static void convertToHex(long value, Appendable out) throws IOException {
		convertToHex((int)(value >>> 32), out);
		convertToHex((int)value, out);
	}

	/**
	 * Converts a long integer to a full 16-character hex code.
	 *
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static String convertToHex(long value) {
		StringBuilder sb = new StringBuilder(16);
		try {
			convertToHex(value, sb);
		} catch(IOException e) {
			throw new AssertionError(e);
		}
		return sb.toString();
	}

	/**
	 * @deprecated  Please use {@link org.apache.commons.codec.binary.Hex}
	 */
	@Deprecated
	public static long convertLongArrayFromHex(char[] hex) {
		int hexLen = hex.length;
		if(hexLen < 16) throw new IllegalArgumentException("Too few characters: " + hexLen);
		int h = (getHex(hex[0]) << 28)
			| (getHex(hex[1]) << 24)
			| (getHex(hex[2]) << 20)
			| (getHex(hex[3]) << 16)
			| (getHex(hex[4]) << 12)
			| (getHex(hex[5]) << 8)
			| (getHex(hex[6]) << 4)
			| (getHex(hex[7]))
		;
		int l = (getHex(hex[8]) << 28)
			| (getHex(hex[9]) << 24)
			| (getHex(hex[10]) << 20)
			| (getHex(hex[11]) << 16)
			| (getHex(hex[12]) << 12)
			| (getHex(hex[13]) << 8)
			| (getHex(hex[14]) << 4)
			| (getHex(hex[15]))
		;
		return (((long)h) << 32) | (l & 0xffffffffL);
	}

	/**
	 * Gets the approximate size (where k=1024) of a file in this format:
	 *
	 * x byte(s)
	 * xx bytes
	 * xxx bytes
	 * x.x k
	 * xx.x k
	 * xxx k
	 * x.x M
	 * xx.x M
	 * xxx M
	 * x.x G
	 * xx.x G
	 * xxx G
	 * x.x T
	 * xx.x T
	 * xxx T
	 * xxx... T
	 */
	public static String getApproximateSize(long size) {
		boolean neg = size < 0;
		if(neg) {
			size = (size == Long.MIN_VALUE) ? Long.MAX_VALUE : -size;
		}
		if(size == 1) return "1 byte";
		if(size < 1024) return new StringBuilder().append((int)size).append(" bytes").toString();
		String unitName;
		long unitSize;
		if(size < (1024L * 1024)) {
			unitName = " k";
			unitSize = 1024;
		} else if(size < (1024L * 1024 * 1024)) {
			unitName = " M";
			unitSize = 1024L * 1024;
		} else if(size < (1024L * 1024 * 1024 * 1024)) {
			unitName = " G";
			unitSize = 1024L * 1024 * 1024;
		} else {
			unitName = " T";
			unitSize = 1024L * 1024 * 1024 * 1024;
		}
		StringBuilder sb = new StringBuilder();
		if(neg) sb.append('-');
		long whole = size / unitSize;
		if(whole < 100) {
			int fraction = (int)(((size % unitSize) * 10) / unitSize);
			return sb.append(whole).append('.').append(fraction).append(unitName).toString();
		} else {
			return sb.append(whole).append(unitName).toString();
		}
	}

	/**
	 * Gets the approximate bit rate (where k=1000) in this format:
	 *
	 * x
	 * xx
	 * xxx
	 * x.x k
	 * xx.x k
	 * xxx k
	 * x.x M
	 * xx.x M
	 * xxx M
	 * x.x G
	 * xx.x G
	 * xxx G
	 * x.x T
	 * xx.x T
	 * xxx T
	 * xxx... T
	 */
	public static String getApproximateBitRate(long bitRate) {
		boolean neg = bitRate < 0;
		if(neg) {
			bitRate = (bitRate == Long.MIN_VALUE) ? Long.MAX_VALUE : -bitRate;
		}
		if(bitRate < 1000) return Integer.toString((int)bitRate);
		String unitName;
		long unitSize;
		if(bitRate < (1000_000)) {
			unitName = " k";
			unitSize = 1000;
		} else if(bitRate < 1000_000_000) {
			unitName = " M";
			unitSize = 1000_000;
		} else if(bitRate < 1000_000_000_000L) {
			unitName = " G";
			unitSize = 1000_000_000;
		} else {
			unitName = " T";
			unitSize = 1000_000_000_000L;
		}
		StringBuilder sb = new StringBuilder();
		if(neg) sb.append('-');
		long whole = bitRate / unitSize;
		if(whole < 100) {
			int fraction = (int)(((bitRate % unitSize) * 10) / unitSize);
			return sb.append(whole).append('.').append(fraction).append(unitName).toString();
		} else {
			return sb.append(whole).append(unitName).toString();
		}
	}

	/**
	 * Compares two strings in a case insensitive manner.  However, if they are considered equals in the
	 * case-insensitive manner, the case sensitive comparison is done.
	 */
	public static int compareToIgnoreCaseCarefulEquals(String s1, String s2) {
		int diff = s1.compareToIgnoreCase(s2);
		if(diff == 0) diff = s1.compareTo(s2);
		return diff;
	}

	/**
	 * Finds the next of a substring like regular String.indexOf, but stops at a certain maximum index.
	 * Like substring, will look up to the character one before toIndex.
	 */
	@SuppressWarnings("AssignmentToForLoopParameter")
	public static int indexOf(String source, String target, int fromIndex, int toIndex) {
		if(fromIndex>toIndex) throw new IllegalArgumentException("fromIndex>toIndex: fromIndex="+fromIndex+", toIndex="+toIndex);

		int sourceCount = source.length();

		// This line makes it different than regular String indexOf method.
		if(toIndex<sourceCount) sourceCount = toIndex;

		int targetCount = target.length();

		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char first  = target.charAt(0);
		int max = sourceCount - targetCount;

		for (int i = fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (source.charAt(i) != first) {
				while (++i <= max && source.charAt(i) != first) {
					// Intentionally empty
				}
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = 1; j < end && source.charAt(j) == target.charAt(k); j++, k++) {
					// Intentionally empty
				}

				if (j == end) {
					/* Found whole string. */
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the first line only, and only up to the maximum number of characters.  If the
	 * value is modified, will append a horizontal ellipsis (Unicode 0x2026).
	 */
	public static String firstLineOnly(String value, int maxCharacters) {
		if(value==null) return value;
		int pos = value.indexOf(lineSeparator);
		if(pos==-1) pos = value.length();
		if(pos>maxCharacters) pos = maxCharacters;
		return pos==value.length() ? value : (value.substring(0, pos) + '\u2026');
	}

	/**
	 * Returns null if the string is null or empty.
	 */
	public static String nullIfEmpty(String value) {
		return value==null || value.isEmpty() ? null : value;
	}

	/**
	 * Determines if a character is whitespace.
	 * A character is considered whitespace if it is either <code>&lt;= '\u0020'</code>
	 * (for compatibility with {@link String#trim()})
	 * or matches {@link Character#isWhitespace(char)}.
	 */
	public static boolean isWhitespace(char ch) {
		return ch <= ' ' || Character.isWhitespace(ch);
	}

	/**
	 * Determines if a code point is whitespace.
	 * A code point is considered whitespace if it is either <code>&lt;= '\u0020'</code>
	 * (for compatibility with {@link String#trim()})
	 * or matches {@link Character#isWhitespace(int)}.
	 */
	public static boolean isWhitespace(int codePoint) {
		return codePoint <= ' ' || Character.isWhitespace(codePoint);
	}

	/**
	 * Trims a value, as per rules of {@link #isWhitespace(int)}.
	 *
	 * @return  The value trimmed or {@code null} when was {@code null}
	 *
	 * @see String#substring(int, int)
	 */
	public static String trim(String value) {
		if(value == null) return null;
		final int valueLen = value.length();
		int len = valueLen;
		int st = 0;

		int cp;
		while ((st < len) && isWhitespace(cp = value.codePointAt(st))) {
			st += Character.charCount(cp);
		}
		while ((st < len) && isWhitespace(cp = value.codePointBefore(len))) {
			len -= Character.charCount(cp);
			if(len < st) len = st; // Just in case strangely overlapping invalid code points
		}
		assert st  >= 0;
		assert len <= valueLen;
		assert st  <= len;
		return
			  (st == 0 && len == valueLen) ? value // Unchanged
			: (st == len) ? ""                     // Now empty
			: value.substring(st, len);            // Trimmed
	}

	/**
	 * Trims a value, as per rules of {@link #isWhitespace(int)}.
	 *
	 * @return  The value trimmed or {@code null} when was {@code null}
	 *
	 * @see CharSequence#subSequence(int, int)
	 */
	public static CharSequence trim(CharSequence value) {
		if(value == null) return null;
		final int valueLen = value.length();
		int len = valueLen;
		int st = 0;

		int cp;
		while ((st < len) && isWhitespace(cp = Character.codePointAt(value, st))) {
			st += Character.charCount(cp);
		}
		while ((st < len) && isWhitespace(cp = Character.codePointBefore(value, len))) {
			len -= Character.charCount(cp);
			if(len < st) len = st; // Just in case strangely overlapping invalid code points
		}
		assert st  >= 0;
		assert len <= valueLen;
		assert st  <= len;
		return
			  (st == 0 && len == valueLen) ? value // Unchanged
			: value.subSequence(st, len);          // Trimmed
	}

	/**
	 * Trims a value, as per rules of {@link #isWhitespace(int)}, returning {@code null} if empty after trimming.
	 */
	public static String trimNullIfEmpty(String value) {
		if(value != null) {
			value = trim(value);
			if(value.isEmpty()) value = null;
		}
		return value;
	}

	/**
	 * Trims a value, as per rules of {@link #isWhitespace(int)}, returning {@code null} if empty after trimming.
	 */
	public static CharSequence trimNullIfEmpty(CharSequence value) {
		if(value != null) {
			value = trim(value);
			if(value.length() == 0) value = null;
		}
		return value;
	}
}
