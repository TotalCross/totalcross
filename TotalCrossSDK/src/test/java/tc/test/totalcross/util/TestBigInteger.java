/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.util;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.unit.TestCase;
import totalcross.util.BigInteger;
import totalcross.util.Random;

public class TestBigInteger extends TestCase
{
  private final static int bitlen = 100;

  @Override
  public void testRun()
  {
    try
    {
      testConstants();
      testConstructor();
      testRandom();
      testAddition();
      testMultiplication();
      testGcd();
    } catch (InvalidNumberException exception)
    {
      fail();
    }      
  }

  /**
   * Tests static initialization and constants.
   */
  private void testConstants() 
  {  
    assertEquals(BigInteger.ONE.subtract(BigInteger.ONE).intValue(), BigInteger.ZERO.intValue());
    assertEquals(BigInteger.ZERO.intValue(), 0);
    assertEquals(BigInteger.ONE.intValue(), 1);
    assertEquals(BigInteger.TEN.intValue(), 10);
  }

  /**
   * Test constructor and toString.
   * @throws InvalidNumberException 
   */
  private void testConstructor() throws InvalidNumberException 
  {
    assertEquals(new BigInteger("34"), new BigInteger("34"));
    assertEquals(new BigInteger("-4"), new BigInteger("-4"));

    String s = "1111111111111111111111111111111111111111111";
    String t = new BigInteger(s).toString();
    assertEquals(s, t);

    BigInteger a = new BigInteger("1");
    BigInteger b = new BigInteger("-1");

    assertEquals(a.intValue(), 1);
    assertEquals(b.intValue(), -1);
    assertEquals(a.intValue(), b.negate().intValue());
    assertEquals(a.add(b).intValue(), BigInteger.ZERO.intValue());
  }

  /**
   * Test random integer. 
   * @throws InvalidNumberException 
   */
  private void testRandom() throws InvalidNumberException 
  {
    BigInteger a = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger b = new BigInteger("" + a);

    assertEquals(b.subtract(a).intValue(), BigInteger.ZERO.intValue());
    assertEquals(0, b.compareTo(new BigInteger(b.toString())));
  }

  /**
   * Tests addition.
   */
  private void testAddition() 
  {
    BigInteger a = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger b = a.add(BigInteger.ZERO);
    BigInteger c = a.add(a).subtract(a);

    assertEquals(c, a);
    assertEquals(0, c.compareTo(a));

    assertEquals(b.intValue(), a.intValue());
    b = a.subtract(BigInteger.ZERO);
    assertEquals(b.intValue(), a.intValue());
    b = a.subtract(a);
    assertEquals(b.intValue(), BigInteger.ZERO.intValue());  
  }

  /**
   * Test multiplication.
   */
  private void testMultiplication() 
  {
    BigInteger a = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger b = a.multiply(a);
    BigInteger c = b.divide(a);

    assertEquals(c.intValue(), a.intValue());
    assertEquals(0, c.compareTo(a));

    assertEquals(a.multiply(BigInteger.ONE).intValue(), a.intValue());
    assertEquals(a.divide(BigInteger.ONE).intValue(), a.intValue());

    a = new BigInteger(bitlen << 1, new Random(Convert.MAX_INT_VALUE >> 1));
    b = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger[] qr = a.divideAndRemainder(b);
    c = qr[0].multiply(b);
    c = c.add(qr[1]);
    assertEquals(a.intValue(), c.intValue());
  }

  /**
   * Tests gcd. 
   */
  private void testGcd() 
  {
    BigInteger a = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger b = new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1));
    BigInteger c = a.gcd(b);

    BigInteger[] qr = a.divideAndRemainder(c);
    assertEquals(a.intValue(), qr[0].multiply(c).intValue());
    assertEquals(qr[1].intValue(), BigInteger.ZERO.intValue());

    qr = b.divideAndRemainder(c);
    assertEquals(b.intValue(), qr[0].multiply(c).intValue());
    assertEquals(qr[1].intValue(), BigInteger.ZERO.intValue());

    c = new BigInteger(bitlen << 2, new Random(Convert.MAX_INT_VALUE >> 1));
    a = a.multiply(c);
    b = b.multiply(c);
    c = a.gcd(b); // = c

    qr = a.divideAndRemainder(c);
    assertEquals(a.intValue(), qr[0].multiply(c).intValue());
    assertEquals(qr[1].intValue(), BigInteger.ZERO.intValue());

    qr = b.divideAndRemainder(c);
    assertEquals(b.intValue(), qr[0].multiply(c).intValue());
    assertEquals(qr[1].intValue(), BigInteger.ZERO.intValue());
  }
}
