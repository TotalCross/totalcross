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

import totalcross.net.URI;

/**
 * <code>XmlReadable</code> abstracts any sequential resource that can be
 * passed to an XmlReader.
 *
 * @author Pierre G. Richard
 */
public interface XmlReadable
{
   /**
    * Read this XmlReadable through an XmlReader parser.
    *
    * @param rdr
    *           the XmlReader that will report the SAX events
    * @exception SyntaxException
    * @throws totalcross.io.IOException
    */
   public void readXml(XmlReader rdr) throws SyntaxException, totalcross.io.IOException;

   /**
    * Get the base URI attached to this XmlReadable
    *
    * @return the base URI attached to this XmlReadable
    */
   public URI getBaseURI();
   
   public void setCaseInsensitive(boolean caseInsensitive); // guich@tc113_29      
}
