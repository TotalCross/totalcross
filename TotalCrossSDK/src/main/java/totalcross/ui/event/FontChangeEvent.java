/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.   
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

package totalcross.ui.event;

import totalcross.ui.Control;
import totalcross.ui.font.Font;

public class FontChangeEvent extends Event<FontChangeHandler> {

  private static Type<FontChangeHandler> TYPE;

  public final Font font;

  public static final Type<FontChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<FontChangeHandler>();
    }
    return TYPE;
  }

  public FontChangeEvent(Control target, Font font) {
    this.target = target;
    this.font = font;
  }

  @Override
  public void dispatch(FontChangeHandler listener) {
    listener.onFontChange(this);
  }

//  @Override
//  public Type<FontChangeHandler> getAssociatedType() {
//    return TYPE;
//  }
}
