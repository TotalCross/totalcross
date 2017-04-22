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

package totalcross.sql;

public class SQLWarning extends SQLException
{
   SQLWarning next;
   
   public SQLWarning(String reason, String sqlState, int errorCode)
   {
      super(reason, sqlState, errorCode);
   }

   public SQLWarning(String reason, String sqlState)
   {
      super(reason, sqlState);
   }

   public SQLWarning(String reason)
   {
      super(reason);
   }

   public SQLWarning()
   {
      super();
   }

   public SQLWarning getNextWarning()
   {
      return next;
   }

   public void setNextException(SQLWarning ex)
   {
      next = ex;
   }
}
