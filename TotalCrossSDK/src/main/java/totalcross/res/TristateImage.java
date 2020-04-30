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

package totalcross.res;

import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;

/** An image that has three states: normal, pressed and disabled.
 * 
 * Used in the Android user interface style.
 * 
 * @since TotalCross 1.3
 */

public class TristateImage {
  private Hashtable htNormal = new Hashtable(5);
  private Hashtable htPressed = new Hashtable(5);
  private Hashtable htDisabled = new Hashtable(5);
  private Image base;

  public TristateImage(String name) throws ImageException, IOException {
    base = new Image(name);
  }

  public void flush() {
    htNormal.clear();
    htPressed.clear();
    htDisabled.clear();
  }

  public Image getCopy() throws ImageException {
    return base.getCopy();
  }

  public Image getNormalInstance(int width, int height, int backColor) throws ImageException {
    return getNormalInstance(width, height, backColor, true);
  }

  private Image getNormalInstance(int width, int height, int backColor, boolean apply) throws ImageException {
    int hash;
    StringBuffer sb = new StringBuffer(20);
    hash = Convert.hashCode(sb.append((width << 16) | height).append('|').append(backColor));
    Image ret = (Image) htNormal.get(hash);
    if (ret == null) {
      ret = scaleTo(width, height);
      ret.applyColor(backColor);
      htNormal.put(hash, ret);
    }
    return ret;
  }

  public Image getDisabledInstance(int width, int height, int backColor) throws ImageException {
    int hash;
    StringBuffer sb = new StringBuffer(20);
    hash = Convert.hashCode(sb.append((width << 16) | height).append('|').append(backColor));
    Image ret = (Image) htDisabled.get(hash);
    if (ret == null) {
      htDisabled.put(hash, ret = getNormalInstance(width, height, backColor, false).getFadedInstance());
    }
    return ret;
  }

  public Image getPressedInstance(int width, int height, int backColor, int pressColor, boolean enabled)
      throws ImageException {
    int hash;
    StringBuffer sb = new StringBuffer(20);
    hash = Convert
        .hashCode(sb.append((width << 16) | height).append('|').append(backColor).append(enabled).append(pressColor));
    Image ret = (Image) htPressed.get(hash);
    if (ret == null) {
      if (pressColor != -1) {
        ret = scaleTo(width, height);
        ret.applyColor2(pressColor);
      } else {
        ret = getNormalInstance(width, height, backColor, false)
            .getTouchedUpInstance(Color.getBrightness(backColor) > (256 - 32) ? (byte) -64 : (byte) 32, (byte) 0);
      }
      if (!enabled) {
        ret = ret.getFadedInstance();
      }
      htPressed.put(hash, ret);
    }
    return ret;
  }

  private Image scaleTo(int w, int h) throws ImageException {
    Image img = base.getSmoothScaledInstance(w, h);
    if (img == base) {
      img = base.getFrameInstance(0);
    }
    return img;
  }
}
