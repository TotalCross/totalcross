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

import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.Vm;

/**
 *
 * A Tokenizer for XML input. In non-strict mode (default), it recognizes
 * HTML constructs as well, <i>e.g.:</i> unquoted attributes value,
 * unterminated references, etc.
 * <P>
 * Four "tokenize" methods are provided: one takes a byte[] array; another
 * takes a byte[] array with offset and count; another one for an HTML document which is embedded within an HTTP stream;
 * and the last takes a (byte) Stream.
 * <P>
 * Tokenization events are reported via overridable methods:
 * <UL>
 * <LI>foundStartOfInput
 * <LI>foundStartTagName
 * <LI>foundEndTagName
 * <LI>foundEndEmptyTag
 * <LI>foundCharacterData
 * <LI>foundCharacter
 * <LI>foundAttributeName
 * <LI>foundAttributeValue
 * <LI>foundComment
 * <LI>foundProcessingInstruction
 * <LI>foundDeclaration
 * <LI>foundReference
 * <LI>foundEndOfInput
 * </UL>
 * </P>
 * <P>
 * Some of these methods pass the parameters pertinent to the kind of
 * tokenized events: tag name, attribute name and value...&nbsp; These values
 * are only valid for the time the event is reported.&nbsp; Never assume
 * that, after returning from a "foundXxx" method, the information that was
 * reported is still available! Persistent values are however provided
 * through the "getAbsoluteOffset()" method, which returns the absolute
 * offset of the current parameters of the foundXxxx method.
 * </P>
 *
 * <P>
 * <U>Typical invocation</U>
 * </P>
 *
 * <PRE>
 * class XmlTokenizerTest
 * {
 *    static class MyXmlTokenizer extends XmlTokenizer
 *    {
 *       public void foundStartOfInput(byte buffer[], int offset, int count)
 *       {
 *          Vm.debug(&quot;Start: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundStartTagName(byte buffer[], int offset, int count)
 *       {
 *          Vm.debug(&quot;StartTagName: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundEndTagName(byte buffer[], int offset, int count)
 *       {
 *          Vm.debug(&quot;EndTagName: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundEndEmptyTag()
 *       {
 *          Vm.debug(&quot;EndEmptyTag&quot;);
 *       }
 *
 *       public void foundCharacterData(byte buffer[], int offset, int count)
 *       {
 *          Vm.debug(&quot;Content: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundCharacter(char charFound)
 *       {
 *          Vm.debug(&quot;Content Ref  |&quot; + charFound + '|');
 *       }
 *
 *       public void foundAttributeName(byte buffer[], int offset, int count)
 *       {
 *          Vm.debug(&quot;AttributeName: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundAttributeValue(byte buffer[], int offset, int count, byte dlm)
 *       {
 *          Vm.debug(&quot;AttributeValue: &quot; + new String(buffer, offset, count));
 *       }
 *
 *       public void foundEndOfInput(int count)
 *       {
 *          Vm.debug(&quot;Ended: &quot; + count + &quot; bytes parsed.&quot;);
 *       }
 *    }
 *
 *    public static void testMe()
 *    {
 *       String input = &quot;&lt;p&gt;Hello&lt;i&gt;World!&lt;/i&gt;&lt;/p&gt;&quot;;
 *       MyXmlTokenizer xtk = new MyXmlTokenizer();
 *       try
 *       {
 *          xtk.tokenize(input.getBytes());
 *       }
 *       catch (SyntaxException ex)
 *       {
 *          Vm.debug(ex.getMessage());
 *       }
 *    }
 * }
 * </PRE>
 *
 * <P>
 * <U>Note:</U> A Tokenizer is not a Parser.&nbsp; The correctness of the
 * tag structure (stack) is not examined. <BR/> Ex: the dangling markup
 * "&lt;foo>&lt;bar>opop&lt;/foo>" is syntactically valid. <BR/> As
 * a result, a Tokenizer can work on document fragments.
 */
public class XmlTokenizer {
  private int ofsStart;
  private int ofsCur;
  private int ofsEnd;
  private int readPos;
  private int state;
  private int substate;
  private byte[] endTagToSkipTo;
  private int ixEndTagToSkipTo;
  private byte quote;
  private boolean strictlyXml;
  private boolean resolveCharRef;

  // XML Predefined Named References
  private static final byte chrRef[][] = { { (byte) '<', (byte) 'l', (byte) 't' },
      { (byte) '>', (byte) 'g', (byte) 't' }, { (byte) '&', (byte) 'a', (byte) 'm', (byte) 'p' },
      { (byte) '\'', (byte) 'a', (byte) 'p', (byte) 'o', (byte) 's' },
      { (byte) '"', (byte) 'q', (byte) 'u', (byte) 'o', (byte) 't' } };

  // Was class XmlByteType.  Moved here for optim and footprint.
  // (no one but the Tokenizer is supposed to use this class!)
  private static final byte is[] = new byte[256];
  private static final byte ISNAMESTART = 1 << 0;
  private static final byte ISNAMEFOLLOWER = 1 << 1;
  private static final byte ISSPACE = 1 << 2;
  private static final byte ISQUOTE = 1 << 3;
  private static final byte ISCONTENTDLM = 1 << 4;
  private static final byte ISENDTAGDLM = 1 << 5;
  private static final byte ISENDREFERENCE = 1 << 6;

  static {
    byte isNameStartOrFollower = (byte) (ISNAMESTART | ISNAMEFOLLOWER);
    Convert.fill(is, 'a', 'z' + 1, isNameStartOrFollower);
    Convert.fill(is, 'A', 'Z' + 1, isNameStartOrFollower);
    Convert.fill(is, '0', '9' + 1, ISNAMEFOLLOWER);
    is['_'] = isNameStartOrFollower;
    is[':'] = isNameStartOrFollower;
    is['-'] = ISNAMEFOLLOWER;
    is['.'] = ISNAMEFOLLOWER;
    is[' '] = ISSPACE;
    is['\r'] = ISSPACE;
    is['\n'] = ISSPACE;
    is['\t'] = ISSPACE;
    is['\f'] = ISSPACE;
    is['\''] = ISQUOTE;
    is['\"'] = ISQUOTE;
    is['>'] = ISENDTAGDLM;
    is['<'] = ISCONTENTDLM;
    is['&'] = ISCONTENTDLM;
    is[';'] = ISENDREFERENCE;
  }

  protected XmlTokenizer() {
    resolveCharRef = true;
  }

  /**
   * Tokenize an array of bytes.
   *
   * @param input
   *           byte array to tokenize
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes to tokenize
   * @exception SyntaxException
   */
  public final void tokenize(byte input[], int offset, int count) throws SyntaxException {
    ofsStart = 0;
    ofsCur = offset;
    ofsEnd = count;
    readPos = offset;
    state = 0;
    foundStartOfInput(input, offset, count);
    tokenizeBytes(input);
    endTokenize(input);
  }

  /**
   * Tokenize an array of bytes.
   *
   * @param input
   *           byte array to tokenize
   * @exception SyntaxException
   */
  public final void tokenize(byte input[]) throws SyntaxException {
    tokenize(input, 0, input.length);
  }

  /**
   * Tokenize a stream
   *
   * @param input
   *           stream to tokenize
   * @exception SyntaxException
   * @throws totalcross.io.IOException
   */
  public final void tokenize(Stream input) throws SyntaxException, totalcross.io.IOException {
    byte buffer[] = new byte[1024];
    tokenize(input, buffer, 0, input.readBytes(buffer, 0, buffer.length), 0);
  }

  /**
   * Tokenize an already buffered Stream.
   * <P>
   * Versus the general method above, this tokenize method requires more
   * arguments. It should be used when the HTML document is embedded within
   * an HTTP stream.
   *
   * @param input
   *           stream to tokenize
   * @param buffer
   *           buffer already filled with bytes read from the input stream
   * @param start
   *           starting position in the buffer
   * @param end
   *           ending position in the buffer
   * @param pos
   *           read position of the byte at offset 0 in the buffer
   * @exception SyntaxException
   * @throws totalcross.io.IOException
   */
  public final void tokenize(Stream input, byte[] buffer, int start, int end, int pos)
      throws SyntaxException, totalcross.io.IOException {
    ofsStart = start;
    ofsCur = start;
    ofsEnd = end;
    readPos = pos;
    state = 0;
    foundStartOfInput(buffer, 0, ofsEnd);
    while (ofsCur < ofsEnd) {
      tokenizeBytes(buffer); // returns when ofsCur == ofsEnd
      if (ofsEnd == buffer.length) {
        // no more room
        if (ofsStart > 0) {
          // tidy is still possible
          Vm.arrayCopy(buffer, ofsStart, buffer, 0, ofsEnd - ofsStart);
          readPos += ofsStart;
          ofsCur -= ofsStart;
          ofsStart = 0;
        } else if (((state == 10) || (state == 22)) && (ofsCur > 0)) {
          // "Data" mode: flush
          foundCharacterData(buffer, 0, ofsCur);
          Vm.arrayCopy(buffer, 0, buffer, 0, ofsEnd - ofsCur);
          readPos += ofsCur;
          ofsCur = ofsStart = 0;
        } else {
          // nothing else to do than to extend
          byte oldBuffer[] = buffer;
          int newSize = oldBuffer.length * 15 / 10; // guich@510_17: instead of double, grow 50%...
          buffer = new byte[newSize];
          Vm.arrayCopy(oldBuffer, 0, buffer, 0, ofsEnd);
        }
      }
      if (ofsCur >= ofsEnd) {
        break;
      }
      ofsEnd = ofsCur + input.readBytes(buffer, ofsCur, buffer.length - ofsCur);
    }
    endTokenize(buffer);
  }

  /**
   * Resolve a numeric or named character reference. See <a
   * href=http://www.w3.org/TR/REC-xml#sec-predefined-ent>XML Predefined
   * Entities</a>
   *
   * @param input
   *           byte array which describes the reference
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes of the reference
   * @return the resulting character, or '&#x5C;uffff' (not a unicode
   *         character) if the conversion could not be done
   */
  public static final char resolveCharacterReference(byte input[], int offset, int count) {
    if ((count > 1) && (input[offset] == '#')) {
      if ((input[++offset] == 'x') || (input[offset] == 'X')) {
        return hex2char(input, offset + 1, count - 2);
      } else {
        return dec2char(input, offset, count - 1);
      }
    } else {
      return ref2char(input, offset, count);
    }
  }

  /**
   * Get the absolute offset of the data parameters of the currently
   * reported event.
   *
   * @return the absolute offset of the data parameters of the currently
   *         reported event.
   */
  public final int getAbsoluteOffset() {
    return ofsStart + readPos;
  }

  /**
   * Declare the input to be CDATA, until the end tag of the element
   * <code>tagName</code> is found.
   * <P>
   * This settings permits to handle character data.&nbsp; For example, when
   * the &lt;Script&gt; tag is reported the derived class call this method:
   * <code>skipToEndOf("SCRIPT");</code> before to return.&nbsp; From this
   * point, all input is reported as data until <code>&lt;/SCRIPT&gt;</code>is
   * found.
   * <P>
   * <U>Note:</U> The Tokenizer is a low level class and does not register
   * the tag name. Therefore, this method must be called at each time the
   * caller wants to suprress markup recognition until the end tag is
   * found.&nbsp;
   *
   * @param input
   *           byte array containing the name of the element the end tag of
   *           which ends the character data
   * @param offset
   *           position of the first character in the array
   * @param count
   *           number of relevant bytes
   */
  protected final void setCdataContents(byte input[], int offset, int count) {
    endTagToSkipTo = new byte[count];
    for (int i = 0; i < count; ++i) {
      byte b = input[offset + i];
      if ('a' <= b) {
        b -= ('a' - 'A'); // fast toUpper
      }
      endTagToSkipTo[i] = b;
    }
  }

  /**
   * Tell if the data which is currently reported by foundCharacterData is
   * <code>CDATA</code> versus <code>PCDATA</code>.
   * <P>
   * In ISO 8879 (SGML) terminology, <code>CDATA</code> describes
   * &quot;non displayable&quot; data, as, for instance, data that is the
   * contents of a <code>SCRIPT</code> element.&nbsp; It differs from
   * &quot;regular data&quot; as, for instance, data that is the contents of
   * a <code>P</code> element is named <code>PCDATA</code> (Parsed
   * Character Data)
   */
  public final boolean isDataCDATA() {
    return (endTagToSkipTo != null);
  }

  /**
   * Set or unset the strict XML mode of the parser.
   * <P>
   * By default, the parser will allow most commonly used HTML constructs.
   *
   * @param toSet
   *           if true, set the strict XML mode; if false, allows HTML
   *           constructs.
   */
  public final void setStrictlyXml(boolean toSet) {
    strictlyXml = toSet;
  }

  /**
   * Turn off or on the automatic resolution of references.
   * <P>
   * References are normally solved, and reported via
   * {@link XmlTokenizer#foundCharacter(char)}.&nbsp; When automatic
   * resolution is turned off,
   * {@link XmlTokenizer#foundReference(byte[],int,int)} is called
   * instead.&nbsp; By default, automatic resolution of references is <u>on</u>,
   * and {@link XmlTokenizer#foundReference(byte[],int,int)} is not called.
   * <P>
   * This option should be set before starting the tokenization.&nbsp; See
   * {@link XmlTokenizer#foundReference(byte[],int,int)} for more details.
   *
   * @param disable
   *           boolean: if <code>true</code> automatic resolution of
   *           references is turned off, otherwise, it is turned on.
   */
  public final void disableReferenceResolution(boolean disable) {
    resolveCharRef = !disable;
  }

  /**
   * Method called before to start tokenizing.
   * <P>
   * Derived class may override this method, for doing whatever appropriate
   * housekeeping (sniffing at the encoding, etc.)
   *
   * @param input
   *           byte array containing the first bytes of the input about to
   *           be tokenized
   * @param offset
   *           position of the first byte to be tokenized
   * @param count
   *           number of bytes to be tokenized
   */
  protected void foundStartOfInput(byte input[], int offset, int count) {
  }

  /**
   * Method called when a start-tag has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the name of the tag that started
   * @param offset
   *           position of the first character of the tag name in the array
   * @param count
   *           number of bytes the tag name is made of
   */
  protected void foundStartTagName(byte input[], int offset, int count) {
  }

  /**
   * Method called when an end-tag has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the name of the tag that ended
   * @param offset
   *           position of the first character of the tag name in the array
   * @param count
   *           number of bytes the tag name is made of
   */
  protected void foundEndTagName(byte input[], int offset, int count) {
  }

  /**
   * Method called when an empty-tag has been found.
   * <P>
   * This method is called just after all events related to the starting tag
   * have been reported. The implied tag name is the one of the starting tag (<i>e.g.:</i>
   * the most recently reported start tag.)
   * <P>
   * Derived class may override this method.
   * <P> Example:
   * <PRE>
   * 
   *   &lt;FOO A=B&gt; generates:
   *   - foundStartTagName(&quot;FOO&quot;);
   *   - foundAttributeName(&quot;A&quot;);
   *   - foundAttributeValue(&quot;B&quot;);
   *   - foundEndEmptyTag();
   * </PRE>
   *
   */
  protected void foundEndEmptyTag() {
  }

  /**
   * Method called when a character data content has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the character data that was found
   * @param offset
   *           position of the first character data in the array
   * @param count
   *           number of bytes the character data content is made of
   */
  protected void foundCharacterData(byte input[], int offset, int count) {
  }

  /**
   * Method called when a character has been found in the contents, which is resulting from a character reference resolution.
   * <P>
   * Derived class may override this method.
   *
   * @param charFound
   *           resolved character - if the character is invalid, this value
   *           is set to '&#x5C;uffff', which is not a unicode character.
   * @see XmlTokenizer#foundReference(byte[],int,int)
   */
  protected void foundCharacter(char charFound) {
  }

  /**
   * Method called when an attribute name has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the attribute name
   * @param offset
   *           position of the first character of the attribute name in the
   *           array
   * @param count
   *           number of bytes the attribute name is made of
   */
  protected void foundAttributeName(byte input[], int offset, int count) {
  }

  /**
   * Method called when an attribute value has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the attribute value
   * @param offset
   *           position of the first character of the attribute value in the
   *           array
   * @param count
   *           number of bytes the attribute value is made of
   * @param dlm
   *           delimiter that started the attribute value (' or "). '\0' if
   *           none
   */
  protected void foundAttributeValue(byte input[], int offset, int count, byte dlm) {
  }

  /**
   * Method called when a comment has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the comment (without the
   *           <CODE><B>&lt;!--</CODE></B> and <CODE><B>--&gt;</CODE></B>
   *           delimiters)
   * @param offset
   *           position of the first character of the comment in the array
   * @param count
   *           number of bytes the comment is made of
   */
  protected void foundComment(byte input[], int offset, int count) {
  }

  /**
   * Method called when a processing instruction has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the processing instruction (without the
   *           <CODE><B>&lt;?</CODE></B> and <CODE><B>?&gt;</CODE></B>
   *           delimiters)
   * @param offset
   *           position of the first character of the processing instruction
   *           in the array
   * @param count
   *           number of bytes the processing instruction is made of
   */
  protected void foundProcessingInstruction(byte input[], int offset, int count) {
  }

  /**
   * Method called when a declaration has been found.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the declaration (without the
   *           <CODE><B>&lt;!</CODE></B> and <CODE><B>&gt;</CODE></B>
   *           delimiters)
   * @param offset
   *           position of the first character of the declaration in the
   *           array
   * @param count
   *           number of bytes the declaration is made of
   */
  protected void foundDeclaration(byte input[], int offset, int count) {
  }

  /**
   * Method called when a reference been found in content.
   * <P>
   * It can be either a named or numeric character reference, or an entity
   * reference.&nbsp; Given the several syntaxes of reference, no
   * verification is made <i>a priori</i> on the validity of the "name" of
   * the reference.
   * <P>
   * For conveniency, a static method:
   * {@link XmlTokenizer#resolveCharacterReference(byte[],int,int)} allows
   * to convert the character reference into its UCS-2 encoded value.
   * </P>
   * <TABLE cellpadding=0 cellspacing=0>
   * <TR VALIGN=top>
   * <TD><B>Note:</B></TD>
   * <TD>&nbsp;</TD>
   * <TD>
   * <TD><code>foundReference</code> is called only if
   * {@link XmlTokenizer#disableReferenceResolution(boolean disable)} has
   * been called first, with <code>disable</code> set to <code>true</code>.&nbsp;
   * If not, then <code>foundReference</code> is <U>never called</U>, and
   * {@link XmlTokenizer#foundCharacter(char)} is called instead.&nbsp; This
   * design permits to easily handle simple XML documents &mdash; only
   * predefined named character entities, and numeric character entities
   * &mdash; and documents which have user-defined internal/external
   * entities.&nbsp; This is explained below. </TD>
   * </TR>
   * </TABLE>
   * <P>
   * When working with a set of externally defined entities, issue
   * <code>disableReferenceResolution(true)</code> to turn off automatic
   * reference resolution. Then, your code in <code>foundReference</code>
   * could make a quick check to see if the found reference is
   * numeric.&nbsp; If it is numeric &mdash; it starts with a <code>#</code>
   * character &mdash; call <i>resolveCharacterReference</i>; if it is not
   * a numeric reference, checks if the reference belongs to the known list
   * of defined entities for the parsed document.&nbsp; If it does, do the
   * substitution; if not, call <i>resolveCharacterReference</i>, because
   * it could be one of the <a
   * href=http://www.w3.org/TR/REC-xml#sec-predefined-ent> XML Predefined
   * Entities</a>
   * <P>
   * By default, each character reference is naturally reported via
   * {@link XmlTokenizer#foundCharacter(char)}, which, again, <u>supersedes</u>
   * the <code>foundReference</code> notification.
   *
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the reference name
   * @param offset
   *           position of the first character of the reference name in the
   *           array
   * @param count
   *           number of bytes the reference name is made of
   * @see XmlTokenizer#setStrictlyXml(boolean toSet)
   */
  protected void foundReference(byte input[], int offset, int count) {
  }

  /**
   * Method called when invalid data was found. This is often due to a bad
   * tag syntax.
   * <P>
   * Derived class may override this method.
   *
   * @param input
   *           byte array containing the invalid data
   * @param offset
   *           position of the first character of the invalid data in the
   *           array
   * @param count
   *           number of bytes the invalidData is made of
   */
  protected void foundInvalidData(byte input[], int offset, int count) {
  }

  /**
   * Method called when the end of the input was found, and the tokenization is
   * about to end.
   * <P>
   * Derived class may override this method.
   *
   * @param count
   *           number of bytes parsed
   */
  protected void foundEndOfInput(int count) {
  }

  /**
   * Private method to tokenize a bunch of bytes. It returns when no bytes
   * are available, but can be resumed again with more bytes to parse
   *
   * @param input
   *           byte array to parse
   * @exception SyntaxException
   */
  private void tokenizeBytes(byte input[]) throws SyntaxException {
    while (ofsCur < ofsEnd) {
      int ch = (int) input[ofsCur] & 0xFF;
      switch (state) {
      case 0:
        ofsStart = ofsCur;
        if (endTagToSkipTo != null) {
          state = 22;
          continue; // same ofsCur!!! it can start </script>
        } else if (ch == '<') {
          state = 1;
        } else if (ch == '&') {
          state = 11;
        } else {
          state = 10;
        }
        break;
      case 1:
        if ((is[ch] & ISNAMESTART) != 0) {
          state = 2;
        } else if (ch == '/') {
          state = 12;
        } else if (ch == '!') {
          state = 16;
        } else if (ch == '?') {
          state = 20;
          substate = 0; // so we wait for "?>"
        } else if (!strictlyXml) {
          state = 10; // recovery: process "<$xxx" as data
        } else {
          endTokenize(input); // strictly XML: give up
        }
        break;
      case 2:
        while ((is[ch] & ISNAMEFOLLOWER) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ch == '>') {
          state = 0;
        } else if (ch == '/') {
          state = 9;
        } else if ((is[ch] & ISSPACE) != 0) {
          state = 3;
        } else if (!strictlyXml) {
          // <ABC$xxx
          state = 10; // recovery: process "<ABC$xxx" as data
          break;
        } else {
          endTokenize(input); // strictly XML: give up
        }
        foundStartTagName(input, ofsStart + 1, ofsCur - ofsStart - 1);
        break;
      case 3:
        while ((is[ch] & ISSPACE) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ch == '>') {
          state = 0;
        } else if (ch == '/') {
          state = 9;
        } else if ((is[ch] & ISNAMESTART) != 0) {
          ofsStart = ofsCur;
          state = 4;
        } else {
          state = 21; // possible recovery: skip to TAGC
        }
        break;
      case 4:
        while ((is[ch] & ISNAMEFOLLOWER) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if ((is[ch] & ISSPACE) != 0) {
          state = 5;
        } else if (ch == '=') {
          state = 6;
        } else if (!strictlyXml && (ch == '>')) {
          state = 0; // <list compact> allowed in HTML
        } else {
          state = 21; // possible recovery: skip to TAGC
          break;
        }
        foundAttributeName(input, ofsStart, ofsCur - ofsStart);
        break;
      case 5:
        while ((is[ch] & ISSPACE) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ch == '=') {
          state = 6;
        } else if (!strictlyXml) {
          if (ch == '>') {
            state = 0; // <list compact > allowed in HTML
          } else if ((is[ch] & ISNAMESTART) != 0) {
            ofsStart = ofsCur;
            state = 4; // <list compact simple> allowed in HTML
          } else {
            state = 21; // possible recovery: skip to TAGC
          }
        } else {
          state = 21; // possible recovery: skip to TAGC
        }
        break;
      case 6:
        while ((is[ch] & ISSPACE) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if ((is[ch] & ISQUOTE) != 0) {
          quote = (byte) ch;
          ofsStart = ofsCur;
          state = 7;
        } else if (!strictlyXml) {
          if (ch == '>') {
            state = 0;
          } else {
            ofsStart = ofsCur;
            state = 15;
          }
        } else {
          endTokenize(input); // strictly XML: give up
        }
        break;
      case 7:
        while (ch != quote) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        ++ofsStart;
        foundAttributeValue(input, ofsStart, ofsCur - ofsStart, quote);
        state = 8;
        break;
      case 8:
        if (ch == '>') {
          state = 0;
        } else if (ch == '/') {
          state = 9;
        } else if ((is[ch] & ISSPACE) != 0) {
          state = 3;
        } else if ((is[ch] & ISNAMESTART) != 0) {
          ofsStart = ofsCur;
          state = 4;
        } else {
          state = 21; // possible recovery: skip to TAGC
        }
        break;
      case 9:
        if (ch != '>') {
          state = 21; // possible recovery: skip to TAGC
        } else {
          foundEndEmptyTag();
          state = 0;
        }
        break;
      case 10:
        while ((is[ch] & ISCONTENTDLM) == 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ofsCur > ofsStart) {
          foundCharacterData(input, ofsStart, ofsCur - ofsStart);
        }
        ofsStart = ofsCur;
        if (ch == '<') {
          state = 1;
        } else {
          state = 11;
        }
        break;
      case 11:
        while ((is[ch] & (ISCONTENTDLM | ISSPACE | ISENDREFERENCE)) == 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        tellReference(input, ofsStart + 1, ofsCur - ofsStart - 1);
        if (ch == ';') {
          ofsStart = ofsCur + 1; // data starts at next byte
          state = 10;
        } else if (!strictlyXml) {
          ofsStart = ofsCur;
          if (ch == '<') {
            state = 1;
          } else if (ch != '&') {
            // spaces (else '&' again, stay here)
            state = 10;
          }
        } else {
          endTokenize(input); // strictly XML: give up
        }
        break;
      case 12:
        if ((is[ch] & ISNAMESTART) != 0) {
          state = 13;
        } else if (!strictlyXml) {
          state = 10; // recovery: process "</$xxx" as data
        } else {
          endTokenize(input); // strictly XML: give up
        }
        break;
      case 13:
        while ((is[ch] & ISNAMEFOLLOWER) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ch == '>') {
          state = 0;
        } else if ((is[ch] & ISSPACE) != 0) {
          state = 14;
        } else if (!strictlyXml) {
          state = 10; // recovery: process "</xxx$" as data
          break;
        } else {
          endTokenize(input); // strictly XML: give up
        }
        foundEndTagName(input, ofsStart + 2, ofsCur - ofsStart - 2);
        break;
      case 14:
        while ((is[ch] & ISSPACE) != 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        if (ch == '>') {
          state = 0;
        } else {
          state = 21; // possible recovery: skip to TAGC
        }
        break;
      case 15: // !strictlyXml
        while ((is[ch] & (ISSPACE | ISENDTAGDLM)) == 0) {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        foundAttributeValue(input, ofsStart, ofsCur - ofsStart, (byte) 0);
        if (ch == '>') {
          state = 0;
        } else {
          state = 3;
        }
        break;
      case 16:
        ofsStart = ofsCur;
        if (ch == '-') {
          state = 18;
        } else {
          state = 17;
        }
        break;
      case 17:
        while (ch != '>') {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        foundDeclaration(input, ofsStart, ofsCur - ofsStart);
        state = 0;
        break;
      case 18:
        if (ch == '-') {
          ofsStart = ofsCur;
          state = 19;
          substate = 0; // so we wait for "-->"
        } else {
          // keep ofsStart unchanged!
          state = 17;
        }
        break;
      case 19:
        switch (substate) {
        case 0:
          while (ch != '-') {
            if (++ofsCur >= ofsEnd) {
              return;
            }
            ch = (int) input[ofsCur] & 0xFF;
          }
          substate = 1;
          break;
        case 1: // '-' found
          if (ch != '-') {
            substate = 0;
          } else {
            substate = 2;
          }
          break;
        case 2: // '-'('-')+ found
          while (ch == '-') {
            if (++ofsCur >= ofsEnd) {
              return;
            }
            ch = (int) input[ofsCur] & 0xFF;
          }
          if (ch == '>') {
            foundComment(input, ofsStart + 1, ofsCur - ofsStart - 3);
            ofsStart = ofsCur;
            state = 0;
          } else if (ch == '!') {
            substate = 3;
          } else {
            substate = 0;
          }
          break;
        case 3:
          if (ch == '>') {
            foundComment(input, ofsStart + 1, ofsCur - ofsStart - 4);
            ofsStart = ofsCur;
            state = 0;
          } else {
            substate = 0;
          }
          break;
        }
        break;
      case 20:
        switch (substate) {
        case 0:
          while (ch != '?') {
            if (++ofsCur >= ofsEnd) {
              return;
            }
            ch = (int) input[ofsCur] & 0xFF;
          }
          substate = 1;
          break;
        case 1:
          if (ch == '>') {
            foundProcessingInstruction(input, ofsStart + 2, ofsCur - ofsStart - 3);
            ofsStart = ofsCur;
            state = 0;
          } else {
            substate = 0;
          }
          break;
        }
        break;
      case 21: // Skip to TAGC
        if (strictlyXml) {
          endTokenize(input); // strictly XML: give up
        } else {
          ofsStart = ofsCur;
          while (ch != '>') {
            if (++ofsCur >= ofsEnd) {
              return;
            }
            ch = (int) input[ofsCur] & 0xFF;
          }
          foundInvalidData(input, ofsStart, ofsCur - ofsStart);
          state = 0;
        }
        break;
      case 22: // skip to end tag (SCRIPT contents)
        while (ch != '<') {
          if (++ofsCur >= ofsEnd) {
            return;
          }
          ch = (int) input[ofsCur] & 0xFF;
        }
        state = 23;
        break;
      case 23:
        if (ch != '/') {
          state = 22;
        } else {
          state = 24;
          ixEndTagToSkipTo = 0;
        }
        break;
      case 24:
        if (ixEndTagToSkipTo == endTagToSkipTo.length) {
          int ofsTemp = ofsCur - ixEndTagToSkipTo - 2;
          ;
          if (ch == '>') {
            state = 0;
          } else if ((is[ch] & ISSPACE) != 0) {
            state = 14;
          } else {
            state = 22;
            break; // abandon here
          }
          foundCharacterData(input, ofsStart, ofsTemp - ofsStart);
          ofsStart = ofsTemp;
          foundEndTagName(input, ofsTemp + 2, ixEndTagToSkipTo);
          endTagToSkipTo = null;
        } else {
          if ('a' <= ch) {
            ch -= ('a' - 'A'); // fast toUpper
          }
          if (endTagToSkipTo[ixEndTagToSkipTo++] != (byte) ch) {
            state = 22;
          }
        }
        break;
      }
      ++ofsCur;
    }
  }

  /**
   * Private method to check the state when input ends. Reason is that we
   * don't do "non-SGML characters",
   * Impl. note: This method is also called when an invalid character is found.
   * Reason is that the parse is OK when it ends either on ">" or data. For the
   * former, any character is valid (we don't do non-SGML characters, remember
   * that we work on byte, not on encoded characters.) For the latter, '>'
   * can't be an invalid character.
   *
   * @param input
   *           current buffer
   */
  private void endTokenize(byte[] input) throws SyntaxException {
    switch (state) {
    case 0:
      break;
    case 10:
      if (ofsCur > ofsStart) {
        foundCharacterData(input, ofsStart, ofsCur - ofsStart);
      }
      break;
    case 11:
      if (!strictlyXml) {
        tellReference(input, ofsStart, ofsCur - ofsStart);
        break;
      }
      /* fall thru */
    default:
      throw new SyntaxException(state, ofsCur + readPos);
    }
    foundEndOfInput(ofsCur + readPos);
  }

  /**
   * Method called when a reference been found in content.
   *
   * @param input
   *           byte array which describes the reference
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes of the reference
   */
  private void tellReference(byte[] input, int offset, int count) throws SyntaxException {
    if (resolveCharRef) {
      char res = resolveCharacterReference(input, offset, count);
      if (strictlyXml && (res == '\uffff')) {
        throw new SyntaxException(state, ofsCur + readPos);
      }
      foundCharacter(res);
    } else {
      foundReference(input, offset, count);
    }
  }

  /**
   * Convert a byte array of hexadecimal digits into a UCS-2 encoded Unicode
   * character.
   *
   * @param input
   *           byte array to convert
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes to convert
   * @return the resulting character, or '&#x5C;uffff' (not a unicode
   *         character) if the conversion could not be done
   */
  private static char hex2char(byte[] input, int offset, int count) {
    char res = 0;
    if ((count > 0) && (count <= 4)) {
      while (true) {
        char c = (char) (input[offset++]);
        if (c <= '9') {
          if (c < '0') {
            break;
          }
          res += (c & 0xF);
        } else {
          if ((c = (char) ((c & ~('a' - 'A')) - 'A')) >= (char) (16 - 10)) {
            break;
          }
          res += (c + 10);
        }
        if (--count == 0) {
          return res;
        }
        res <<= 4;
      }
    }
    return '\uffff';
  }

  /**
   * Convert a byte array of decimal digits into a UCS-2 encoded Unicode
   * character.
   *
   * @param input
   *           byte array to convert
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes to convert
   * @return the resulting character, or '&#x5C;uffff' (not a unicode
   *         character)
   */
  private static char dec2char(byte[] input, int offset, int count) {
    char res = 0;
    if (count > 0) {
      while (true) {
        char c = (char) (input[offset++]);
        if (c <= '9') {
          if (c < '0') {
            break;
          }
          res += (c & 0xF);
        }
        if (--count == 0) {
          return res;
        }
        if ((res >= 6553) && ((res > 6553) || (input[offset + 1] > '5'))) {
          break;
        }
        res = (char) ((res << 1) + (res << 3));
      }
    }
    return '\uffff';
  }

  /**
   * Convert a named character reference into its UCS-2 encoded Unicode
   * character value. See <a
   * href=http://www.w3.org/TR/REC-xml#sec-predefined-ent>XML Predefined
   * Entities</a>
   *
   * @param input
   *           byte array which contains the name of the reference
   * @param offset
   *           position of the first byte in the array
   * @param count
   *           number of bytes making the name of the reference
   * @return the resulting character, or '&#x5C;uffff' (not a unicode
   *         character)
   */
  private static char ref2char(byte[] input, int offset, int count) {
    ++count;
    for (int i = 0; i < chrRef.length; ++i) {
      if (chrRef[i].length == count) {
        byte b[] = chrRef[i];
        int k = offset;
        int j = 0;
        while (true) {
          if (++j == count) {
            return (char) (b[0]);
          }
          if (b[j] != input[k++]) {
            break;
          }
        }
      }
    }
    return '\uffff';
  }

  /** Returns the hashcode of the given bytes.
   * @since TotalCross 1.25
   */
  public int hashCode(byte[] input, int offset, int count) {
    int hash = 0;
    while (--count >= 0) {
      hash = (hash << 5) - hash + (int) input[offset++];
    }
    return hash;
  }
}
