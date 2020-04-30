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

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!  REMEMBER THAT ANY CHANGE YOU MAKE IN THIS CODE MUST BE SENT BACK TO SUPERWABA COMPANY     !!!!
//!!!!  LEMBRE-SE QUE QUALQUER ALTERACAO QUE SEJA FEITO NESSE CODIGO DEVERÁ SER ENVIADA PARA NOS  !!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.UIColors;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.xml.AttributeList;

/**
 *
 * The class <code>Style</code> group the several values required to paint
 * a <code>Tile</code>.
 * <P>
 * <code>Style</code>s are grouped into a linked list in ancestry order.
 * This disposition permits to implement style inheritance.
 *
 * @author Pierre G. Richard
 */
public class Style {
  /** Set the default font size to be used in the current build.
   * Note that the Document must be re-rendered in order to update its font. 
   */
  public static int defaultFontSize = Font.NORMAL_SIZE;

  // Font Bits
  static final int BOLD = 1 << 0;
  static final int ITALIC = 1 << 1;
  static final int UNDERLINE = 1 << 2;
  static final int STRIKETHROUGH = 1 << 3;
  static final int SUBSCRIPT = 1 << 4;
  static final int SUPERSCRIPT = 1 << 5;
  static final int MONOSPACE = 1 << 6;
  static final int FontMask = 0x7F;

  private static final int DISJOINT = 1 << 7;
  private static final int HEADED = 1 << 8; // List items, etc
  private static final int CHG_STYLE = 1 << 9;
  private static final int LINK = 1 << 10;
  static final int P_AFTER = 1 << 11;

  private static final int TopMarginPos = 16; // Mask pos
  private static final int TopMarginLen = 2; // Mask width: [0,7]
  private static final int IndentPos = 18; // Mask pos
  private static final int IndentLen = 3; // Mask width: [-4,+3]
  private static final int AlignPos = 21; // Mask pos
  private static final int AlignLen = 2; // Mask width: [0,3]
  private static final int FontSizePos = 23; // Mask pos: next pos 15
  private static final int FontSizeLen = 3; // Mask width: [-4,+3]
  private static final int TypePos = 26; // Mask pos
  private static final int TypeLen = 2; // Mask width: [0,3]

  static final int ALIGN_LEFT = 0;
  static final int ALIGN_CENTER = 1;
  static final int ALIGN_RIGHT = 2;
  static final int ALIGN_NONE = 3; // no formatting

  private static final int TYPE_SPECIAL = 3;
  private static final int TYPE_DIV = 2;
  private static final int TYPE_BLOCK = 1;
  private static final int TYPE_INLINE = 0;
  private static final int TYPE_WIERD = 0;

  private static final int tagStyles[] = { /* 0  UNKNOWN   */ 0,
      /* 1  A         */ type(TYPE_INLINE) | CHG_STYLE | UNDERLINE | LINK, // miketogg@570_59: define fontcolor for A tags, remove underline
      /* 2  ABBR      */ 0, /* 3  ACRONYM   */ 0, /* 4  ADDRESS   */ type(TYPE_BLOCK) | CHG_STYLE | ITALIC,
      /* 5  APPLET    */ 0, /* 6  AREA      */ 0, /* 7  B         */ type(TYPE_INLINE) | CHG_STYLE | BOLD,
      /* 8  BASE      */ 0, /* 9  BASEFONT  */ 0, /* 10 BGSOUND   */ 0,
      /* 11 BIG       */ type(TYPE_INLINE) | CHG_STYLE | fontSize(+2) | BOLD,
      /* 12 BLOCKQUOTE*/ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | P_AFTER | top(5) | indent(+2), /* 13 BODY      */ 0,
      /* 14 BR        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | indent(-1), /* 15 BUTTON    */ 0,
      /* 16 CAPTION   */ 0, /* 17 CENTER    */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | align(ALIGN_CENTER),
      /* 18 CITE      */ 0, /* 19 CODE      */ type(TYPE_INLINE) | CHG_STYLE | fontSize(-1), /* 20 COL       */ 0,
      /* 21 COLGROUP  */ 0, /* 22 COMMENT   */ 0,
      /* 23 DD        */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | indent(2),
      /* 24 DEL       */ type(TYPE_INLINE) | CHG_STYLE | STRIKETHROUGH, /* 25 DFN       */ 0,
      /* 26 DIR       */ type(TYPE_DIV) | CHG_STYLE, /* 27 DIV       */ 0,
      /* 28 DL        */ type(TYPE_DIV) | CHG_STYLE | DISJOINT,
      /* 29 DT        */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | BOLD | indent(-2),
      /* 30 EM        */ type(TYPE_INLINE) | CHG_STYLE | BOLD | ITALIC, /* 31 EMBED     */ 0, /* 32 FIELDSET  */ 0,
      /* 33 FONT      */ type(TYPE_INLINE) | CHG_STYLE, /* 34 FORM      */ type(TYPE_WIERD), /* 35 FRAME     */ 0,
      /* 36 FRAMESET  */ 0,
      /* 37 H1        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(+3) | BOLD,
      /* 38 H2        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(+2) | BOLD,
      /* 39 H3        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(+1) | BOLD,
      /* 40 H4        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(+0) | BOLD,
      /* 41 H5        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(-1) | BOLD,
      /* 42 H6        */ type(TYPE_INLINE) | CHG_STYLE | DISJOINT | P_AFTER | align(ALIGN_NONE) | fontSize(-2) | BOLD,
      /* 43 HEAD      */ 0, /* 44 HR        */ type(TYPE_WIERD), /* 45 HTML      */ 0,
      /* 46 I         */ type(TYPE_INLINE) | CHG_STYLE | ITALIC, /* 47 IFRAME    */ 0,
      /* 48 IMG       */ type(TYPE_INLINE), /* 49 INPUT     */ type(TYPE_INLINE), /* 50 INS       */ 0,
      /* 51 ISINDEX   */ 0, /* 52 KBD       */ 0, /* 53 LABEL     */ 0, /* 54 LEGEND    */ 0,
      /* 55 LI        */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | HEADED | indent(+3), /* 56 LINK      */ 0,
      /* 57 LISTING   */
      /* 58 MAP       */ 0,
      /* 59 MARQUEE   */
      /* 60 MENU      */ 0, /* 61 META      */ 0,
      /* 62 NOBR      */
      /* 63 NOFRAMES  */ 0, /* 64 NOSCRIPT  */ 0, /* 65 OBJECT    */ 0,
      /* 66 OL        */ type(TYPE_DIV) | CHG_STYLE | DISJOINT | indent(+2), /* 67 OPTGROUP  */ 0, /* 68 OPTION    */ 0,
      /* 69 P         */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | top(3), /* 70 PARAM     */ 0, /* 71 PLAINTEXT */ 0,
      /* 72 PRE       */ type(TYPE_BLOCK) | CHG_STYLE | align(ALIGN_NONE),
      /* 73 Q         */ type(TYPE_INLINE) | CHG_STYLE | indent(+2),
      /* 74 S         */ type(TYPE_INLINE) | CHG_STYLE | STRIKETHROUGH,
      /* 75 SAMP      */ type(TYPE_BLOCK) | CHG_STYLE | fontSize(-2), /* 76 SCRIPT    */ 0, /* 77 SELECT    */ 0,
      /* 78 SMALL     */ type(TYPE_INLINE) | CHG_STYLE | fontSize(-1), /* 79 SPAN      */ 0,
      /* 80 STRIKE    */ type(TYPE_INLINE) | CHG_STYLE | STRIKETHROUGH,
      /* 81 STRONG    */ type(TYPE_INLINE) | CHG_STYLE | BOLD, /* 82 STYLE     */ 0,
      /* 83 SUB       */ type(TYPE_INLINE) | CHG_STYLE | SUBSCRIPT | fontSize(-2),
      /* 84 SUP       */ type(TYPE_INLINE) | CHG_STYLE | SUPERSCRIPT | fontSize(-2),
      /* 85 TABLE     */ type(TYPE_SPECIAL) | CHG_STYLE, /* 86 TBODY     */ 0,
      /* 87 TD        */ type(TYPE_SPECIAL) | CHG_STYLE, /* 88 TEXTAREA  */ 0, /* 89 TFOOT     */ 0,
      /* 90 TH        */ type(TYPE_SPECIAL) | CHG_STYLE | BOLD, /* 91 THEAD     */ 0, /* 92 TITLE     */ 0,
      /* 93 TR        */ type(TYPE_SPECIAL) | CHG_STYLE, /* 94 TT        */ 0,
      /* 95 U         */ type(TYPE_INLINE) | CHG_STYLE | UNDERLINE,
      /* 96 UL        */ type(TYPE_DIV) | CHG_STYLE | DISJOINT | indent(+2), /* 97 VAR       */ 0, /* 98 WBR       */ 0,
      /* 99 XMP       */ type(TYPE_BLOCK) | CHG_STYLE | DISJOINT | top(3) | MONOSPACE };

  private static int type(int val) {
    return (val << TypePos) & ((-1 >>> (32 - TypeLen)) << TypePos);
  }

  private static int fontSize(int grow) {
    return (grow << FontSizePos) & ((-1 >>> (32 - FontSizeLen)) << FontSizePos);
  }

  private static int indent(int val) {
    return (val << IndentPos) & ((-1 >>> (32 - IndentLen)) << IndentPos);
  }

  private static int align(int val) {
    return (val << AlignPos) & ((-1 >>> (32 - AlignLen)) << AlignPos);
  }

  private static int top(int val) {
    return (val << TopMarginPos) & ((-1 >>> (32 - TopMarginLen)) << TopMarginPos);
  }

  protected boolean isParagraph;
  private Style parent;
  private int tagHashId;
  protected int fontBits;
  protected int topMargin;
  protected int fontSize;
  protected String fontFace;
  protected int fontColor;
  protected int backColor;
  protected int alignment;
  protected int indent;
  private boolean isInited;
  private boolean isHeaded;
  protected boolean isDisjoint;
  private int style;
  protected AttributeList atts;

  /** If this text is part of a link, what is the href for this link */
  protected String href;

  /**
   * Constructor for the initial Style. Note that the tagHashId is 0.
   */
  Style() {
    fontSize = defaultFontSize;
    fontFace = Font.DEFAULT;
    fontColor = UIColors.htmlContainerControlsFore;
    backColor = UIColors.htmlContainerControlsBack;
  }

  /**
   * Constructor
   *
   * @param tag
   *           tag identifier for this element
   * @param atts
   *           The attributes attached to the element
   * @param parent
   *           parent Style
   */
  private Style(int tagHashId, AttributeList atts, Style parent) {
    this.parent = parent;
    this.tagHashId = tagHashId;
    this.atts = atts;
    String attval;
    style = getStyle(tagHashId);
    href = parent.href;
    isParagraph = parent.isParagraph;

    attval = atts.getAttributeValue("align");
    if (attval != null) {
      if (attval.equalsIgnoreCase("center")) {
        alignment = ALIGN_CENTER;
      } else if (attval.equalsIgnoreCase("right")) {
        alignment = ALIGN_RIGHT; // guich@511_2: defaults to left instead of right.
      } else {
        alignment = ALIGN_LEFT;
      }
    } else {
      alignment = getAlignment();
    }

    topMargin = getTopMargin();
    isDisjoint = (style & DISJOINT) != 0;
    isHeaded = (style & HEADED) != 0;
    boolean isLink = (style & LINK) != 0;

    // inherited styles
    // bottomMargin = parent.bottomMargin;
    indent = parent.indent + getIndention();
    if (indent < 0) {
      indent = 0;
    }
    fontBits = getFontBits() | parent.fontBits;
    fontSize = parent.fontSize + getFontSizeDelta();
    fontFace = parent.fontFace;
    fontColor = isLink ? UIColors.htmlContainerLink : parent.fontColor;

    if ((attval = atts.getAttributeValue("bgcolor")) != null) {
      backColor = getColor(attval, parent.backColor);
    } else {
      backColor = parent.backColor;
    }

    switch (tagHashId) {
    case TagDereferencer.TD: // a TD stops indent inheritance
      indent = 0;
      break;
    case TagDereferencer.A:
      href = atts.getAttributeValue("href");
      break;
    case TagDereferencer.FONT:
      int v = atts.getAttributeValueAsInt("size", -1);
      if (v != -1) {
        fontSize = v;
      }
      if (((attval = atts.getAttributeValue("color")) != null)) {
        fontColor = getColor(attval, parent.backColor);
      }
      if ((attval = atts.getAttributeValue("face")) != null) {
        fontFace = attval;
      }
      break;
    }
  }

  static int getColor(String attval, int defaultColor) {
    if (attval.startsWith("#")) {
      attval = attval.substring(1);
    }
    return attval.length() == 0 ? defaultColor : Color.getRGB(attval);
  }

  /**
   * End enough Styles in the list to create a legal element content for a
   * new element starting.
   *
   * @param current
   *           current Style
   * @param tag
   *           tag identifier of the starting element
   * @param atts
   *           The attributes attached to the element
   * @return the new current Style
   *
   * Impl. Note:
   *
   * To ensure proper element nesting requires the use of well-balanced end tag
   * (ala XHTML), however we want to allow bad markup. The algorithm used here
   * consists to class HTML tags in four categories:
   *  | Specials | Division | Block | Inlines : Wierd |
   * +----------+----------+---------------+-------------------------+-------+ |
   * TABLE | DIR | ADDRESS DD | H1 A Q : HR | | TR | DL | BLOCKQUOTE DT | H2 B
   * S : FORM | | TD | OL | CENTER LI | H3 BIG SMALL : | | | UL | P BR | H4
   * CODE STRIKE : | | | | PRE | H5 DEL STRONG : | | | | SAPos | H6 EM SUB : | | | |
   * XPos | IMG FONT SUP : | | | | | INPUT I U : |
   * +----------+----------+---------------+-------------------------+-------+
   *
   * "Wierd" tags are categorized as "inlines", but are not as such - So is HR --
   * though HR could be seen as a true inline. HR is empty, cleaned up from the
   * list here. - FORM is also special. FORMS ends nothing. The dangling of
   * FORM is OK for many parsers.
   *
   * When a tag that belongs to column 'n' starts, it ends all tags in columns
   * 'm', with 'm > n';
   *
   * When (n == m) - the "Inlines" category does not end another Inline
   * (nested) - the "Block" category does end an other Block (not nested) - the
   * "Division" category does not end another Division (nested) - in the
   * "Special" category: . TABLE ends nothing in its column, nor a DIV . TR
   * ends TD . TD ends TD
   *
   * These are simplimistic rules, but it works with most bad markup.
   *
   */
  static Style tagStartFound(Style current, int tagHashId, AttributeList atts) {
    int type = getType(tagHashId);
    if ((current.tagHashId == TagDereferencer.HR) || (current.tagHashId == TagDereferencer.IMG)
        || (current.tagHashId == TagDereferencer.INPUT)) {
      current = current.parent;
    }
    redo: while (true) {
      switch (type) {
      case TYPE_SPECIAL: // end themselves and INLINEs
        switch (tagHashId) {
        case TagDereferencer.TR: // A TR ends everything up to <TABLE>
          while ((current.tagHashId != 0) && (current.tagHashId != TagDereferencer.TABLE)) {
            if (current.tagHashId == TagDereferencer.TR) {
              current = current.parent;
              break;
            }
            current = current.parent;
          }
          break;
        case TagDereferencer.TD: // <TD> ends everything up to <TR>
          while ((current.tagHashId != 0) && (current.tagHashId != TagDereferencer.TR)) {
            current = current.parent;
          }
          break;
        case TagDereferencer.TABLE:
          type -= 2; // <TABLE>s end what BLOCKs end
          continue redo; // a kind of "fall thru"
        }
        break;
      case TYPE_DIV: // end what BLOCKs end
        --type;
        /* fall thru */
      case TYPE_BLOCK: // end themselves and INLINEs
        while ((current.tagHashId != 0) && (type >= current.getType())) {
          current = current.parent;
        }
        break;
      default: // INLINEs or WIERD: do nothing
        break;
      }
      break;
    }
    if (isStyleChanged(tagHashId)) {
      switch (tagHashId) {
      case TagDereferencer.OL:
        current = new Style.HeaderMaker.Ordered(tagHashId, atts, current);
        break;
      case TagDereferencer.UL:
        current = new Style.HeaderMaker.Unordered(tagHashId, atts, current);
        break;
      default:
        int originalAlign = current.alignment;
        current = new Style(tagHashId, atts, current);

        //flsobral@tc126: allow <p> to inherit the current alignment
        if (tagHashId != TagDereferencer.P && originalAlign == ALIGN_NONE) {
          current.alignment = ALIGN_NONE;
        } else if (tagHashId == TagDereferencer.BR && originalAlign != ALIGN_LEFT) {
          current.alignment = ALIGN_NONE;
        }
        break;
      }
    }
    return current;
  }

  /**
   * End enough Style in the list until we find the matching start tag which
   * is now ended.
   *
   * @param current
   *           current Style
   * @param tag
   *           tag identifier of the starting element
   * @return the new current Style
   */
  static Style tagEndFound(Style current, int tagHashId) {
    if (isStyleChanged(tagHashId)) {
      Style save = current; // avoid it to be gc'ed
      while ((current.tagHashId != 0) && (current.tagHashId != tagHashId)) {
        current = current.parent;
      }
      current = current.tagHashId == 0 ? save : current.parent;
    }
    return current;
  }

  /**
   * Find out if the initial values of this Style have already been applied,
   * and compute these values if they haven't been queried before.
   *
   * @return false if the initial values have been applied. true, otherwise.
   */
  boolean hasInitialValues() {
    if (isInited) {
      return false;
    }
    isInited = true;
    for (Style s = parent; (s != null) && (!s.isInited); s = s.parent) {
      isParagraph |= s.isParagraph;
      isDisjoint |= s.isDisjoint;
      topMargin += s.topMargin;
      s.isInited = true;
    }
    return true;
  }

  static int getStyle(int tagHashId) {
    return tagStyles[tagHashId];
  }

  private int getFontBits() {
    return style & FontMask;
  }

  private int getType() {
    // >>> b/c unsigned
    int style = tagStyles[tagHashId];
    return (style << (32 - (TypePos + TypeLen))) >>> (32 - TypeLen);
  }

  private int getFontSizeDelta() {
    return (style << (32 - (FontSizePos + FontSizeLen))) >> (32 - FontSizeLen);
  }

  private int getIndention() {
    // >> b/c signed  -  indentions are 4 pixel per unit
    return ((style << (32 - (IndentPos + IndentLen))) >> (32 - IndentLen)) << 2;
  }

  private int getAlignment() {
    // >>> b/c unsigned
    return (style << (32 - (AlignPos + AlignLen))) >>> (32 - AlignLen);
  }

  int getTopMargin() {
    return (style << (32 - (TopMarginPos + TopMarginLen))) >>> (32 - TopMarginLen);
  }

  private static boolean isStyleChanged(int tagHashId) {
    return (tagStyles[tagHashId] & CHG_STYLE) != 0;
  }

  private static int getType(int tagHashId) {
    // >>> b/c unsigned
    int style = tagStyles[tagHashId];
    return (style << (32 - (TypePos + TypeLen))) >>> (32 - TypeLen);
  }

  /**
   * Get the header associated to this style, if any.
   *
   * @return the header associated to this style
   * @see Header
   */
  Header getHeader() {
    if (isHeaded) {
      for (Style s = parent; s != null; s = s.parent) {
        if (s instanceof Style.HeaderMaker) {
          return ((Style.HeaderMaker) s).getHeader();
        }
      }
    }
    return null;
  }

  static class Header extends Control {
  }

  private static abstract class HeaderMaker extends Style {
    HeaderMaker(int tagHashId, AttributeList atts, Style parent) {
      super(tagHashId, atts, parent);
    }

    @Override
    abstract Header getHeader();

    private static class Unordered extends HeaderMaker {
      private class Bullet extends Header {
        public Bullet() {
          focusTraversable = false;
        }

        @Override
        public void onPaint(Graphics g) {
          g.backColor = UIColors.htmlContainerControlsFore;
          g.fillCircle(height / 2, height - height / 2, height / 4);
        }

        @Override
        public int getPreferredWidth() {
          return fmH;
        }

        @Override
        public int getPreferredHeight() {
          return fmH;
        }
      }

      Unordered(int tagHashId, AttributeList atts, Style parent) {
        super(tagHashId, atts, parent);
      }

      @Override
      Header getHeader() {
        return new Bullet();
      }
    }

    private static class Ordered extends HeaderMaker {
      int order;

      private class Number extends Header {
        String text;

        Number(int i) {
          this.text = i + ". ";
          focusTraversable = false;
        }

        @Override
        public void onPaint(Graphics g) {
          g.drawText(text, 0, (height - fmH) / 2); // guich@tc114_21: vertical align
        }

        @Override
        public int getPreferredWidth() {
          return fm.stringWidth(text);
        }

        @Override
        public int getPreferredHeight() {
          return fmH + Edit.prefH; // guich@tc114_21: add prefH
        }
      }

      Ordered(int tagHashId, AttributeList atts, Style parent) {
        super(tagHashId, atts, parent);
        order = 0;
      }

      @Override
      Header getHeader() {
        return new Number(++order);
      }
    }
  }

  public Font getFont() {
    return Font.getFont(fontFace, ((fontBits & BOLD) != 0), fontSize);
  }

  /**
   * Returns the Control alignment constant to be used by elements using this style.
   * 
   * @param isRelative
   *           true if the alignment constant returned should be relative to another control instead of relative to the
   *           screen.
   * @return the matching Control alignment constant
   * @since TotalCross 1.27
   */
  public int getControlAlignment(boolean isRelative) {
    switch (alignment) {
    case Style.ALIGN_CENTER:
      return isRelative ? Control.CENTER_OF : Control.CENTER;
    case Style.ALIGN_RIGHT:
      return isRelative ? Control.RIGHT_OF : Control.RIGHT;
    case Style.ALIGN_LEFT:
    default:
      return isRelative ? 0 : Control.LEFT;
    }
  }
}
