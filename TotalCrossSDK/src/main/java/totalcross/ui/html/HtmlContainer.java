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

import totalcross.sys.SpecialKeys;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;

/**
 * <code>HtmlContainer</code> renders an HTML Page.<br><br>
 * Note that the form controls back and fore colors are defined by
 * UIColors.htmlContainerControlsFore and UIColors.htmlContainerControlsBack.
 * <p>
 * When a link is clicked, a PRESSED event is thrown, with this HtmlContainer as 
 * target. The link can then be retrieved with the pressedLink property.
 *
 * @see #pressedLink
 * @see totalcross.ui.UIColors#htmlContainerControlsFore
 * @see totalcross.ui.UIColors#htmlContainerControlsBack
 */
public class HtmlContainer extends Container {
  protected Document doc;

  public String pressedLink;

  /**
   * Constructor
   */
  public HtmlContainer() {
    this.focusTraversable = true; // kmeehl@tc100
  }

  public Document getDocument() {
    return doc;
  }

  /**
   * Sets the document to be displayed in this HtmlContainer.
   * @param doc document to be rendered.
   */
  public void setDocument(Document doc) {
    this.doc = doc;
    this.backColor = doc.getBackColor(); //flsobral@tc126_36: added support to bgcolor attribute in body tag.
    removeAll();
    add(doc, LEFT, TOP, FILL, FILL);
  }

  static HtmlContainer getHtmlContainer(Control c) {
    for (Control control = c.getParent(); control != null; control = control.getParent()) {
      if (control instanceof HtmlContainer) {
        return (HtmlContainer) control;
      }
    }
    return null;
  }

  void postLinkEvent(String link) {
    pressedLink = link;
    postEvent(new ControlEvent(ControlEvent.PRESSED, this));
  }

  @Override
  public void onEvent(Event e) {
    if (e.type == KeyEvent.SPECIAL_KEY_PRESS && e.target == this) {
      KeyEvent ke = (KeyEvent) e;
      if (ke.isActionKey()) {
        setHighlighting();
      } else if (ke.key == SpecialKeys.RIGHT) {
        doc.scroll(RIGHT);
      } else if (ke.key == SpecialKeys.LEFT) {
        doc.scroll(LEFT);
      } else if (ke.isUpKey()) {
        doc.scroll(TOP);
      } else if (ke.isDownKey()) {
        doc.scroll(BOTTOM);
      }
    }
  }

  @Override
  public void reposition() {
    super.reposition(false);
    if (doc != null) {
      doc.reposition();
    }
  }

  /** Resets the given form of the current document with the given parameters.
   * @param url The url with the parameter and values.
   */
  public void resetWith(String url) // guich@tc114_28
  {
    if (doc != null) {
      doc.resetWith(url);
    }
  }
}
