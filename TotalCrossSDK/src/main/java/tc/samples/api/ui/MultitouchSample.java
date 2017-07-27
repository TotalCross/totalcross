/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import tc.samples.api.BaseContainer;
import totalcross.sys.Settings;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.MultiTouchListener;
import totalcross.ui.image.Image;

public class MultitouchSample extends BaseContainer
{
  ImageControl ic;
  public static Image screenShot;
  private static Image lata;

  @Override
  public void initUI()
  {
    super.initUI();
    isSingleCall = true;

    if (!Settings.isOpenGL && !Settings.onJavaSE){
      add(new Label("This sample works only on iOS, Android and Windows Phone."),CENTER,CENTER);
    }else {
      try
      {
        super.initUI();
        if (lata == null) {
          lata = new Image("ui/images/lata.jpg");
        }
        ic = new ImageControl(screenShot != null ? screenShot : lata);
        screenShot = null;
        ic.allowBeyondLimits = false;
        ic.setEventsEnabled(true);
        updateStatus();
        add(ic,LEFT+gap,TOP+gap,FILL-gap,FILL-gap);
        ic.addMultiTouchListener(new MultiTouchListener()
        {
          @Override
          public void scale(MultiTouchEvent e)
          {
            updateStatus();
          }
        });
      }
      catch (Exception e)
      {
        MessageBox.showException(e,false);
      }
    }
  }

  private void updateStatus()
  {
    setInfo(ic.getImageWidth()+"x"+ic.getImageHeight());
  }
}