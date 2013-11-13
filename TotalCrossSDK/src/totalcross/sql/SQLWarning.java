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
   public SQLWarning(String reason, String SQLState, int vendorCode)
   {
   }

   public SQLWarning(String reason, String SQLState)
   {
   }

   public SQLWarning(String reason)
   {
   }

   public SQLWarning()
   {
   }

   public String getSQLState()
   {
      return null;
   }

   public int getErrorCode()
   {
      return 0;
   }

   public SQLWarning getNextWarning()
   {
      return null;
   }

   public void setNextException(SQLWarning ex)
   {
   }
}
