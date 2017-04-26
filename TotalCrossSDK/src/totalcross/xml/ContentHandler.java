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
* Receive notification of the logical content of a document.
* <P><I><B>Note:</B>
* While in the SAX 2.0 spirit, this implementation is not fully
* compliant.&nbsp;
* Speed and footprint took precedence over what the author judged being
* details.</I></P>
*
* <p>This is the main interface that most SAX applications
* implement: if the application needs to be informed of basic parsing
* events, it implements this interface and registers an instance with
* the XML reader using the {@link XmlReader#setContentHandler}
* method.  The XML reader uses the instance to report
* basic document-related events like the start and end of elements
* and character data.</p>
*
* <p>The order of events in this interface is very important, and
* mirrors the order of information in the document itself.  For
* example, all of an element's content (character data, processing
* instructions, and/or subelements) will appear, in order, between
* the startElement event and the corresponding endElement event.</p>
*/

public abstract class ContentHandler
{
   /**
   * Receive notification of the beginning of an element.
   *
   * <p>The XmlReader will invoke this method at the beginning of every
   * element in the XML document; there will be a corresponding
   * {@link #endElement endElement} event for every startElement event
   * (even when the element is empty). All of the element's content will be
   * reported, in order, before the corresponding endElement
   * event.</p>
   *
   * @param tag tag identifier for this element
   * @param atts The attributes list attached to the element.
   */
   public abstract void startElement(int tag, AttributeList atts);

   /**
   * Receive notification of the end of an element.
   *
   * <p>The XMLReader will invoke this method at the end of every
   * element in the XML document; there will be a corresponding
   * {@link #startElement startElement} event for every endElement
   * event (even when the element is empty).</p>
   *
   * @param tag tag identifier for this element
   */
   public abstract void endElement(int tag);

   /**
   * Receive notification of character data.
   *
   * <p>The XMLReader will call this method to report each chunk of
   * character data.  This XMLReader implementation return all contiguous
   * character data in a single chunk.
   *
   * @param s The string of characters from the XML document.
   */
   public abstract void characters(String s);

   /**
   * Receive notification of comment data.
   *
   * <p>The XMLReader will call this method to report each chunk of
   * comments.  This XMLReader implementation returns all contiguous
   * comment data in a single chunk.
   *
   * @param s The string of characters from the XML document.
   */
   public void comment(String s) {}

   /**
    * Receive notification of cdata.
    * 
    * <p>
    * The XMLReader will call this method to report each chunk of cdata found during the parse. This XMLReader
    * implementation returns all contiguous data in a single chunk, except for the <CODE><B>&lt;![CDATA[</CODE></B> and
    * <CODE><B>]]&gt;</CODE></B> delimiters.
    * 
    * @param tag
    *           tag identifier for this element
    * @param cdata
    *           unparsed data from the XML document.
    * @since TotalCross 1.27
    */
   public void cdata(int tag, String cdata)
   {
   }
}
