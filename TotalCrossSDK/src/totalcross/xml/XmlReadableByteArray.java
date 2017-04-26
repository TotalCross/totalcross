/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.xml;
import totalcross.io.*;
import totalcross.net.*;

/** Make an XmlReadable from a byte array. */
public class XmlReadableByteArray implements XmlReadable
{
   protected byte[] buf;
   protected URI baseURI;
   private boolean caseInsensitive;

   /**
   * Constructor
   *
   * @param source The byte array to be parsed
   */
   public XmlReadableByteArray(byte[] source)
   {
      buf = source;
   }
   
   protected XmlReadableByteArray()
   {
   }

   public void readXml(XmlReader rdr) throws SyntaxException, IOException
   {
      rdr.setCaseInsensitive(caseInsensitive);
      rdr.parse(buf, 0, buf.length);
      //buf = null; - don't do this, otherwise HtmlBrowser's font change won't work 
   }

   public URI getBaseURI()
   {
      return baseURI;
   }

   public void setCaseInsensitive(boolean caseInsensitive)
   {
      this.caseInsensitive = caseInsensitive;
   }
}
