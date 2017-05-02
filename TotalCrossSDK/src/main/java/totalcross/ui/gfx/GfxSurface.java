/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package totalcross.ui.gfx;

/**
 * GfxSurface is an abstract class that is extended by drawing surfaces, which can have a Graphics.
 * <p>
 * Control and Image are the only two classes that implement the GfxSurface interface.
 * If any other class tries to extend GfxSurface, a RuntimeException will be thrown at the device.
 */

public abstract class GfxSurface
{
   public abstract int getX();
   public abstract int getY();
   public abstract int getWidth();
   public abstract int getHeight();
}
