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



package totalcross.ui.html;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

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
public class HtmlContainer extends Container
{
   protected Document doc;
   
   public String pressedLink;

   /**
   * Constructor
   */
   public HtmlContainer()
   {
      this.focusTraversable = true; // kmeehl@tc100
   }

   public Document getDocument()
   {
      return doc;
   }
   
   /**
   * Sets the document to be displayed in this HtmlContainer.
   * @param doc document to be rendered.
   */
   public void setDocument(Document doc)
   {
      this.doc = doc;
      this.backColor = doc.getBackColor(); //flsobral@tc126_36: added support to bgcolor attribute in body tag.
      removeAll();
      add(doc,LEFT,TOP,FILL,FILL);
   }

   static HtmlContainer getHtmlContainer(Control c)
   {
      for (Control control = c.getParent(); control != null; control = control.getParent())
         if (control instanceof HtmlContainer)
            return (HtmlContainer) control;
      return null;
   }
   
   void postLinkEvent(String link)
   {
      pressedLink = link;
      postEvent(new ControlEvent(ControlEvent.PRESSED, this));
   }
   
   public void onEvent(Event e)
   {
      if (e.type == KeyEvent.SPECIAL_KEY_PRESS && e.target == this)
      {
         KeyEvent ke = (KeyEvent)e;
         if (ke.isActionKey())
            setHighlighting();
         else
         if (ke.key == SpecialKeys.RIGHT)
            doc.scroll(RIGHT);
         else
         if (ke.key == SpecialKeys.LEFT)
            doc.scroll(LEFT);
         else
         if (ke.isUpKey())
            doc.scroll(TOP);
         else
         if (ke.isDownKey())
            doc.scroll(BOTTOM);         
      }
   }
   
   public void reposition()
   {
      super.reposition(false);
      doc.reposition();
   }

   /** Resets the given form of the current document with the given parameters.
    * @param url The url with the parameter and values.
    */
   public void resetWith(String url) // guich@tc114_28
   {
      if (doc != null)
         doc.resetWith(url);
   }
}
