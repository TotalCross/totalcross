/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter.regalloc;

import tc.tools.converter.TCValue;
import totalcross.util.Vector;

public class Stack
{
  public Vector elems = new Vector(8);

  public void push(int e)
  {
    elems.addElement(new TCValue(e));
  }

  public int pop()
  {
    TCValue v = (TCValue) elems.items[elems.size()-1];
    elems.removeElementAt(elems.size()-1);
    return v.asInt;
  }

  public boolean isEmpty()
  {
    return (elems.size() == 0);
  }

  public void erase()
  {
    elems.removeAllElements();
  }
}
