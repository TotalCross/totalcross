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



package totalcross.io;

/** Thrown when a file was not found. */

public class FileNotFoundException extends IOException
{
   /** Constructs an empty Exception. */
   public FileNotFoundException()
   {
      super();
   }

   /** Constructs an exception with the given message. */
   public FileNotFoundException(String path)
   {
      super("File not found: " + path);
   }

   /** Returns the file name from the exception's message.
    * If there's no file name, returns an empty string.
    * @since TotalCross 1.15
    */
   public String getFileName() // guich@tc115_44
   {
      String msg = getMessage();
      int idx;
      if (msg != null && (idx=msg.indexOf(':')) >= 0)
         msg = msg.substring(idx).trim();
      else
         msg = "";
      return msg;
   }
}
