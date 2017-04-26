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

/**
* Make an XmlReadable from a String.
* Example:
* <PRE>
*    XmlReader rdr = new XmlReader();
*    rdr.setContentHandler(...);
*    rdr.parse(new XmlReadableString("Hello World!"));
* </PRE>
*/
public class XmlReadableString extends XmlReadableByteArray
{
   /**
   * Constructor
   *
   * @param source String that contains the XML readable HTML
   */
   public XmlReadableString(String source)
   {
      super(source.getBytes());
   }
}
