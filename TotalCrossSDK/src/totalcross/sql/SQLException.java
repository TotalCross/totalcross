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

public class SQLException extends Exception
{
   String state;
   int code;
   SQLException next;
   
   public SQLException(String reason, String sqlState, int errorCode)
   {
      super(reason);
      this.state = sqlState;
      this.code = errorCode;
   }

   public SQLException(String reason, String sqlState)
   {
      this(reason, sqlState, 0);
   }

   public SQLException(String reason)
   {
      this(reason,null,0);
   }

   public SQLException()
   {
      this(null,null,0);
   }

   public String getSQLState()
   {
      return state;
   }

   public int getErrorCode()
   {
      return code;
   }

   public SQLException getNextException()
   {
      return next;
   }

   public void setNextException(SQLException ex)
   {
      next = ex;
   }
}
