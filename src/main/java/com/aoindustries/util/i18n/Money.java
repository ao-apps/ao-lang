/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2009, 2010, 2011, 2016, 2017, 2019  AO Industries, Inc.
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
package com.aoindustries.util.i18n;

import com.aoindustries.io.FastExternalizable;
import com.aoindustries.io.FastObjectInput;
import com.aoindustries.io.FastObjectOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputValidation;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Stores a monetary value as a combination of currency and amount.  It supports
 * many of the basic operators from <code>BigDecimal</code>, and more will be added
 * as needed.  An <code>ArithmeticException</code> on any attempt to perform operations
 * on monetary values of different currencies.
 *
 * @author  AO Industries, Inc.
 */
final public class Money implements FastExternalizable, ObjectInputValidation, Comparable<Money> {

	private Currency currency;
	private long value;
	private int scale;

	/**
	 * Will change the scale of the value to match the currency, but will not round.
	 * @throws NumberFormatException if unable to scale the value.
	 */
	public Money(Currency currency, BigDecimal value) throws NumberFormatException {
		this.currency = currency;
		try {
			int currencyScale = currency.getDefaultFractionDigits();
			if(currencyScale != -1) value = value.setScale(currencyScale);
			this.scale = value.scale();
			this.value = value.movePointRight(value.scale()).longValueExact();
		} catch(ArithmeticException err) {
			NumberFormatException newErr = new NumberFormatException(err.getMessage());
			newErr.initCause(err);
			throw newErr;
		}
		validate();
	}

	public Money(Currency currency, long value, int scale) throws NumberFormatException {
		this.currency = currency;
		try {
			int currencyScale = currency.getDefaultFractionDigits();
			if(currencyScale != -1 && currencyScale != scale) {
				value = value == 0 ? 0 : BigDecimal.valueOf(value, scale).setScale(currencyScale).movePointRight(currencyScale).longValueExact();
				scale = currencyScale;
			}
			this.value = value;
			this.scale = scale;
		} catch(ArithmeticException err) {
			NumberFormatException newErr = new NumberFormatException(err.getMessage());
			newErr.initCause(err);
			throw newErr;
		}
		validate();
	}

	private void validate() throws NumberFormatException {
		int currencyScale = currency.getDefaultFractionDigits();
		if(currencyScale != -1 && currencyScale != scale) throw new NumberFormatException("currency.scale != value.scale: " + currencyScale + " != " + scale);
	}

	/**
	 * Equal when has same currency, value, and scale.
	 */
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Money)) return false;
		Money other = (Money)o;
		return
			currency == other.currency
			&& value == other.value
			&& scale == other.scale;
	}

	@Override
	public int hashCode() {
		int hash = currency.getCurrencyCode().hashCode();
		hash = hash * 31 + (int)value;
		hash = hash * 31 + scale;
		return hash;
	}

	/**
	 * Sorts by currency code and then value.
	 *
	 * @see  CurrencyComparator
	 */
	@Override
	public int compareTo(Money other) {
		int diff = CurrencyComparator.getInstance().compare(currency, other.currency);
		if(diff != 0) return diff;
		return getValue().compareTo(other.getValue());
	}

	public Currency getCurrency() {
		return currency;
	}

	public BigDecimal getValue() {
		return BigDecimal.valueOf(value, scale);
	}

	/**
	 * Gets the unscaled value of this currency.
	 */
	public long getUnscaledValue() {
		return value;
	}

	/**
	 * Gets the scale of this currency.
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * Displays the monetary value as currency symbol (in Locale-specific display) followed by value, such as $100.00
	 * or $-100.50.
	 */
	@Override
	public String toString() {
		return currency.getSymbol(ThreadLocale.get())+getValue().toPlainString();
	}

	public Money add(Money addend) throws ArithmeticException {
		if(currency != addend.currency) throw new ArithmeticException("currency != addend.currency: "+currency+" != "+addend.currency);
		return new Money(currency, getValue().add(addend.getValue()));
	}

	/**
	 * Multiplies without rounding.
	 */
	public Money multiply(BigDecimal multiplicand) throws ArithmeticException {
		return multiply(multiplicand, RoundingMode.UNNECESSARY);
	}

	/**
	 * Multiplies with rounding.
	 */
	public Money multiply(BigDecimal multiplicand, RoundingMode roundingMode) throws ArithmeticException {
		int currencyScale = currency.getDefaultFractionDigits();
		if(currencyScale == -1) currencyScale = scale; // Use same scale if currency doesn't dictate
		return new Money(currency, getValue().multiply(multiplicand).setScale(currencyScale, roundingMode));
	}

	/**
	 * Returns a monetary amount that is the negative of this amount.
	 */
	public Money negate() {
		return new Money(currency, getValue().negate());
	}

	// <editor-fold defaultstate="collapsed" desc="FastExternalizable">
	private static final long serialVersionUID = 2287045704444180509L;

	public Money() {
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		FastObjectOutput fastOut = FastObjectOutput.wrap(out);
		try {
			fastOut.writeFastUTF(currency.getCurrencyCode());
			fastOut.writeLong(value);
			fastOut.writeInt(scale);
		} finally {
			fastOut.unwrap();
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		if(currency!=null) throw new IllegalStateException();
		FastObjectInput fastIn = FastObjectInput.wrap(in);
		try {
			currency = Currency.getInstance(fastIn.readFastUTF());
			value = fastIn.readLong();
			scale = fastIn.readInt();
		} finally {
			fastIn.unwrap();
		}
	}

	@Override
	public void validateObject() throws InvalidObjectException {
		try {
			validate();
		} catch(NumberFormatException err) {
			InvalidObjectException newErr = new InvalidObjectException(err.getMessage());
			newErr.initCause(err);
			throw newErr;
		}
	}
	// </editor-fold>
}
