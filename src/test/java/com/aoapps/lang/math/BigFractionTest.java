/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2010, 2011, 2016, 2017, 2021, 2022  AO Industries, Inc.
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

package com.aoapps.lang.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the filesystem iterator.
 *
 * @author  AO Industries, Inc.
 */
public class BigFractionTest extends TestCase {

  public BigFractionTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(BigFractionTest.class);
    return suite;
  }

  public void testCompareTo() {
    assertTrue(new BigFraction("1/3").compareTo(new BigFraction("2/6")) == 0);
    assertTrue(new BigFraction("-1/3").compareTo(new BigFraction("-2/6")) == 0);
    assertTrue(new BigFraction("1/3").compareTo(new BigFraction("1/4")) > 0);
    assertTrue(new BigFraction("1/4").compareTo(new BigFraction("1/3")) < 0);
  }

  public void testReduce() {
    assertEquals(
        new BigFraction("1/1"),
        new BigFraction("10/10").reduce()
    );
    assertEquals(
        new BigFraction("-1/1"),
        new BigFraction("-10/10").reduce()
    );
    assertEquals(
        new BigFraction("23/17"),
        new BigFraction("5516481/4077399").reduce()
    );
    assertEquals(
        new BigFraction("-23/17"),
        new BigFraction("-5516481/4077399").reduce()
    );
    assertEquals(
        new BigFraction("17/23"),
        new BigFraction("4077399/5516481").reduce()
    );
    assertEquals(
        new BigFraction("-17/23"),
        new BigFraction("-4077399/5516481").reduce()
    );
  }

  public void testAdd() {
    assertEquals(
        new BigFraction("1/1"),
        new BigFraction("1/3").add(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("1/3"),
        new BigFraction("-1/3").add(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("11/12"),
        new BigFraction("1/4").add(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("101/10000"),
        new BigFraction("1/100").add(new BigFraction("1/10000"))
    );
  }

  public void testSubtract() {
    assertEquals(
        new BigFraction("-1/3"),
        new BigFraction("1/3").subtract(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("-1/1"),
        new BigFraction("-1/3").subtract(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("-5/12"),
        new BigFraction("1/4").subtract(new BigFraction("2/3"))
    );
    assertEquals(
        new BigFraction("99/10000"),
        new BigFraction("1/100").subtract(new BigFraction("1/10000"))
    );
  }

  public void testMultiply() {
    // Test short-cuts
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("6/8").multiply(BigFraction.ONE)
    );
    assertEquals(
        new BigFraction("3/4"),
        BigFraction.ONE.multiply(new BigFraction("6/8"))
    );
    // Test signs
    assertEquals(
        new BigFraction("1/2"),
        new BigFraction("2/3").multiply(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("-1/2"),
        new BigFraction("2/3").multiply(new BigFraction("-3/4"))
    );
    assertEquals(
        new BigFraction("-1/2"),
        new BigFraction("-2/3").multiply(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("1/2"),
        new BigFraction("-2/3").multiply(new BigFraction("-3/4"))
    );
  }

  public void testDivide() {
    // Test short-cuts
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("6/8").divide(BigFraction.ONE)
    );
    assertEquals(
        new BigFraction("4/3"),
        BigFraction.ONE.divide(new BigFraction("6/8"))
    );
    // Test signs
    assertEquals(
        new BigFraction("8/9"),
        new BigFraction("2/3").divide(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("-8/9"),
        new BigFraction("2/3").divide(new BigFraction("-3/4"))
    );
    assertEquals(
        new BigFraction("-8/9"),
        new BigFraction("-2/3").divide(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("8/9"),
        new BigFraction("-2/3").divide(new BigFraction("-3/4"))
    );
  }

  public void testNegate() {
    assertEquals(
        new BigFraction("-3/4"),
        new BigFraction("3/4").negate()
    );
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("-3/4").negate()
    );
    // Should not trigger reduce
    assertEquals(
        new BigFraction("-6/8"),
        new BigFraction("6/8").negate()
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("-6/8").negate()
    );
  }

  public void testAbs() {
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("3/4").abs()
    );
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("-3/4").abs()
    );
    // Should not trigger reduce
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("6/8").abs()
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("-6/8").abs()
    );
  }

  public void testMax() {
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("3/4").max(new BigFraction("6/8"))
    );
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("6/8").max(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("6/8").max(new BigFraction("12/16"))
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("12/16").max(new BigFraction("6/8"))
    );
    assertEquals(
        new BigFraction("1/3"),
        new BigFraction("1/3").max(new BigFraction("1/6"))
    );
    assertEquals(
        new BigFraction("1/6"),
        new BigFraction("-1/3").max(new BigFraction("1/6"))
    );
  }

  public void testMin() {
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("3/4").min(new BigFraction("6/8"))
    );
    assertEquals(
        new BigFraction("3/4"),
        new BigFraction("6/8").min(new BigFraction("3/4"))
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("6/8").min(new BigFraction("12/16"))
    );
    assertEquals(
        new BigFraction("6/8"),
        new BigFraction("12/16").min(new BigFraction("6/8"))
    );
    assertEquals(
        new BigFraction("1/6"),
        new BigFraction("1/3").min(new BigFraction("1/6"))
    );
    assertEquals(
        new BigFraction("-1/3"),
        new BigFraction("-1/3").min(new BigFraction("1/6"))
    );
  }

  public void testPow() {
    assertEquals(
        new BigFraction("1/1"),
        new BigFraction("-1/3").pow(0)
    );
    assertEquals(
        new BigFraction("-1/3"),
        new BigFraction("-1/3").pow(1)
    );
    assertEquals(
        new BigFraction("-1/3"),
        new BigFraction("-3/9").pow(1)
    );
    assertEquals(
        new BigFraction("1/9"),
        new BigFraction("-1/3").pow(2)
    );
    assertEquals(
        new BigFraction("1/9"),
        new BigFraction("-3/9").pow(2)
    );
    assertEquals(
        new BigFraction("-1/27"),
        new BigFraction("-1/3").pow(3)
    );
    assertEquals(
        new BigFraction("-1/27"),
        new BigFraction("-3/9").pow(3)
    );
  }

  public void testValueOfBigDecimal() {
    assertEquals(
        new BigFraction("1/10"),
        BigFraction.valueOf(new BigDecimal("0.1"), false)
    );
    assertEquals(
        new BigFraction("1/4"),
        BigFraction.valueOf(new BigDecimal("0.25"), false)
    );
    assertEquals(
        new BigFraction("2398/1"),
        BigFraction.valueOf(new BigDecimal("2398"), false)
    );
    assertEquals(
        new BigFraction("4797/2"),
        BigFraction.valueOf(new BigDecimal("2398.5"), false)
    );
  }

  public void testGetBigInteger() {
    assertEquals(
        new BigInteger("0"),
        new BigFraction("1/3").getBigInteger(RoundingMode.DOWN)
    );
    assertEquals(
        new BigInteger("0"),
        new BigFraction("-1/3").getBigInteger(RoundingMode.DOWN)
    );
    assertEquals(
        new BigInteger("1"),
        new BigFraction("1/3").getBigInteger(RoundingMode.UP)
    );
    assertEquals(
        new BigInteger("-1"),
        new BigFraction("-1/3").getBigInteger(RoundingMode.UP)
    );
    assertEquals(
        new BigInteger("0"),
        new BigFraction("1/3").getBigInteger(RoundingMode.FLOOR)
    );
    assertEquals(
        new BigInteger("-1"),
        new BigFraction("-1/3").getBigInteger(RoundingMode.FLOOR)
    );
    assertEquals(
        new BigInteger("254"),
        new BigFraction("59436/234").getBigInteger()
    );
    assertEquals(
        new BigInteger("25"),
        new BigFraction("59436/2340").getBigInteger(RoundingMode.HALF_UP)
    );
  }

  public void testGetBigDecimal() {
    assertEquals(
        new BigDecimal("0.333"),
        new BigFraction("1/3").getBigDecimal(3, RoundingMode.DOWN)
    );
    assertEquals(
        new BigDecimal("-0.333"),
        new BigFraction("-1/3").getBigDecimal(3, RoundingMode.DOWN)
    );
    assertEquals(
        new BigDecimal("-0.334"),
        new BigFraction("-1/3").getBigDecimal(3, RoundingMode.FLOOR)
    );
    assertEquals(
        new BigDecimal("0.33333333333333333333"),
        new BigFraction("1/3").getBigDecimal(20, RoundingMode.DOWN)
    );
    assertEquals(
        new BigDecimal("-0.33333333333333333333"),
        new BigFraction("-1/3").getBigDecimal(20, RoundingMode.DOWN)
    );
    assertEquals(
        new BigDecimal("-0.33333333333333333334"),
        new BigFraction("-1/3").getBigDecimal(20, RoundingMode.FLOOR)
    );
  }

  public static void assertEquals(Object[] array1, Object[] array2) {
    if (!Arrays.equals(array1, array2)) {
      failNotEquals(null, Arrays.toString(array1), Arrays.toString(array2));
    }
  }

  public void testPercentages() {
    // 48% * 1/4 = 3/25
    assertEquals(
        "3/25",
        new BigFraction("48%").multiply(new BigFraction("1/4")).toString()
    );

    // 48% / 4/1 = 12%
    assertEquals(
        "12%",
        new BigFraction("48%").divide(new BigFraction("4/1")).toString()
    );

    // 25% + 3/4 = 100%
    assertEquals(
        "100%",
        new BigFraction("25%").add(new BigFraction("3/4")).toString()
    );

    // 3/4 + 25% = 100%
    assertEquals(
        "100%",
        new BigFraction("3/4").add(new BigFraction("25%")).toString()
    );

    // 12.5% + 12.5% = 25%
    assertEquals(
        "25%",
        new BigFraction("12.5%").add(new BigFraction("12.5%")).toString()
    );

    // 25% / 2/1 = 12.5%
    assertEquals(
        "12.5%",
        new BigFraction("25%").divide(new BigFraction("2/1")).toString()
    );

    // 25% / 4/1 = 6.25%
    assertEquals(
        "6.25%",
        new BigFraction("25%").divide(new BigFraction("4/1")).toString()
    );

    // 25% / 8/1 = 3.125%
    assertEquals(
        "3.125%",
        new BigFraction("25%").divide(new BigFraction("8/1")).toString()
    );

    // 25% / 16/1 = 3.125%
    assertEquals(
        "1/64",
        new BigFraction("25%").divide(new BigFraction("16/1")).toString()
    );

    // 25% * 1/1 = 1/4
    assertEquals(
        "1/4",
        new BigFraction("25%").multiply(new BigFraction("1/1")).toString()
    );

    // 25% / 1/1 = 25%
    assertEquals(
        "25%",
        new BigFraction("25%").divide(new BigFraction("1/1")).toString()
    );
  }

  public void testFractionalMoneyProportional() {
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("100000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/3"),
            new BigFraction("1/3"),
            new BigFraction("1/3")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("100000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-33333.34"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("-200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("100000.00"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/2"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-33333.34"),
            new BigDecimal("-100000.00"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("-200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/2"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("0.00"),
            new BigDecimal("33333.33"),
            new BigDecimal("100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("0/1"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
            new BigDecimal("0.01"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.02"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.01"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
        },
        BigFraction.distributeValue(
            new BigDecimal("0.01"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("100%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.00"),
        },
        BigFraction.distributeValue(
            new BigDecimal("0.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/1")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("133333.34")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("2/3")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.42"),
            new BigDecimal("85714.29"),
            new BigDecimal("85714.29")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.29"),
            new BigDecimal("85714.29")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.01"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.30"),
            new BigDecimal("85714.29")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.02"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.30"),
            new BigDecimal("85714.30")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.03"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.42857"),
            new BigDecimal("85714.28572"),
            new BigDecimal("85714.28571")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00000"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.42857"),
            new BigDecimal("85714.28572"),
            new BigDecimal("85714.28572")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00001"),
            BigFraction.DistributionMethod.PROPORTIONAL,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
  }

  public void testFractionalMoneyHalfUp() {
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("100000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/3"),
            new BigFraction("1/3"),
            new BigFraction("1/3")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00"),
            new BigDecimal("25000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("100000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33"),
            new BigDecimal("100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-33333.34"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("-200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("100000.00"),
            new BigDecimal("33333.33"),
            new BigDecimal("33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/2"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-33333.34"),
            new BigDecimal("-100000.00"),
            new BigDecimal("-33333.33"),
            new BigDecimal("-33333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("-200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/2"),
            new BigFraction("1/6"),
            new BigFraction("1/6")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("0.00"),
            new BigDecimal("33333.33"),
            new BigDecimal("100000.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("0/1"),
            new BigFraction("1/6"),
            new BigFraction("1/2")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
            new BigDecimal("0.01"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.02"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.01"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        },
        BigFraction.distributeValue(
            new BigDecimal("0.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4"),
            new BigFraction("1/4")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.01"),
        },
        BigFraction.distributeValue(
            new BigDecimal("0.01"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("100%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("0.00"),
        },
        BigFraction.distributeValue(
            new BigDecimal("0.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/1")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("33333.34"),
            new BigDecimal("33333.33"),
            new BigDecimal("133333.33")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/6"),
            new BigFraction("1/6"),
            new BigFraction("2/3")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.29"),
            new BigDecimal("85714.28")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.29"),
            new BigDecimal("85714.29")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.01"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.30"),
            new BigDecimal("85714.29")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.02"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.43"),
            new BigDecimal("85714.30"),
            new BigDecimal("85714.30")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.03"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.42857"),
            new BigDecimal("85714.28572"),
            new BigDecimal("85714.28571")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00000"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("28571.42857"),
            new BigDecimal("85714.28572"),
            new BigDecimal("85714.28572")
        },
        BigFraction.distributeValue(
            new BigDecimal("200000.00001"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("1/7"),
            new BigFraction("3/7"),
            new BigFraction("3/7")
        )
    );
  }

  // These percentages are from production client data
  public void testFractionalMoneyPca() {
    // id=2
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("519.58"),
            new BigDecimal("779.37")
        },
        BigFraction.distributeValue(
            new BigDecimal("1298.95"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-519.58"),
            new BigDecimal("-779.37")
        },
        BigFraction.distributeValue(
            new BigDecimal("-1298.95"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    // id=7
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("1110.96"),
            new BigDecimal("1666.43")
        },
        BigFraction.distributeValue(
            new BigDecimal("2777.39"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-1110.96"),
            new BigDecimal("-1666.43")
        },
        BigFraction.distributeValue(
            new BigDecimal("-2777.39"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    // id=24
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("436.92"),
            new BigDecimal("655.39")
        },
        BigFraction.distributeValue(
            new BigDecimal("1092.31"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-436.92"),
            new BigDecimal("-655.39")
        },
        BigFraction.distributeValue(
            new BigDecimal("-1092.31"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    // id=45
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("495.46"),
            new BigDecimal("743.20")
        },
        BigFraction.distributeValue(
            new BigDecimal("1238.66"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-495.46"),
            new BigDecimal("-743.20")
        },
        BigFraction.distributeValue(
            new BigDecimal("-1238.66"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    // id=82
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("494.43"),
            new BigDecimal("741.65")
        },
        BigFraction.distributeValue(
            new BigDecimal("1236.08"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-494.43"),
            new BigDecimal("-741.65")
        },
        BigFraction.distributeValue(
            new BigDecimal("-1236.08"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    // id=88
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("541.37"),
            new BigDecimal("812.06")
        },
        BigFraction.distributeValue(
            new BigDecimal("1353.43"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
    assertEquals(
        new BigDecimal[]{
            new BigDecimal("-541.37"),
            new BigDecimal("-812.06")
        },
        BigFraction.distributeValue(
            new BigDecimal("-1353.43"),
            BigFraction.DistributionMethod.HALF_UP,
            new BigFraction("40%"),
            new BigFraction("60%")
        )
    );
  }
}
