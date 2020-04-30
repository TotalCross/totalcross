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

import totalcross.net.HttpStream;
import totalcross.net.URI;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Check;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.ListBox;
import totalcross.ui.MultiEdit;
import totalcross.ui.MultiListBox;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.ScrollContainer;
import totalcross.ui.UIColors;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Hashtable;
import totalcross.util.IntVector;
import totalcross.util.Vector;
import totalcross.xml.AttributeList;
import totalcross.xml.ContentHandler;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlReadable;
import totalcross.xml.XmlReader;

/** Represents a Html Document.
 * Here is a sample of how to load an image of a document instead of downloading it from
 * a URL:
 * <pre>
 * // Extends Document so that we can provide another way to load images, from our internal database
 * class ArticleDocument extends Document
 * {
 *    PDBFile cat;
 *    DataStream ds;
 *
 *    /** The cat parameter should be an already open PDBFile which contains all the images.
 *      * Note that the image is in the form "<rec number>.gif" or "<rec number>.jpeg".
 *      * /
 *    public ArticleDocument(XmlReadable doc, PDBFile cat)
 *    {
 *       this.cat = cat;
 *       ds = new DataStream(cat);
 *       renderDoc(doc);
 *    }
 *
 *    protected Image loadImage(String src, URI base)
 *    {
 *       int dot = src.indexOf('.');
 *       if (dot == -1) throw new RuntimeException("src image not in expected format: "+src);
 *       int idx = Convert.toInt(src.substring(0,dot));
 *       String ext = src.substring(dot+1).toLowerCase();
 *       if (cat.setRecordPos(idx))
 *       {
 *          int size = cat.getRecordSize();
 *          byte []data = new byte[size];
 *          ds.readBytes(data);
 *          return new Image(new ByteArrayStream(data));
 *      }
 *    }
 * }
 * </pre>
 * And here's a sample on how to call it:
 * <pre>
 * String s = ...; // get the html from somewhere and store in a String
 * if (!s.substring(0,10).toLowerCase().startsWith("<body")) // make sure we are between a body tag
 *    s = "<body>"+s+"</body>";
 * // Displays the xml
 * hcView.setDocument(new ArticleDocument(new XmlReadableString(s),cat)); // cat must be already instantiated
 * </pre>
 * To change the font size, you must change Style.defaultFontSize. To change the default colors,
 * change UIColors.htmlXXX fields, and don't forget to call htmlContainer.setBackForeColors with these colors.
 * @see Style#defaultFontSize
 * @see totalcross.ui.UIColors#htmlContainerControlsBack
 * @see totalcross.ui.UIColors#htmlContainerControlsFore
 * @see totalcross.ui.UIColors#htmlContainerLink
 */

public class Document extends ScrollContainer {
  /** Base URL for this document */
  public URI baseURI;

  /** Title associated with this document. */
  public String title = "";

  /** All forms in this document. */
  public Vector vForms = new Vector(2);

  /**
   * The password mode for the Edits. Defaults to Edit.PASSWORD_ALL.
   * 
   * @see totalcross.ui.Edit#PASSWORD
   * @see totalcross.ui.Edit#PASSWORD_ALL
   */
  public byte passwordMode = Edit.PASSWORD_ALL;

  /**
   * Constructor
   *
   * @param doc XmlReadable to be read
   * @throws totalcross.io.IOException
   */
  public Document(XmlReadable doc) throws totalcross.io.IOException, SyntaxException {
    renderDoc(doc);
  }

  /**
   * Default Constructor. The document must be rendered using <code>renderDoc</code>.
   */
  protected Document() // guich@510_22
  {
  }

  /**
   * Can be overridden by the caller in order to load an image from a different place.
   * Must return null if not handled/not found.
   * By default, searches for the given file at the loaded libraries (TCZ files).
   * @param src The image name
   * @param baseURI The base URI
   * @return an Image
   */
  protected Image loadImage(String src, URI baseURI) // guich@510_20
  {
    try {
      return new Image(src);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Renders the document.
   * @param doc XmlReadable to be read
   * @throws totalcross.io.IOException
   */
  protected void renderDoc(XmlReadable doc) throws totalcross.io.IOException, SyntaxException // guich@510_22
  {
    doc.setCaseInsensitive(true);
    baseURI = doc.getBaseURI();
    backColor = new Builder(doc).currStyle.backColor;
  }

  static class Hidden extends Control {
    public Hidden() {
      focusTraversable = false;
    }

    @Override
    public int getPreferredWidth() {
      return 0;
    }

    @Override
    public int getPreferredHeight() {
      return 0;
    }
  }

  private class Img extends Control implements Document.CustomLayout, Document.SizeDelimiter {
    /**
     * read align attribute once
     */
    private int align;
    public int valign;

    private String altName;
    private Image img;

    /**
     * Constructor
     *
     * @param doc containing document
     * @param atts tag attributes
     * @param style associated style
     */
    Img(Style currStyle, AttributeList atts, URI baseURI) {
      String v;
      boolean mustBeScaled = false;

      v = atts.getAttributeValue("align");
      this.align = v == null ? currStyle.getControlAlignment(false)
          : v.equalsIgnoreCase("center") ? CENTER : v.equalsIgnoreCase("right") ? RIGHT : LEFT;
      v = atts.getAttributeValue("valign");
      this.valign = v == null ? TOP
          : v.equalsIgnoreCase("middle") || v.equalsIgnoreCase("center") ? CENTER
              : v.equalsIgnoreCase("bottom") ? BOTTOM : TOP;
      this.altName = atts.getAttributeValue("alt");
      int width = atts.getAttributeValueAsInt("width", 0);
      int height = atts.getAttributeValueAsInt("height", 0);
      mustBeScaled = width > 0 || height > 0;
      String src = atts.getAttributeValue("src");
      if (src != null) {
        try {
          img = Document.this.loadImage(src, baseURI); // let our caller handle the image - maybe it comes from another source?
          if (img == null) {
            URI uri = new URI(src, baseURI);
            HttpStream in = new HttpStream(uri);
            if (in.isOk()) {
              img = in.makeImage();
            }
          }
          if (img != null) {
            int imgWidth, imgHeight;
            if ((imgWidth = img.getWidth()) <= 0 || (imgHeight = img.getHeight()) <= 0) {
              img = null;
            } else if (mustBeScaled) {
              img = (height <= 0 && (height = (imgHeight * width) / imgWidth) <= 0)
                  || (width <= 0 && (width = (imgWidth * height) / imgHeight) <= 0) ? null
                      : img.getSmoothScaledInstance(width, height);
            }
          }
        } catch (Exception e) {
          img = null;
          if (Settings.onJavaSE) {
            Vm.warning("Error reading image: " + e.getMessage());
          }
        }
      }
      if (img == null) {
        try {
          String s = altName != null ? altName : src != null ? src : "no image";
          int tw = fm.stringWidth(s);
          img = new Image(Math.max(width, tw) + 4, Math.max(height, fmH) + 3);
          int w = img.getWidth();
          int h = img.getHeight();
          Graphics g = img.getGraphics();
          g.backColor = UIColors.htmlContainerControlsBack;
          g.fillRect(0, 0, w, h);
          g.foreColor = UIColors.htmlContainerControlsFore;
          g.drawRect(0, 0, w, h);
          g.foreColor = UIColors.htmlContainerControlsFore;
          g.drawText(s, (w - tw) / 2 + 1, (h - fmH) / 2);
        } catch (ImageException e) {
        }
      }
      focusTraversable = currStyle.href != null;
    }

    @Override
    public int getMaxWidth() {
      return img.getWidth();
    }

    @Override
    public int getPreferredWidth() {
      return img.getWidth();
    }

    @Override
    public int getPreferredHeight() {
      return img.getHeight();
    }

    @Override
    public void layout(LayoutContext lc) {
      int width = img.getWidth();
      int height = img.getHeight();
      int alignedPosX = lc.nextX;
      int alignedPosY = lc.nextY;

      //mike@570_59 implemented layouting accordingly to the alignment of the image
      switch (align) {
      case CENTER:
        alignedPosX += (lc.parentContainer.getWidth() - width) / 2;
        break;
      case RIGHT:
        alignedPosX += lc.parentContainer.getWidth() - width;
        break;
      case LEFT:
        // default, left aligned
        break;
      }

      switch (valign) {
      case CENTER:
        alignedPosY = (parent.getHeight() - alignedPosY - height) / 2;
        break;
      case BOTTOM:
        alignedPosY += parent.getHeight() - height;
        break;
      case TOP:
        // default, top aligned
        break;
      }

      lc.verify(img.getWidth());
      setRect(alignedPosX, alignedPosY, PREFERRED, PREFERRED);
      lc.update(width);
      lc.incY = Math.max(lc.incY, img.getHeight() - fmH);
    }

    @Override
    public void onPaint(Graphics g) {
      g.drawImage(img, 0, 0);
    }

    /** added support for links on images */
    @Override
    public void onEvent(Event e) // mike@570_59
    {
      Style s;
      if ((e.type == PenEvent.PEN_DOWN || e.type == KeyEvent.ACTION_KEY_PRESS)
          && (s = ControlProperties.getStyle(this)).href != null) {
        HtmlContainer.getHtmlContainer(this).postLinkEvent(s.href);
      }
    }
  }

  static class BR extends Control implements CustomLayout {
    private boolean isP;

    public BR(boolean isP) {
      this.isP = isP;
      focusTraversable = false;
    }

    @Override
    public int getPreferredWidth() {
      return 0;
    }

    @Override
    public int getPreferredHeight() {
      return fmH + (isP ? Edit.prefH : 0);
    }

    @Override
    public void layout(LayoutContext lc) {
      setRect(lc.nextX, lc.nextY, PREFERRED, PREFERRED);
      lc.disjoin();
    }
  }

  static class Entry {
    String key, value;

    Entry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    @Override
    public boolean equals(Object arg0) //flsobral@tc114_92: added method equals to class totalcross.ui.html.Document.Entry, to compare only the entry value. This fixes an issue with Form.setValue.
    {
      return arg0.equals(value);
    }
  }

  static class SubmitReset implements PressListener {
    boolean isReset;
    Form currForm;
    String title;

    public SubmitReset(Form currForm, boolean isReset, String title) {
      this.title = title;
      this.isReset = isReset;
      this.currForm = currForm;
    }

    @Override
    public void controlPressed(ControlEvent e) {
      if (isReset) {
        currForm.reset();
      } else {
        currForm.submit(title);
      }
    }
  }

  private class Builder extends XmlReader {
    private Style currStyle;
    private Form currForm;

    Builder(XmlReadable doc) throws totalcross.io.IOException, SyntaxException {
      currStyle = new Style();
      Document.this.width = Document.this.height = 4096;
      new HeadContentHandler();
      disableReferenceResolution(true);
      parse(doc);
    }

    /** Impl. note: only works when reference resolution has been disabled */
    @Override
    protected void foundReference(byte input[], int offset, int count) {
      char res = NamedEntitiesDereferencer.toCode(input, offset, count);
      if (res == 0) {
        res = super.resolveCharacterReference(input, offset, count);
      }
      foundCharacter(res);
    }

    @Override
    protected int getTagCode(byte b[], int offset, int count) {
      return TagDereferencer.toCode(b, offset, count);
    }

    @Override
    public final void foundStartTagName(byte buffer[], int offset, int count) {
      super.foundStartTagName(buffer, offset, count);
      switch (tagNameHashId) {
      case TagDereferencer.SCRIPT:
      case TagDereferencer.TEXTAREA:
        setCdataContents(buffer, offset, count);
        break;
      case TagDereferencer.PRE:
      case TagDereferencer.XMP:
        setNewlineSignificant(true);
        break;
      }
    }

    @Override
    public final void foundEndTagName(byte buffer[], int offset, int count) {
      super.foundEndTagName(buffer, offset, count);
      switch (tagNameHashId) {
      case TagDereferencer.PRE:
      case TagDereferencer.XMP:
        setNewlineSignificant(false);
        break;
      }
    }

    private class HeadContentHandler extends ContentHandler {
      boolean inTitle;

      HeadContentHandler() {
        setContentHandler(this);
      }

      @Override
      public void startElement(int tagHashId, AttributeList atts) {
        switch (tagHashId) {
        case TagDereferencer.HTML:
          break;
        case TagDereferencer.TITLE:
          inTitle = true;
          break;
        case TagDereferencer.BASE: {
          String href = atts.getAttributeValue("href");
          if (href != null) {
            baseURI = new URI(href);
          }
          break;
        }
        case TagDereferencer.BODY:
          //flsobral@tc126_36: added support to bgcolor attribute in body tag.
          String bgcolor = atts.getAttributeValue("bgcolor");
          if (bgcolor != null) {
            currStyle.backColor = Style.getColor(bgcolor, currStyle.backColor);
          }
          new BodyContentHandler();
          break;
        }
      }

      @Override
      public void endElement(int tagHashId) {
        if (tagHashId == TagDereferencer.TITLE) {
          inTitle = false;
        }
      }

      @Override
      public void characters(String s) {
        if (inTitle) {
          title = s;
        }
      }
    }

    private class BodyContentHandler extends ContentHandler {
      Container currTile;
      ContentHandler prevContentHandler;
      private Hashtable rgTable; // Names and RadioGroup instances
      private TextSpan currTextSpan;
      private boolean openIdent;
      IntVector alignmentStack = new IntVector();
      boolean insidePre = false;

      BodyContentHandler() {
        alignmentStack.addElement(Style.ALIGN_LEFT);
        currTile = Document.this;
        rgTable = new Hashtable(13);
        prevContentHandler = getContentHandler();
        setContentHandler(this);
      }

      @Override
      public void startElement(int tagHashId, AttributeList atts) {
        int size;
        Control control = null;
        currStyle = Style.tagStartFound(currStyle, tagHashId, atts);
        if (!insidePre && currStyle.alignment == Style.ALIGN_NONE) {
          try {
            currStyle.alignment = alignmentStack.peek();
          } catch (ElementNotFoundException e) {
            // ignore
          }
        }
        String name = atts.getAttributeValue("name");
        String value = atts.getAttributeValue("value");
        boolean glue = false;
        switch (tagHashId) {
        case TagDereferencer.CENTER:
          alignmentStack.push(Style.ALIGN_CENTER);
          break;
        case TagDereferencer.P:
          control = new BR(true);
          break;
        case TagDereferencer.BR:
          control = new BR(false);
          break;
        case TagDereferencer.OL:
        case TagDereferencer.UL:
          openIdent = true;
          break;
        case TagDereferencer.PRE:
          insidePre = true;
          currStyle.alignment = Style.ALIGN_NONE;
          setNewlineSignificant(true);
          break;
        case TagDereferencer.HR:
          control = new Hr();
          break;
        case TagDereferencer.IMG:
          control = new Img(currStyle, atts, baseURI);
          break;
        case TagDereferencer.FORM:
          vForms.addElement(currForm = new Form(currForm, atts));
          break;
        case TagDereferencer.INPUT:
          String type = atts.getAttributeValue("type");
          if (type == null || type.equalsIgnoreCase("password")) // Edit is the default input
          {
            size = atts.getAttributeValueAsInt("size", -1);
            int maxLen = atts.getAttributeValueAsInt("maxlength", -1);
            if (size <= 0) {
              size = 10;
            }
            Edit ed = new Edit(Convert.dup('0', size));
            if (maxLen >= 0) {
              ed.setMaxLength(maxLen);
            }
            ed.clearValueStr = value == null ? "" : value;
            if (type != null) {
              ed.setMode(passwordMode);
            }
            control = ed;
          } else if (type.equalsIgnoreCase("button")) {
            control = new Button(value == null ? " " : value);
          } else if (type.equalsIgnoreCase("submit") || type.equalsIgnoreCase("reset")) {
            final boolean isReset = type.equalsIgnoreCase("reset");
            control = new Button(value == null ? (isReset ? "Reset" : "Submit") : value);
            ((Button) control).addPressListener(new SubmitReset(currForm, isReset, value));
          } else if (type.equalsIgnoreCase("checkbox")) {
            glue = true;
            control = new Check("");
            control.clearValueInt = atts.exists("checked") ? 1 : 0;
          } else if (type.equalsIgnoreCase("radio")) {
            glue = true;
            RadioGroupController rg = null;
            if (name != null) {
              rg = (RadioGroupController) rgTable.get(name);
              if (rg == null) {
                rgTable.put(name, rg = new RadioGroupController());
              }
            }
            control = new Radio("", rg);
            control.clearValueInt = atts.exists("checked") ? 1 : 0;
          } else if (type.equalsIgnoreCase("hidden")) {
            control = new Hidden();
          }
          break;
        case TagDereferencer.TABLE: {
          Table table = new Table(atts, currStyle);
          control = table;
          new TableContentHandler(table);
          break;
        }
        case TagDereferencer.SELECT: {
          // if "multiple" is present, its a MultiListBox. If size > 1, is a 
          // listbox with size lines, else, if size <= 1, its a ComboBox
          size = atts.getAttributeValueAsInt("size", -1);
          if (atts.exists("multiple")) {
            control = new MultiListBox();
            ((ListBox) control).visibleLines = size == -1 ? 4 : size;
          } else if (size > 1) {
            control = new ListBox();
            ((ListBox) control).visibleLines = size;
          } else {
            control = new ComboBox(); // default is a combobox
          }
          new SelectContentHandler(control);
          break;
        }
        case TagDereferencer.TEXTAREA: {
          int rows = atts.getAttributeValueAsInt("rows", -1);
          int cols = atts.getAttributeValueAsInt("cols", -1);
          String mask = Convert.dup('0', cols <= 0 ? 20 : cols);
          if (rows <= 0) {
            rows = 3;
          }
          MultiEdit me = new MultiEdit(mask, rows, 1);
          me.drawDots = false;
          new TextAreaContentHandler(me);
          break;
        }
        default:
          return;
        }
        if (control != null) {
          control.appObj = new ControlProperties(name, value, currStyle);
          control.setFont(currStyle.getFont());
          ((ControlProperties) control.appObj).glue = glue;
          if (currForm != null && !(control instanceof BR)) {
            currForm.inputs.addElement(control); // add this control to the form.
          }
          currTile.add(control);
          control.setBackForeColors(UIColors.htmlContainerControlsBack, UIColors.htmlContainerControlsFore);
        }
      }

      @Override
      public void endElement(int tagHashId) {
        switch (tagHashId) {
        case TagDereferencer.CENTER:
          try {
            alignmentStack.pop();
          } catch (ElementNotFoundException e) {
            // ignore
          }
          break;
        case TagDereferencer.TABLE:
          //mike@570_59 notify table about end
          ((TableContentHandler) getContentHandler()).table.endTable();
          currStyle = Style.tagEndFound(currStyle, tagHashId);
        case TagDereferencer.BODY:
        case TagDereferencer.SELECT:
          setContentHandler(prevContentHandler);
          break;
        case TagDereferencer.FORM:
          if (currForm != null) {
            currForm.reset();
            currForm = currForm.previous;
          }
          break;
        case TagDereferencer.P:
          currTile.add(new BR(false));
          break;
        case TagDereferencer.PRE:
          currStyle = Style.tagEndFound(currStyle, tagHashId);
          insidePre = false;
          break;
        case TagDereferencer.OL:
        case TagDereferencer.UL:
          currTextSpan.finishIdent = true;
          // no break!
        default:
          currStyle = Style.tagEndFound(currStyle, tagHashId);
          if ((Style.getStyle(tagHashId) & Style.P_AFTER) != 0) // add a <p> after a <Hn>
          {
            currTile.add(new BR(false));
            currTile.add(new BR(false));
          }
          break;
        }
      }

      @Override
      public void characters(String s) {
        if (!isDataCDATA() && s.trim().length() > 0) //mike@570_59 Don't create textspan tiles for empty texts
        {
          currTextSpan = new TextSpan(currTile, s, currStyle);
          currTextSpan.openIdent = openIdent;
          openIdent = false;
        }
      }
    }

    private class TableContentHandler extends BodyContentHandler {
      private Table table;

      TableContentHandler(Table table) {
        // super() set currTile to the Document.rootTile this is a fall-back in case TextSpan are created
        // while no table cells exist.  This garbaged text will show up after the end of the table.
        super();
        this.table = table;
      }

      @Override
      public void startElement(int tagHashId, AttributeList atts) {
        super.startElement(tagHashId, atts);
        switch (tagHashId) {
        case TagDereferencer.TR:
          table.startRow(atts, currStyle);
          break;
        case TagDereferencer.TD:
        case TagDereferencer.TH:
          currTile = table.startCell(atts, currStyle);
          break;
        }
      }

      @Override
      public void endElement(int tagHashId) {
        super.endElement(tagHashId);
        switch (tagHashId) {
        case TagDereferencer.TR:
          table.endRow();
          // fall thru
        case TagDereferencer.TD:
        case TagDereferencer.TH:
          currTile = Document.this; // reset default document tiles
          break;
        }
      }
    }

    private class SelectContentHandler extends BodyContentHandler {
      private Control select;
      private AttributeList currOptionAtts; // remembers curr OPTION attributes
      private StringBuffer sb; // To catenate the option text

      SelectContentHandler(Control select) {
        this.select = select;
        sb = new StringBuffer(64);
      }

      private void endCurrentOption() {
        if (currOptionAtts != null) {
          String item = sb.toString();
          String key = currOptionAtts.getAttributeValue("value");
          boolean selected = currOptionAtts.exists("selected");
          if (select instanceof MultiListBox) // must come first!
          {
            MultiListBox lb = (MultiListBox) select;
            lb.add(new Entry(key, item));
            if (selected) {
              lb.clearValues.addElement(lb.size() - 1);
            }
          } else if (select instanceof ListBox) {
            ListBox lb = (ListBox) select;
            int count = lb.size();
            lb.add(new Entry(key, item));
            if (selected) {
              lb.setSelectedIndex(lb.clearValueInt = count);
            }
          } else // ComboBox
          {
            ComboBox cb = (ComboBox) select;
            int count = cb.size();
            cb.add(new Entry(key, item));
            if (selected || (count == 0)) {
              cb.clearValueInt = count;
            }
          }
          sb.setLength(0);
          currOptionAtts = null;
        }
      }

      @Override
      public void startElement(int tagHashId, AttributeList atts) {
        // no other tags except OPTION are permitted within a SELECT
        if (tagHashId == TagDereferencer.OPTION) {
          endCurrentOption();
          currOptionAtts = new AttributeList(atts);
        }
      }

      @Override
      public void endElement(int tagHashId) {
        switch (tagHashId) {
        case TagDereferencer.SELECT:
          endCurrentOption();
          setContentHandler(prevContentHandler);
          break;
        case TagDereferencer.OPTION:
          endCurrentOption();
          break;
        }
      }

      @Override
      public void characters(String s) {
        if (currOptionAtts != null) {
          sb.append(s);
        }
      }
    }

    private class TextAreaContentHandler extends BodyContentHandler {
      private MultiEdit textArea;
      private StringBuffer sb; // To concatenate the text of the TextArea

      TextAreaContentHandler(MultiEdit textArea) {
        this.textArea = textArea;
        sb = new StringBuffer(128);
      }

      @Override
      public void startElement(int tagHashId, AttributeList atts) {
        // no tags are permitted within a TEXTAREA (#CDATA element)
      }

      @Override
      public void endElement(int tagHashId) {
        if (tagHashId == TagDereferencer.TEXTAREA) {
          textArea.setText(textArea.clearValueStr = sb.toString());
          setContentHandler(prevContentHandler);
        }
      }

      @Override
      public void characters(String s) {
        sb.append(s);
      }
    }
  }

  static interface CustomLayout {
    public void layout(LayoutContext lc);
  }

  static interface StopLayout {
  }

  static interface SizeDelimiter {
    public int getMaxWidth();
  }

  static void layout(Control control, LayoutContext lc) {
    if (control instanceof CustomLayout) {
      ((CustomLayout) control).layout(lc);
    } else if (control.appObj != null) {
      ControlProperties cp = (ControlProperties) control.appObj;
      Style style = cp.style;
      if ((style.hasInitialValues() && style.isDisjoint)) {
        lc.disjoin();
      }

      //TextSpan.debug(lc,style,control.toString());
      if (control instanceof BR) {
        lc.incY += control.getPreferredHeight();
      } else {
        lc.verify(control.getPreferredWidth());
        int dif = (control.getFont().fm.height + Edit.prefH - control.getPreferredHeight()) / 2; // vertically align the controls with their texts
        if (dif < 0) {
          dif = 0;
        }

        int alignedPosX = lc.nextX;
        switch (style.alignment) {
        case Style.ALIGN_CENTER:
          alignedPosX += (lc.parentContainer.getWidth() - control.getPreferredWidth()) / 2;
          break;
        case Style.ALIGN_RIGHT:
          alignedPosX += lc.parentContainer.getWidth() - control.getPreferredWidth();
          break;
        case Style.ALIGN_LEFT:
          // default, left aligned
          break;
        }
        control.setRect(alignedPosX, lc.nextY + dif, Control.PREFERRED, Control.PREFERRED, lc.lastControl);
        control.getParent().incLastY(-dif);
        if (!cp.glue) {
          control.getParent().incLastX(control.fm.charWidth(' '));
        }
        lc.update(control.getWidth());
        lc.lastControl = control;
      }
    }

    if (control instanceof Container && !(control instanceof StopLayout)) {
      Control[] children = ((Container) control).getChildren();
      for (int i = children.length; --i >= 0; ) {
        layout(children[i], lc);
      }
    }
  }

  private int getMaxWidth(Control control, int maxWidth) {
    maxWidth = Math.max(maxWidth,
        control instanceof SizeDelimiter ? ((SizeDelimiter) control).getMaxWidth() : control.getPreferredWidth());

    if (control instanceof Container) {
      Control[] children = ((Container) control).getChildren();
      for (int i = children.length; --i >= 0;) {
        maxWidth = getMaxWidth(children[i], maxWidth);
      }
    }
    return maxWidth;
  }

  /** Layout the controls of this document. */
  @Override
  public void initUI() {
    setBackColor(parent.getBackColor());
    //flsobral@tc126_36: restore scrolls default colors.
    sbV.setBackColor(UIColors.controlsBack);
    sbH.setBackColor(UIColors.controlsBack);
    sbV.focusTraversable = sbH.focusTraversable = false;
    sbV.setValue(0);
    sbH.setValue(0);
    int maxWidth = getMaxWidth(this, 0);
    resize(maxWidth, 60000);
    layout(this, new LayoutContext(getClientRect().width - sbV.getPreferredWidth(), this));
    resize();
  }

  public void scroll(int dir) {
    switch (dir) {
    case RIGHT:
      sbH.blockScroll(true);
      break;
    case LEFT:
      sbH.blockScroll(false);
      break;
    case BOTTOM:
      sbV.blockScroll(true);
      break;
    case TOP:
      sbV.blockScroll(false);
      break;
    }
  }

  @Override
  public void reposition() {
    super.reposition(false);
    sbV.reposition();
    sbH.reposition();
  }

  void resetWith(String url) // guich@tc114_28
  {
    Hashtable ht = new Hashtable(URI.decode(url.replace('&', '\n'))); //flsobral@tc115_3: Decode the received String.
    for (int i = vForms.size(); --i >= 0;) {
      Form f = (Form) vForms.items[i];
      Vector inputs = f.inputs;
      for (int j = inputs.size(); --j >= 0;) {
        Control item = (Control) inputs.items[j];
        ControlProperties cl = (ControlProperties) item.appObj;
        String name = cl.name, v;
        if (name != null && (v = (String) ht.get(name)) != null && v.length() > 0) {
          Form.setValue(item, v);
        }
      }
    }
  }
}
