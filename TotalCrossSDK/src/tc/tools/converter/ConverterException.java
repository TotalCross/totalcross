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

// $Id: ConverterException.java,v 1.3 2011-01-04 13:19:25 guich Exp $

package tc.tools.converter;

public class ConverterException extends RuntimeException
{
   public ConverterException()
   {
   }

   public ConverterException(String msg)
   {
      super(msg);
   }
}
