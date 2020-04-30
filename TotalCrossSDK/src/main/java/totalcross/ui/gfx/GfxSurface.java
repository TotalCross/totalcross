// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

/**
 * GfxSurface is an abstract class that is extended by drawing surfaces, which can have a Graphics.
 * <p>
 * Control and Image are the only two classes that implement the GfxSurface interface.
 * If any other class tries to extend GfxSurface, a RuntimeException will be thrown at the device.
 */

public abstract class GfxSurface {
  public abstract int getX();

  public abstract int getY();

  public abstract int getWidth();

  public abstract int getHeight();
}
