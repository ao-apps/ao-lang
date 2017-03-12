/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017  AO Industries, Inc.
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
package com.aoindustries.text;

import com.aoindustries.lang.ObjectUtils;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Compares strings in a case-insensitive, locale-aware manner.  Also supports
 * integer ranges within the string so would be sorted in the following order:
 * <ol>
 *   <li>1A</li>
 *   <li>2A</li>
 *   <li>10A</li>
 *   <li>11A</li>
 *   <li>11a</li>
 *   <li>12</li>
 *   <li>B</li>
 * </ol>
 *
 * @author  AO Industries, Inc.
 */
public class SmartComparator implements Comparator<Object> {

	private final Collator collator;

	public SmartComparator(Collator collator) {
		this.collator = collator;
	}

	public SmartComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	public SmartComparator() {
		this(Collator.getInstance());
	}

	enum TokenType {
		EMPTY,
		NUMERIC,
		STRING
	}

	static class Token {
		final TokenType tokenType;
		final String value;
		final int begin;
		final int end;

		Token(TokenType tokenType, String value, int begin, int end) {
			this.tokenType = tokenType;
			this.value = value;
			this.begin = begin;
			this.end = end;
		}
		/*
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Token)) return false;
			Token other = (Token)obj;
			return
				tokenType == other.tokenType
				&& begin == other.begin
				&& end == other.end
				&& value.equals(other.value)
			;
		}*/
	}

	static final Token nextToken(String value, int pos) {
		int len = value.length();
		if(pos > len) throw new IllegalArgumentException();
		// At end
		if(pos == len) return new Token(TokenType.EMPTY, value, pos, len);
		for(int i=pos; i<len; i++) {
			// Check if is a number at the current index
			boolean isNumeric;
			boolean dotUsed;
			int prefixLen;
			{
				char ch1 = value.charAt(i);
				char ch2;
				// #
				if(
					ch1 >= '0'
					&& ch1 <= '9'
				) {
					isNumeric = true;
					dotUsed = false;
					prefixLen = 1;
				}
				// .#
				else if(
					ch1=='.'
					&& i < (len-1)
					&& (ch2 = value.charAt(i+1)) >= '0'
					&& ch2 <= '9'
				) {
					isNumeric = true;
					dotUsed = true;
					prefixLen = 2;
				}
				// -#
				else if(
					ch1=='-'
					&& i < (len-1)
					&& (ch2 = value.charAt(i+1)) >= '0'
					&& ch2 <= '9'
				) {
					isNumeric = true;
					dotUsed = false;
					prefixLen = 2;
				}
				// -.#
				else if(
					ch1=='-'
					&& i < (len-2)
					&& value.charAt(i+1) == '.'
					&& (ch2 = value.charAt(i+2)) >= '0'
					&& ch2 <= '9'
				) {
					isNumeric = true;
					dotUsed = true;
					prefixLen = 3;
				} else {
					isNumeric = false;
					dotUsed = false;
					prefixLen = 0;
				}
			}

			if(isNumeric) {
				if(i==pos) {
					// Starts with numeric, find end of numeric range
					for(i += prefixLen; i<len; i++) {
						char ch = value.charAt(i);
						if(ch=='.') {
							if(dotUsed) break;
							dotUsed = true;
						} else if(ch<'0' || ch>'9') {
							break;
						}
					}
					return new Token(TokenType.NUMERIC, value, pos, i);
				} else {
					// Starts with a string
					return new Token(TokenType.STRING, value, pos, i);
				}
			}
		}
		// No numeric found, remaining is all string
		return new Token(TokenType.STRING, value, pos, len);
	}

	@Override
	public int compare(Object o1, Object o2) {
		return compare(
			ObjectUtils.toString(o1),
			ObjectUtils.toString(o2)
		);
	}

	public int compare(String s1, String s2) {
		// Put all nulls after non-nulls
		if(s1 == null) {
			if(s2 == null) return 0;
			else return 1;
		} else {
			if(s2 == null) return -1;
		}
		// Handle one token at a time
		int len1 = s1.length();
		int len2 = s2.length();
		int pos1 = 0;
		int pos2 = 0;
		while(pos1 < len1 || pos2 < len2) {
			Token t1 = nextToken(s1, pos1);
			Token t2 = nextToken(s2, pos2);
			TokenType type1 = t1.tokenType;
			TokenType type2 = t2.tokenType;
			// Both empty
			if(type1==TokenType.EMPTY && type2==TokenType.EMPTY) {
				return 0;
			} else if(type1 != type2) {
				// Token type mismatch, handle remainder of string using collator
				String remainder1 = s1.substring(t1.begin);
				String remainder2 = s2.substring(t2.begin);
				// Use collator first
				int diff = collator.compare(remainder1, remainder2);
				// Use direct string comparison only when collator considers the two strings equal
				if(diff == 0) diff = remainder1.compareTo(remainder2);
				return diff;
			} else {
				String sub1 = t1.value.substring(t1.begin, t1.end);
				String sub2 = t2.value.substring(t2.begin, t2.end);
				// Both numeric
				if(type1==TokenType.NUMERIC) {
					assert type2==TokenType.NUMERIC;
					BigDecimal bd1 = new BigDecimal(sub1);
					BigDecimal bd2 = new BigDecimal(sub2);
					int diff = bd1.compareTo(bd2);
					if(diff == 0) diff = sub1.compareTo(sub2);
					if(diff != 0) return diff;
				}
				// Both string
				else if(type1==TokenType.STRING) {
					assert type2==TokenType.STRING;
					// Use collator first
					int diff = collator.compare(sub1, sub2);
					// Use direct string comparison only when collator considers the two strings equal
					if(diff == 0) diff = sub1.compareTo(sub2);
					if(diff != 0) return diff;
				} else {
					throw new AssertionError();
				}
				// Off to next token
				pos1 = t1.end;
				pos2 = t2.end;
			}
		}
		// No difference found in all tokens
		return 0;
	}
}
