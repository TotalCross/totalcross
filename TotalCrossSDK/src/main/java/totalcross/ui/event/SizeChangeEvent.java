// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.event;

public class SizeChangeEvent extends Event<SizeChangeHandler> {

  private static Type<SizeChangeHandler> TYPE;

  public final int width;
  public final int height;

  public static final Type<SizeChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<SizeChangeHandler>();
    }
    return TYPE;
  }
  
  public SizeChangeEvent(Object target, int width, int height) {
    this.target = target;
    this.width = width;
    this.height = height;
  }

  @Override
  public void dispatch(SizeChangeHandler listener) {
    listener.onSizeChange(this);
  }

//  @Override
//  public Type<FontChangeHandler> getAssociatedType() {
//    return TYPE;
//  }
}
