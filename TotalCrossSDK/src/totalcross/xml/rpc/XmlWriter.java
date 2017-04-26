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



package totalcross.xml.rpc;

import totalcross.sys.*;

// Copyright (C) 2004 Nimkathana (www.nimkathana.com), USA
//
// License: LGPL
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, execute to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

/**
 * Correctly formats Strings in XML syntax. Currently only supports ISO-8859-1 encoding of XML.
 *
 * @version March 2006
 * @author Added to SuperWaba by Guich
 * @author Maintained by Nimkathana (<a href="http://www.nimkathana.com">www.nimkathana.com</a>)
 * @author Original by IOP GmbH (<a href="http://www.iop.de">www.iop.de</a>)
 */
public class XmlWriter
{
   private StringBuffer buf = new StringBuffer(4000);
   private String header = "<?xml version=\"1.0\"?>";

   /**
    */
   public XmlWriter()
   {
      buf.append(header);
   }

   /** Resets this XmlWriter so it can be used again */
   public void reset()
   {
      buf.setLength(header.length()); // keep only the header
   }

   /**
    * Write the given text.
    */
   public void write(String text)
   {
      buf.append(text);
   }

   /**
    * Gets this writer's contents as string (already appends a \n).
    * Consider using getBytes() instead of this one.
    * 
    * @see #getBytes()
    * @return All that was written to the internal buffer
    */
   public String toString()
   {
      return buf.append('\n').toString();
   }

   /**
    * Gets this writer's contents as a byte array (already appends a \n)
    *
    * @return All that was written to the internal buffer
    * @since TotalCross 1.23
    */
   public byte[] getBytes()
   {
      return Convert.getBytes(buf.append('\n'));
   }

   /**
    * Formats an opening XML tag
    *
    * @param elem
    *           The element of the opening tag
    */
   public void startElement(String elem)
   {
      buf.append('<').append(elem).append('>');
   }

   /**
    * Formats a closing XML tag
    *
    * @param elem
    *           The element of the closing tag
    */
   public void endElement(String elem)
   {
      buf.append("</").append(elem).append('>');
   }

   /**
    * Formats cdata for XML
    *
    * @param text
    *           The character data to be formatted
    */
   public void chardata(String text)
   {
      text = Convert.replace(text, "&", "&amp;"); // this must go first!
      text = Convert.replace(text, "<", "&lt;");
      text = Convert.replace(text, ">", "&gt;");
      buf.append(text);
   }
}
