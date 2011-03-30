/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: HighlightListener.java,v 1.5 2011-01-04 13:19:16 guich Exp $

package totalcross.ui.event;

/** Interface used to listen to Highlight events. */

public interface HighlightListener
{
   /** A HIGHLIGHT_IN event was dispatched.
    * @see ControlEvent 
    */
   public void highlightIn(ControlEvent e);
   /** A HIGHLIGHT_OUT event was dispatched.
    * @see ControlEvent 
    */
   public void highlightOut(ControlEvent e);
}
