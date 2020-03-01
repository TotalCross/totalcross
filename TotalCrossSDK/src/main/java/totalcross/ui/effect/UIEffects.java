/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.   
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

package totalcross.ui.effect;

import totalcross.ui.Control;
import totalcross.ui.gfx.Graphics;

public abstract class UIEffects {
  public enum Effects {
    NONE, MATERIAL,
  }

  public static int X_UNKNOWN = -9999999; // used clicked outside the component
  public static int duration = 200;
  public static Effects defaultEffect = Effects.NONE;

  public boolean darkSideOnPress;
  public boolean enabled = true;
  public int color = -1;
  /** The alpha value to be applied during the press, range from 0 to 255 (default value). */
  public int alphaValue = 0xFF;

  public static UIEffects get(Control c) {
    switch (defaultEffect) {
    case MATERIAL:
      return new MaterialEffect(c);
    default:
      return null;
    }
  }

  public abstract boolean isRunning();

  public abstract void startEffect();

  public abstract void paintEffect(Graphics g);
}
