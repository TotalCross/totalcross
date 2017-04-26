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

/**
* Make an XmlReadable from a File
* Example:
* <PRE>
*    XmlReader rdr = new XmlReader();
*    rdr.setContentHandler(...);
*    File f = new File(...);
*    rdr.parse(new XmlReadableFile(f));
* </PRE>
*/
public class XmlReadableFile extends XmlReadableByteArray
{
   /**
   * Constructor
   *
   * @param f The file you want to read. Be sure to don't open it with DONT_OPEN mode.
   * @throws IOException
   */
   public XmlReadableFile(File f) throws totalcross.io.IOException
   {
      buf = new byte[f.getSize()];
      f.readBytes(buf, 0, buf.length);
   }
}
