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

import totalcross.io.Stream;

   /**
    * Used to read HTML or XML documents, reporting events to handlers (for
    * example, {@link ContentHandler}).
    * <P>
    * <I><B>Note:</B> While in the SAX 2.0 spirit, this implementation is not
    * fully compliant.&nbsp; Speed and footprint took precedence over what the
    * author judged being details.</I>
    * </P>
    * <P>
    * Unlike SAX, reporting tag names, like in
    * {@link ContentHandler#startElement}, passes an integral
    * <code><B>tag code</B></code> rather than the name itself.&nbsp; This
    * is, again, for performance reasons.&nbsp; Comparing integers vs. strings is
    * notably more efficient and tag name comparison is heavily used for XML
    * applications.
    * <P>
    * The <code>tag code</code> must uniquely identify the name of the
    * tag.&nbsp; The default implementation &mdash; see {@link #getTagCode} in
    * this code &mdash; simply consists to hash the tag name.&nbsp; It can be
    * overriden to suit specific needs.
    * <P>
    * Tag names should be translated to tag codes as soon as they are known,
    * when reading the DTD for instance, or computed in advance and saved into a
    * static correspondence table.&nbsp;
    *
    * @author Pierre G. Richard
    */
public class XmlReader extends XmlTokenizer
{
   private static final int     SPRING_STATE    = 0;
   private static final int     START_TAG_STATE = 1;
   private static final int     END_TAG_STATE   = 2;
   private static final int     PCDATA_STATE    = 3;

   /**
    * hash ID of current tag name, set by <code>foundStartTagName</code> or
    * <code>foundEndTagName</code>
    */
   protected int                tagNameHashId;

   private StringBuffer         pcdata;                                                //CKC
   private int                  state;
   private int                  newlineSignificant;
   private AttributeList        attList;
   private String               attrName;
   private ContentHandler       cntHandler;

   public XmlReader()
   {
      attList = new AttributeList();
      pcdata = new StringBuffer(1000);
   }

   /** Set to true if you want the get/set methods of the AttributeList to be case insensitive. */
   public void setCaseInsensitive(boolean caseInsensitive) // guich@tc113_29
   {
      attList.caseInsensitive = caseInsensitive;
   }
   
    /**
       * Allow an application to register a content event cntHandler.
       * <p>
       * If the application does not register a content cntHandler, all content
       * events reported by the SAX parser will be silently ignored.
       * </p>
       * <p>
       * Applications may register a new or different cntHandler in the middle
       * of a parse, and the SAX parser must begin using the new cntHandler
       * immediately.
       * </p>
       *
       * @param cntHandler
       *           The content cntHandler.
       * @exception java.lang.NullPointerException
       *               If the cntHandler argument is null.
       * @see #getContentHandler
       */
   public void setContentHandler(ContentHandler cntHandler)
   {
      this.cntHandler = cntHandler;
   }

    /**
       * Set an AttributeList.Filter to filter the attribute entered in the
       * AttributeList
       *
       * @param filter
       *           AttributeList.Filter to set, or null if the current
       *           AttributeList filter must be removed
       * @return The previous AttributeList.Filter or null if none was set
       */
   public AttributeList.Filter setAttributeListFilter(AttributeList.Filter filter)
   {
      return attList.setFilter(filter);
   }

    /**
       * Return the current content cntHandler.
       *
       * @return The current content cntHandler, or null if none has been
       *         registered.
       * @see #setContentHandler
       */
   public ContentHandler getContentHandler()
   {
      return cntHandler;
   }

    /**
       * Parse an XML document from a Stream.
       * <p>
       * The application can use this method to instruct the XML reader to begin
       * parsing an XML document from reading a Stream.
       * <p>
       * Here is the general contract for all <code>parse</code> methods.
       * <p>
       * Applications may not invoke this method while a parse is in progress
       * (they should create a new XMLReader instead for each nested XML
       * document). Once a parse is complete, an application may reuse the same
       * XMLReader object, possibly with a different input source.
       * </p>
       * <p>
       * During the parse, the XMLReader will provide information about the XML
       * document through the registered event handlers.
       * </p>
       * <p>
       * This method is synchronous: it will not return until the parsing has ended.
       * If a client application wants to terminate the parsing early, it should
       * throw an exception.
       * </p>
       *
       * @param input
       *           The input source for the top-level XML document.
       * @exception SyntaxException
       * @throws totalcross.io.IOException
       * @see #setContentHandler
       */
   public final void parse(Stream input) throws SyntaxException, totalcross.io.IOException
   {
      newlineSignificant = 0;
      state = SPRING_STATE;
      if (cntHandler != null)
         tokenize(input);
   }

    /**
       * Parse an XML document from an already buffered stream.
       * <P>
       * Unlike the general method above, this method requires more arguments.
       * It should be used when the HTML document is embedded within an HTTP
       * stream.
       * <P>
       * See the general contract of {@link XmlReader#parse(Stream)}.
       *
       * @param input
       *           stream to parse
       * @param buffer
       *           buffer, already filled with bytes read from the input stream
       * @param start
       *           starting position in the buffer
       * @param end
       *           ending position in the buffer
       * @param pos
       *           read position of the byte at offset 0 in the buffer
       * @exception SyntaxException
       * @throws totalcross.io.IOException
       */
   public final void parse(Stream input, byte[] buffer, int start, int end, int pos) throws SyntaxException,
         totalcross.io.IOException
   {
      newlineSignificant = 0;
      state = SPRING_STATE;
      if (cntHandler != null)
         tokenize(input, buffer, start, end, pos);
   }

    /**
       * Parse an XmlReadable
       * Impl. Note: This is just for conveniency. It is more natural to write:
       * rdr.parse(doc) than doc.readXml(rdr)
       *
       * @param input
       *           The input source for the top-level XML document.
       * @throws totalcross.io.IOException
       */
   public final void parse(XmlReadable input) throws SyntaxException, totalcross.io.IOException
   {
      input.readXml(this);
   }

    /**
       * Parse XML data from an array of bytes, offset and count.
       * <P>
       * See the general contract of {@link XmlReader#parse(Stream)}.
       *
       * @param input
       *           byte array to parse
       * @param offset
       *           position of the first byte in the array
       * @param count
       *           number of bytes to parse
       * @exception SyntaxException
       */
   public final void parse(byte[] input, int offset, int count) throws SyntaxException
   {
      newlineSignificant = 0;
      state = SPRING_STATE;
      if (cntHandler != null)
         tokenize(input);
   }

    /**
       * Enable or disable coalescing white spaces, according to HTML rules.
       * <P>
       * White spaces are any character less or equal to the ascii space (0x20).
       * <P>
       * This method allows to process the contents of pre-formatted lines, such
       * as the contents of the &lt;PRE&gt; tag.&nbsp; When the parsing process starts,
       * newlines are not significant.&nbsp; Hence, setNewLineSignificant must
       * be called <b>after</b> the parsing has started.&nbsp; For example, to make
       * all newlines significant:
       *
       * <PRE>
       * class MyXmlReader extends XmlReader
       * {
       *    public void foundStartOfInput(byte input[], int offset, int count)
       *    {
       *       setNewLineSignificant(true);
       *    }
       * }
       *
       * </PRE>
       *
       * <P>
       * <U>Note:</U> this is a "stacked" call.
       *
       * <PRE>
       *
       * setNewlineSignificant(true); // newlines are significant - stack is 1 
       * setNewlineSignificant(true); // newlines are significant - stack is 2 
       * setNewlineSignificant(false); // newlines are still significant - stack is 1
       * setNewlineSignificant(false); // newlines are no more significant again - stack is 0
       *
       *
       * </PRE>
       *
       * @param val
       *           true if newline characters must be significant, false if they
       *           must be collapsed according to HTML rules.
       */
   public void setNewlineSignificant(boolean val)
   {
      newlineSignificant += (val ? 1 : -1);
   }

   /**
    * Method to compute the tag code identifying a tag name.
    * <P>
    * This is the value which is passed to ContentHandler's for reporting a
    * tag name.&nbsp; Derived class may override it.
    * Impl Note: Transforming to uppercase takes into account that the bytes are
    * in the range [0-9A-Za-z]: (ch >= 'a') means "ch is a lower case letter".
    * Also, we *do* know that the count is > 0.
    *
    * @param b
    *           byte array containing the bytes to be hashed
    * @param offset
    *           position of the first byte in the array
    * @param count
    *           number of bytes to be hashed
    * @return the corresponding hash code
    */
   protected int getTagCode(byte b[], int offset, int count)
   {
      int i = b[offset];
      if ('a' <= i)
         i -= ('a' - 'A'); // fast toUpper
      while (--count > 0)
      {
         byte ch = b[++offset];
         if ('a' <= ch)
            ch -= ('a' - 'A'); // fast toUpper
         i = (i << 5) - i + ch;
      }
      return i;
   }

   /**
    * Override of XmlTokenizer
    */
   public void foundStartTagName(byte buffer[], int offset, int count)
   {
      switch (state)
      {
         case START_TAG_STATE:
            reportStartTag();
            break;
         case PCDATA_STATE:
            reportData(false);
            break;
      }
      tagNameHashId = getTagCode(buffer, offset, count);
      state = START_TAG_STATE;
   }

   /**
    * Override of XmlTokenizer
    */
   public void foundEndTagName(byte buffer[], int offset, int count)
   {
      switch (state)
      {
         case START_TAG_STATE:
            reportStartTag();
            break;
         case PCDATA_STATE:
             // Should have been: reportData(true). | Alas: the (badly marked
             // up) "once <i>upon </i>a time" | would be seen as "once upon a time"
            reportData(false);
            break;
      }
      tagNameHashId = getTagCode(buffer, offset, count);
      cntHandler.endElement(tagNameHashId);
      state = END_TAG_STATE;
   }

   /**
    * Override of XmlTokenizer
    */
   public final void foundEndEmptyTag()
   {
      reportStartTag();
      cntHandler.endElement(tagNameHashId); // <BR/> is like "<BR></BR>"
      state = SPRING_STATE;
   }

   /**
    * Override of XmlTokenizer
    */
   public final void foundCharacterData(byte buffer[], int offset, int count)
   {
      if (state == START_TAG_STATE)
         reportStartTag();
      storeData(buffer, offset, count, (state == START_TAG_STATE) || (state == SPRING_STATE));
      state = (pcdata.length() > 0) ? PCDATA_STATE : SPRING_STATE;
   }

   /**
    * Override of XmlTokenizer
    * Impl Note: this assumes the found character is encoded in ISO 8859-1
    * later, we will need the appropriate encoder
    */
   public final void foundCharacter(char charFound)
   {
      if (state == START_TAG_STATE)
         reportStartTag();
      pcdata.append(charFound); // kcchan@554_39
      state = PCDATA_STATE;
   }

   /** Override of XmlTokenizer */
   public final void foundAttributeName(byte buffer[], int offset, int count)
   {
      flushAttribute();
      attrName = new String(buffer, offset, count);
   }

   /** Override of XmlTokenizer */
   public final void foundAttributeValue(byte buffer[], int offset, int count, byte dlm)
   {
      attList.addAttribute(attrName, new String(buffer, offset, count), dlm);
      attrName = null;
   }

   /** Override of XmlTokenizer */
   public final void foundComment(byte buffer[], int offset, int count)
   {
      switch (state)
      {
         case START_TAG_STATE:
            reportStartTag();
            break;
         case PCDATA_STATE:
            reportData(false);
            break;
      }
      cntHandler.comment(new String(buffer, offset, count));
      state = SPRING_STATE;
   }

   /** Override of XmlTokenizer */
   public final void foundEndOfInput(int count)
   {
      switch (state)
      {
         case START_TAG_STATE:
            reportStartTag();
            break;
         case PCDATA_STATE:
            reportData(true);
            break;
      }
   }
   
   /**
    * Override of XmlTokenizer
    * 
    * @since TotalCross 1.27
    */
   protected void foundDeclaration(byte[] input, int offset, int count)
   {
      flushAttribute();
      cntHandler.startElement(tagNameHashId, attList);
      attList.clear();

      if (count > 7 && new String(totalcross.sys.Convert.charConverter.bytes2chars(input, offset, 7)).equals("[CDATA["))
         cntHandler.cdata(tagNameHashId,
               new String(totalcross.sys.Convert.charConverter.bytes2chars(input, offset + 7, count - 9)));
   }

    /**
       * Store PCDATA in our ByteArrayStream. White spaces found inside the byte
       * array <code>input</code> are replaced by one space (' ').
       * <P>
       * If "stripLeadingSpace" is on, leading spaces are removed. Trailing
       * spaces are not removed.
       * <p>
       * When the string is made only of 1 or more white spaces, the returned
       * byte array is empty (length == 0) if stripLeadingSpace is true, or is
       * made of exactly one space if stripLeadingSpace is false.
       * <p>
       * Impl Note: This applies the following HTML Rules: - the parser treats \r
       * and \r\n and \n as newlines - newlines, when relevant, are seen as spaces -
       * newlines after start tags and before end tags are ignored (this is a rough
       * simplification of SGML rules) - in HTML, 2 or more spaces are coalesced in
       * one space except if "significantWhiteSpace" is required, as for the
       * &lt;PRE> tag
       *
       * <PRE>
       * Examples: &quot; AB\r\nC &quot; -&gt; &quot; AB C &quot; with stripLeadingSpace set to false: &quot; AB\r\nC &quot; -&gt; &quot;AB C &quot; with
       * stripLeadingSpace set to true; &quot;A\tB CD&quot; -&gt; &quot;A B CD&quot;
       * </PRE>
       *
       * @param input
       *           the byte array that must be ws-coalesced
       * @param offset
       *           position of the first byte to ws-coalesce in the array
       * @param count
       *           number of bytes to ws-coalesce
       */
   private void storeData(byte input[], int offset, int count, boolean stripLeadingSpaces)
   {
      if (newlineSignificant > 0)
      {
         if (stripLeadingSpaces)
         {
            while ((count > 0) && (input[offset] & 0xFF) <= ' ')
            {
               --count;
               ++offset;
            }
         }
         if (count > 0)
            pcdata.append(totalcross.sys.Convert.charConverter.bytes2chars(input, offset, count)); // kcchan@554_39
      }
      else
      {
         int from = offset - 1;
         if ((!stripLeadingSpaces) && (count > 0) && (input[offset] & 0xFF) <= ' ')
            pcdata.append(' '); // kcchan@554_39
         ++count;
         while (--count > 0)
         {
            if ((input[++from]&0xFF) > ' ')
            {
               int fromOrig = from++;
               while ((--count > 0) && (input[from] & 0xFF) > ' ')
                  ++from;
               pcdata.append(totalcross.sys.Convert.charConverter.bytes2chars(input, fromOrig, from - fromOrig)); // kcchan@554_39
               if (count == 0)
                  break;
               else
                  pcdata.append(' '); // kcchan@554_39
            }
         }
      }
   }

   /**
    * Called when an entire chunk of PCDATA has been recognized.
    * 
    * @param stripTrailingSpace
    *           if false, the data will be followed by a space had the source data ended with one or more white spaces;
    *           if true, the data ends at the last non-space character.
    */
   private void reportData(boolean stripTrailingSpaces)
   {
      int count = pcdata.length(); // kcchan@554_39
      if (count > 0)
      {
         String s = pcdata.toString();
         if (stripTrailingSpaces)
            s = s.trim();
         cntHandler.characters(s);
         pcdata.setLength(0);
      }
   }

   /**
    * Called when an attribute had no value.
    * <P>
    * Note that an attribute name not followed by a value assignment (Ex: <code>&gt;dl compact&gt;</code> is perfectly
    * legal HTML/SGML.
    */
   private void flushAttribute()
   {
      if (attrName != null)
      {
         attList.addAttribute(attrName, "", (byte) 0);
         attrName = null;
      }
   }

   /**
    * Called when a start tag has been completely tokenized.
    */
   private void reportStartTag()
   {
      flushAttribute();
      cntHandler.startElement(tagNameHashId, attList);
      attList.clear();
   }
}
