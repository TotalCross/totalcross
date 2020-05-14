// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
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
