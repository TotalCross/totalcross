// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

import totalcross.net.URI;

/**
 * <code>XmlReadable</code> abstracts any sequential resource that can be
 * passed to an XmlReader.
 *
 * @author Pierre G. Richard
 */
public interface XmlReadable {
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
