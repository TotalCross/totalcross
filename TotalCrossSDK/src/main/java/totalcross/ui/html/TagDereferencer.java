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

package totalcross.ui.html;

/**
 * <code>NamedEntitiesDereferencer</code> provides an extremely fast way
 * to map the set of known HTML tags to unique integer values.
 * <P>
 * <b>Note:</b> the Elements described below comes from the HTML 4.01 DTD
 *       with IE extensions.&nbsp; Also, notice that tag names are case
 *       insensitive.
 */
public class TagDereferencer {
  /** Code identifying an unknown, possibly invalid, tag */
  public static final int UNKNOWN = 0;
  /** Code identifying a "A" tag */
  public static final int A = 1;
  /** Code identifying a "ABBR" tag */
  public static final int ABBR = 2;
  /** Code identifying a "ACRONYM" tag */
  public static final int ACRONYM = 3;
  /** Code identifying a "ADDRESS" tag */
  public static final int ADDRESS = 4;
  /** Code identifying a "APPLET" tag */
  public static final int APPLET = 5;
  /** Code identifying a "AREA" tag */
  public static final int AREA = 6;
  /** Code identifying a "B" tag */
  public static final int B = 7;
  /** Code identifying a "BASE" tag */
  public static final int BASE = 8;
  /** Code identifying a "BASEFONT" tag */
  public static final int BASEFONT = 9;
  /** Code identifying a "BDO" tag */
  public static final int BDO = 10;
  /** Code identifying a "BIG" tag */
  public static final int BIG = 11;
  /** Code identifying a "BLOCKQUOTE" tag */
  public static final int BLOCKQUOTE = 12;
  /** Code identifying a "BODY" tag */
  public static final int BODY = 13;
  /** Code identifying a "BR" tag */
  public static final int BR = 14;
  /** Code identifying a "BUTTON" tag */
  public static final int BUTTON = 15;
  /** Code identifying a "CAPTION" tag */
  public static final int CAPTION = 16;
  /** Code identifying a "CENTER" tag */
  public static final int CENTER = 17;
  /** Code identifying a "CITE" tag */
  public static final int CITE = 18;
  /** Code identifying a "CODE" tag */
  public static final int CODE = 19;
  /** Code identifying a "COL" tag */
  public static final int COL = 20;
  /** Code identifying a "COLGROUP" tag */
  public static final int COLGROUP = 21;
  /** Code identifying a "COMMENT" tag */
  public static final int COMMENT = 22;
  /** Code identifying a "DD" tag */
  public static final int DD = 23;
  /** Code identifying a "DEL" tag */
  public static final int DEL = 24;
  /** Code identifying a "DFN" tag */
  public static final int DFN = 25;
  /** Code identifying a "DIR" tag */
  public static final int DIR = 26;
  /** Code identifying a "DIV" tag */
  public static final int DIV = 27;
  /** Code identifying a "DL" tag */
  public static final int DL = 28;
  /** Code identifying a "DT" tag */
  public static final int DT = 29;
  /** Code identifying a "EM" tag */
  public static final int EM = 30;
  /** Code identifying a "EMBED" tag */
  public static final int EMBED = 31;
  /** Code identifying a "FIELDSET" tag */
  public static final int FIELDSET = 32;
  /** Code identifying a "FONT" tag */
  public static final int FONT = 33;
  /** Code identifying a "FORM" tag */
  public static final int FORM = 34;
  /** Code identifying a "FRAME" tag */
  public static final int FRAME = 35;
  /** Code identifying a "FRAMESET" tag */
  public static final int FRAMESET = 36;
  /** Code identifying a "H1" tag */
  public static final int H1 = 37;
  /** Code identifying a "H2" tag */
  public static final int H2 = 38;
  /** Code identifying a "H3" tag */
  public static final int H3 = 39;
  /** Code identifying a "H4" tag */
  public static final int H4 = 40;
  /** Code identifying a "H5" tag */
  public static final int H5 = 41;
  /** Code identifying a "H6" tag */
  public static final int H6 = 42;
  /** Code identifying a "HEAD" tag */
  public static final int HEAD = 43;
  /** Code identifying a "HR" tag */
  public static final int HR = 44;
  /** Code identifying a "HTML" tag */
  public static final int HTML = 45;
  /** Code identifying a "I" tag */
  public static final int I = 46;
  /** Code identifying a "IFRAME" tag */
  public static final int IFRAME = 47;
  /** Code identifying a "IMG" tag */
  public static final int IMG = 48;
  /** Code identifying a "INPUT" tag */
  public static final int INPUT = 49;
  /** Code identifying a "INS" tag */
  public static final int INS = 50;
  /** Code identifying a "ISINDEX" tag */
  public static final int ISINDEX = 51;
  /** Code identifying a "KBD" tag */
  public static final int KBD = 52;
  /** Code identifying a "LABEL" tag */
  public static final int LABEL = 53;
  /** Code identifying a "LEGEND" tag */
  public static final int LEGEND = 54;
  /** Code identifying a "LI" tag */
  public static final int LI = 55;
  /** Code identifying a "LINK" tag */
  public static final int LINK = 56;
  /** Code identifying a "MAP" tag */
  public static final int MAP = 57;
  /** Code identifying a "MENU" tag */
  public static final int MENU = 58;
  /** Code identifying a "META" tag */
  public static final int META = 59;
  /** Code identifying a "NOFRAMES" tag */
  public static final int NOFRAMES = 60;
  /** Code identifying a "NOSCRIPT" tag */
  public static final int NOSCRIPT = 61;
  /** Code identifying a "OBJECT" tag */
  public static final int OBJECT = 62;
  /** Code identifying a "OL" tag */
  public static final int OL = 63;
  /** Code identifying a "OPTGROUP" tag */
  public static final int OPTGROUP = 64;
  /** Code identifying a "OPTION" tag */
  public static final int OPTION = 65;
  /** Code identifying a "P" tag */
  public static final int P = 66;
  /** Code identifying a "PARAM" tag */
  public static final int PARAM = 67;
  /** Code identifying a "PLAINTEXT" tag */
  public static final int PLAINTEXT = 68;
  /** Code identifying a "PRE" tag */
  public static final int PRE = 69;
  /** Code identifying a "Q" tag */
  public static final int Q = 70;
  /** Code identifying a "S" tag */
  public static final int S = 71;
  /** Code identifying a "SAMP" tag */
  public static final int SAMP = 72;
  /** Code identifying a "SCRIPT" tag */
  public static final int SCRIPT = 73;
  /** Code identifying a "SELECT" tag */
  public static final int SELECT = 74;
  /** Code identifying a "SMALL" tag */
  public static final int SMALL = 75;
  /** Code identifying a "SPAN" tag */
  public static final int SPAN = 76;
  /** Code identifying a "STRIKE" tag */
  public static final int STRIKE = 77;
  /** Code identifying a "STRONG" tag */
  public static final int STRONG = 78;
  /** Code identifying a "STYLE" tag */
  public static final int STYLE = 79;
  /** Code identifying a "SUB" tag */
  public static final int SUB = 80;
  /** Code identifying a "SUP" tag */
  public static final int SUP = 81;
  /** Code identifying a "TABLE" tag */
  public static final int TABLE = 82;
  /** Code identifying a "TBODY" tag */
  public static final int TBODY = 83;
  /** Code identifying a "TD" tag */
  public static final int TD = 84;
  /** Code identifying a "TEXTAREA" tag */
  public static final int TEXTAREA = 85;
  /** Code identifying a "TFOOT" tag */
  public static final int TFOOT = 86;
  /** Code identifying a "TH" tag */
  public static final int TH = 87;
  /** Code identifying a "THEAD" tag */
  public static final int THEAD = 88;
  /** Code identifying a "TITLE" tag */
  public static final int TITLE = 89;
  /** Code identifying a "TR" tag */
  public static final int TR = 90;
  /** Code identifying a "TT" tag */
  public static final int TT = 91;
  /** Code identifying a "U" tag */
  public static final int U = 92;
  /** Code identifying a "UL" tag */
  public static final int UL = 93;
  /** Code identifying a "VAR" tag */
  public static final int VAR = 94;
  /** Code identifying a "WBR" tag */
  public static final int WBR = 95;
  /** Code identifying a "XMP" tag */
  public static final int XMP = 96;

  /* This table was generated by Jaxo's GenStatTable utility - do not edit! */
  /** GenStatTable double-hash list
   *    - input file: file:/D:/u/newdev/SuperWaba/jaxo/html/HtmlTagProps.txt
   *    - gen date:   Sep 1, 2003 9:13:55 AM CEST
   *    - 0 error(s), 0 warning(s)
   */
  private static final int entries[][] = { { -938331488, 0x9, // BASEFONT
      2176, 0x17, // DD
      2688, 0x5b, // TT
      65760, 0xb, // BIG
      66912, 0x14, // COL
      82464, 0x50, // SUB
      2213344, 0x2b // HEAD
      }, { 65, 0x1 // A
      },
      { 66, 0x7, // B
          2044322, 0xd, // BODY
          79739586, 0x56 // TFOOT
      }, { -445489757, 0x3, // ACRONYM
          72611, 0x30, // IMG
          79491, 0x45 // PRE
      }, { -2137885020, 0x2f, // IFRAME
          2163908, 0x22 // FORM
      }, { 2362885, 0x3b // META
      }, { 1270556102, 0x10 // CAPTION
      }, { 84743, 0x5e, // VAR
          85735, 0x5f, // WBR
          79011047, 0x4b, // SMALL
          1975348647, 0x3c // NOFRAMES
      }, { -1485680184, 0x33, // ISINDEX
          -146362072, 0x20, // FIELDSET
          2184, 0x1c, // DL
          2216, 0x1e // EM
      }, { 73, 0x2e, // I
          2281, 0x25 // H1
      }, { 2282, 0x26, // H2
          2314, 0x2c, // HR
          2551626, 0x4c, // SPAN
          69820330, 0x31 // INPUT
      }, { -1854356277, 0x49, // SCRIPT
          2283, 0x27, // H3
          67563, 0x18, // DEL
          2228139, 0x2d // HTML
      }, { -1960789556, 0x3d, // NOSCRIPT
          2284, 0x28, // H4
          67596, 0x19, // DFN
          701853612, 0x40 // OPTGROUP
      }, { -2053244915, 0x36, // LEGEND
          2285, 0x29, // H5
          65613, 0xa, // BDO
          67693, 0x1a, // DIR
          74189, 0x34, // KBD
          2017421, 0x6, // AREA
          2074093, 0x13, // CODE
          67154253, 0x23, // FRAME
          75898989, 0x43 // PARAM
      }, { 2286, 0x2a, // H6
          72654, 0x32, // INS
          82478, 0x51, // SUP
          79578030, 0x52 // TABLE
      }, { 2163791, 0x21, // FONT
          1788294671, 0xc // BLOCKQUOTE
      }, { 80, 0x42, // P
          2128, 0xe, // BR
          2192, 0x1d, // DT
          2672, 0x54 // TD
      }, { 81, 0x46, // Q
          67697, 0x1b, // DIV
          2001969, 0x2, // ABBR
          2031313, 0x8, // BASE
          2537585, 0x48, // SAMP
          79242641, 0x4f // STYLE
      }, { -1838656590, 0x4d, // STRIKE
          1970608946, 0xf // BUTTON
      }, { 83, 0x47 // S
      }, { -429709356, 0x4, // ADDRESS
          2676, 0x57, // TH
          72189652, 0x35, // LABEL
          79789108, 0x58 // THEAD
      }, { -1956807563, 0x41, // OPTION
          -862326827, 0x24, // FRAMESET
          85, 0x5c, // U
          1984282709, 0x11 // CENTER
      }, { 79620086, 0x53 // TBODY
      }, { -1838650729, 0x4e, // STRONG
          2711, 0x5d, // UL
          2068823, 0x12, // CITE
          464861655, 0x44 // PLAINTEXT
      }, { 79833656, 0x59 // TITLE
      }, { 66082489, 0x1f // EMBED
      }, { -220616902, 0x55, // TEXTAREA
          2336762, 0x38, // LINK
          1937235034, 0x5 // APPLET
      }, { 87035, 0x60 // XMP
      }, { -1852692228, 0x4a, // SELECT
          76092, 0x39 // MAP
      }, { 2429, 0x37, // LI
          2525, 0x3f // OL
      }, { 2686, 0x5a // TR
      }, { -1970038977, 0x3e, // OBJECT
          2362719, 0x3a, // MENU
          146429183, 0x15, // COLGROUP
          1668381247, 0x16 // COMMENT
      } };

  /**
   * Returns the hash code of the given string, automatically converting the chars to upper case.
   * 
   * @param b
   * @param offset
   * @param count
   * @return The hash code.
   */
  public static int hashCode(byte b[], int offset, int count) {
    int key = 0;
    // compute the key associated to the series of bytes
    while (count-- > 0) {
      byte ch = b[offset++];
      // Warning: following line upper cases assuming latin-1 ASCII
      if (('a' <= ch) && (ch <= 'z')) {
        ch -= ('a' - 'A');
      }
      key = (key << 5) - key + ch;
    }
    return key;
  }

  /**
   * Get the code associated to a key.
   *
   * @param b byte array containing the key
   * @param offset position of the first byte of the key in the array
   * @param count number of bytes composing the key
   * @return the corresponding character value, or 0 if invalid
   */
  public static char toCode(byte b[], int offset, int count) {
    int key = hashCode(b, offset, count);
    int[] bucket = entries[key & 0x1F]; // open the bucket with it
    for (int i = 0; i < bucket.length; i += 2) {
      int j = bucket[i];
      if (j >= key) {
        if (j == key) {
          return (char) bucket[i + 1];
        }
        break;
      }
    }
    return 0; // which is an invalid unicode character
  }
}
