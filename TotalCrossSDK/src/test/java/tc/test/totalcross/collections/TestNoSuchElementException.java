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

package tc.test.totalcross.collections;

import java.util.NoSuchElementException;

import totalcross.unit.TestCase;

public class TestNoSuchElementException extends TestCase
{
  @Override
  public void testRun()
  {
    try
    {
      throw new NoSuchElementException();
    }
    catch (NoSuchElementException exception)
    {
      assertTrue(exception instanceof NoSuchElementException);
    }

    try
    {
      throw new NoSuchElementException("erro");
    }
    catch (NoSuchElementException exception)
    {
      assertTrue(exception instanceof NoSuchElementException);
      assertEquals("erro", exception.getMessage());
    }    
  }
}
