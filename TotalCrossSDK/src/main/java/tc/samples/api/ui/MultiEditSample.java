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
import totalcross.sys.Vm;
import totalcross.ui.CaptionPress;
import totalcross.ui.Label;
import totalcross.ui.MultiEdit;
import totalcross.ui.ScrollContainer;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.gfx.Color;

public class MultiEditSample extends BaseContainer
{
  public static String TEXT = "Laurel and Hardy were one of the most popular and critically acclaimed comedy double acts of the " +
      "early Classical Hollywood era of American cinema. Composed of thin Englishman Stan Laurel (1890-1965) " +
      "and heavyset American Oliver Hardy (1892-1957), they became well known during the late 1920s to the " +
      "mid-1940s for their slapstick comedy, with Laurel playing the clumsy and childlike friend of the pompous " +
      "Hardy. They made over 100 films together, initially two-reelers (short films) before expanding into feature " +
      "length films in the 1930s. Their films include Sons of the Desert (1933), the Academy Award-winning short " +
      "film The Music Box (1932), Babes in Toyland (1934), and Way Out West (1937). Hardy's catchphrase, \"Well, " +
      "here's another nice mess you've gotten me into!\", is still widely recognized. [Wikipedia]";

  @Override
  public void initUI()
  {
    try
    {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);
      MultiEdit c;

      // IN MATERIAL UI, IS VERY IMPORTANT THAT THE CAPTION IS SET BEFORE ADDING THE CONTROL.
      c = new MultiEdit(5,1);
      c.caption = "Please enter the text in this MultiEdit";
      c.captionIcon = getAwesomeImage('\uf12d', fmH*75/100, Color.BLACK);
      c.setBackColor(0x0000AA);
      c.fillColor = 0xEEEEFF;
      sc.add(c, LEFT,AFTER+gap,FILL,PREFERRED);

      final MultiEdit c1 = c;
      c.captionPress = new CaptionPress()
      {
        @Override
        public void onIconPress()
        {
          Vm.debug("on icon press");
          c1.clear();
        }

        @Override
        public void onCaptionPress()
        {
          Vm.debug("on caption press");
          c1.clear();
        }
      };

      sc.add(c = new MultiEdit(2,0), LEFT,AFTER+gap,FILL,PREFERRED);
      c.setBackColor(0x00AA00);
      c.fillColor = 0xEEFFEE;

      sc.add(new Label("Non-editable MultiEdit:"),LEFT,AFTER+gap);
      MultiEdit mEdit;
      mEdit = new MultiEdit("",6,1);
      mEdit.drawDots = false;
      mEdit.justify = true;
      mEdit.setEditable(false);
      MultiEdit.hasCursorWhenNotEditable = false;
      sc.add(mEdit,LEFT,AFTER+gap);
      mEdit.setText(TEXT); //eventually
      mEdit.requestFocus();

      Label l;
      sc.add(new Label("Aligned Label:"),LEFT,AFTER+50);
      sc.add(l = new Label(TEXT));
      l.align = FILL;
      l.autoSplit = true;
      l.setRect(LEFT,AFTER+2,FILL-20,PREFERRED);
      l.backgroundType = Label.VERTICAL_GRADIENT_BACKGROUND;
      l.firstGradientColor = Color.YELLOW;
      l.secondGradientColor = Color.RED;
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }
}