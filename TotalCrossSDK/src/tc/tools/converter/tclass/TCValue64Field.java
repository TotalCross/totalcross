/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: TCValue64Field.java,v 1.12 2011-01-04 13:19:25 guich Exp $

package tc.tools.converter.tclass;

/* This structure represents a class field. */
public final class TCValue64Field extends TCField
{
   public double dvalue; // may be a long too. see flags.isLong
   public long lvalue;
}
