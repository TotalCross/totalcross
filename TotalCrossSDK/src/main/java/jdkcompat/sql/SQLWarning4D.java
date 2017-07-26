/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package jdkcompat.sql;

import java.sql.SQLException;

public class SQLWarning4D extends SQLException
{
   SQLWarning4D next;
   
   public SQLWarning4D(String reason, String sqlState, int errorCode)
   {
      super(reason, sqlState, errorCode);
   }

   public SQLWarning4D(String reason, String sqlState)
   {
      super(reason, sqlState);
   }

   public SQLWarning4D(String reason)
   {
      super(reason);
   }

   public SQLWarning4D()
   {
      super();
   }

   public SQLWarning4D getNextWarning()
   {
      return next;
   }

   public void setNextException(SQLWarning4D ex)
   {
      next = ex;
   }
}
