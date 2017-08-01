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

import java.util.EmptyStackException;

import totalcross.unit.TestCase;

public class TestEmptyStackException extends TestCase
{
  @Override
  public void testRun()
  {
    try
    {
      throw new EmptyStackException();
    }
    catch (EmptyStackException exception)
    {
      assertTrue(exception instanceof EmptyStackException);
    }

  }

}
