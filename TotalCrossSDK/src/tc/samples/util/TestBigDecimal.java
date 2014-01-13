package tc.samples.util;

import totalcross.sys.*;
import totalcross.unit.TestCase;
import totalcross.util.*;

public class TestBigDecimal extends TestCase
{
   private final static int bitlen = 100;

   public void testRun()
   {
      try
      {
         testConstants();
         testConstructor();
         testRandom();
         testAddition();
         testMultiplication();
         testDistributive();
      }
      catch (InvalidNumberException exception)
      {
         exception.printStackTrace();
         fail();
      }
   }
   
   /**
    * Tests static initialization and constants.
    */
   private void testConstants() 
   {
      BigDecimal a = BigDecimal.ONE.subtract(BigDecimal.ONE);
   
      assertEquals(a.compareTo(BigDecimal.ZERO), 0);
      assertEquals(a.intValue(), BigDecimal.ZERO.intValue());
      assertEquals(BigDecimal.ONE.intValue(), BigDecimal.ONE.intValue());
   }
   
   /**
    * Tests constructor and toString. 
    * @throws InvalidNumberException 
    */
   private void testConstructor() throws InvalidNumberException 
   {
      BigDecimal a = new BigDecimal("3.4");
      a = a.add(a);    
      assertEquals(0, new BigDecimal("6.8").compareTo(a));
    
      String s = "6.1111111111111111111111111111111111111111111";
      String t = new BigDecimal(s).toString();    
      assertEquals(s, t);
    
      a = new BigDecimal(1);
      BigDecimal b = new BigDecimal(-1).add(a);
    
      assertEquals(a.intValue(), BigDecimal.ONE.intValue());
      assertEquals(b.compareTo(BigDecimal.ZERO), 0);
      assertEquals(b.intValue(), BigDecimal.ZERO.intValue());
   }
        
   /**
    * Tests random rationals.
    * @throws InvalidNumberException 
    */
   private void testRandom() throws InvalidNumberException 
   {
      BigDecimal a = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      BigDecimal b = new BigDecimal("" + a);
      BigDecimal c = a.subtract(a);
      assertEquals(c, BigDecimal.ZERO);    
      assertEquals(0, b.compareTo(new BigDecimal( "" + b )));
   } 
    
   /**
    * Tests addition.
    */
   private void testAddition() 
   {
      BigDecimal a = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(0, a.add(a).subtract(a).compareTo(a));
      BigDecimal b = a.add(BigDecimal.ZERO);
      assertEquals(0, b.compareTo(a));
      b = a.subtract( BigDecimal.ZERO );
      assertEquals(0, b.compareTo(a));
      b = a.subtract(a);
      assertEquals(b.compareTo(BigDecimal.ZERO), 0);
    
      b = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(b.add(a).compareTo(a.add(b)), 0);
   }
       
   /**
    * Tests multiplication.
    * @throws InvalidNumberException 
    */
   private void testMultiplication() throws InvalidNumberException 
   {
      BigDecimal a = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(a.multiply(a).divide(a).compareTo(a), 0);    
      assertEquals(a.multiply(BigDecimal.ONE), a);
      assertEquals(0, a.divide(BigDecimal.ONE).compareTo(a));
         
      a = new BigDecimal(new BigInteger(32, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(a.multiply(BigDecimal.ONE.divide(a, 32, BigDecimal.ROUND_HALF_EVEN)).doubleValue(), 1.0, 0.0001);
    
      BigDecimal b = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(b.multiply(a).compareTo(a.multiply(b)), 0);
    
      BigDecimal c = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      assertEquals(a.multiply(b.multiply(c)).compareTo(a.multiply(b).multiply(c)), 0);
   }
       
   /**
    * Tests distributive law.
    */
   private void testDistributive() 
   {
      BigDecimal a = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      BigDecimal b = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));
      BigDecimal c = new BigDecimal(new BigInteger(bitlen, new Random(Convert.MAX_INT_VALUE >> 1)));    
      assertEquals(a.multiply(b.add(c)).compareTo(a.multiply(b).add(a.multiply(c))), 0);
   }
}
