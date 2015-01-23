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



package totalcross.net.mail;

import totalcross.io.*;
import totalcross.sys.*;

/**
 * Implementation of DataContentHandler that handles textual (not encoded) MIME types.
 * 
 * @since TotalCross 1.13
 */
public class TextContentHandler extends DataContentHandler
{
   public void writeTo(Object obj, String mimeType, Stream stream) throws IOException
   {
      String text;
      if (obj instanceof Part)
      {
         Part part = (Part) obj;
         text = (String) part.content;
      }
      else
         text = obj.toString();
      stream.writeBytes(Convert.CRLF_BYTES);
      stream.writeBytes(text);
      stream.writeBytes(Convert.CRLF_BYTES);
   }
}
