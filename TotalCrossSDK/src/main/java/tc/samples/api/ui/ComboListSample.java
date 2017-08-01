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
import totalcross.ui.Button;
import totalcross.ui.CaptionPress;
import totalcross.ui.ComboBox;
import totalcross.ui.ImageList;
import totalcross.ui.Label;
import totalcross.ui.ListBox;
import totalcross.ui.MultiListBox;
import totalcross.ui.PopupMenu;
import totalcross.ui.ScrollContainer;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.util.IntHashtable;

public class ComboListSample extends BaseContainer
{
  private int lastSel;
  Button btn;

  @Override
  public void initUI()
  {
    try
    {
      super.initUI();
      setTitle("ComboBox and ListBox");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);

      sc.add(btn = new Button("  Clear  "),CENTER,TOP);

      String[] items = {"One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Um","Dois","Tres","Quatro","Cinco","Seis","Sete","Oito","Nove","Dez"};
      ComboBox cb = new ComboBox(items);
      cb.caption = "Numbers with popup";
      cb.captionIcon = getAwesomeImage('\uF12D',fmH,Color.BLACK);
      cb.popupTitle = "Select the item";
      cb.enableSearch = false;
      cb.setBackColor(Color.BRIGHT);
      cb.checkColor = Color.BLUE;
      sc.add(cb,LEFT,AFTER+gap,FILL,PREFERRED);
      final ComboBox cb1 = cb;
      cb.captionPress = new CaptionPress()
      {
        @Override
        public void onIconPress()
        {
          Vm.debug("on icon press");
          cb1.setSelectedIndex(-1);
        }

        @Override
        public void onCaptionPress()
        {
          Vm.debug("on caption press");
          cb1.setSelectedIndex(-1);
        }
      };

      ComboBox.usePopupMenu = false;
      cb = new ComboBox(items);
      cb.caption = "Numbers with dropdown";
      cb.setBackColor(Color.BRIGHT);
      sc.add(cb,LEFT,AFTER+gap ,FILL,PREFERRED);
      final ComboBox cb2 = cb;
      ComboBox.usePopupMenu = true;

      String[] items2 = {"cyan","black","blue","bright","green","dark","magenta","orange","pink","red","white","yellow"};
      cb = new ComboBox(items2);
      cb.popupTitle = "Select the item";
      cb.setBackColor(Color.BRIGHT);
      cb.checkColor = Color.CYAN;
      final ComboBox cb3 = cb;
      sc.add(cb,LEFT,AFTER+gap,FILL,PREFERRED+gap);

      btn.addPressListener(new PressListener()
      {
        @Override
        public void controlPressed(ControlEvent e)
        {
          cb1.setSelectedIndex(-1);
          cb2.setSelectedIndex(-1);
          cb3.setSelectedIndex(-1);
        }
      });

      ListBox l = new ListBox(items);
      l.setBackColor(SELCOLOR);
      sc.add(l,LEFT,AFTER+gap,FILL,FONTSIZE+725);

      sc.add(new Label("Multi-items"),LEFT,AFTER+gap);
      sc.add(new ComboBox(new MultiListBox(items)),SAME,AFTER+gap,PREFERRED+gap,PREFERRED);

      MultiListBox lbox;
      String []items3 = {"one","two","three"};
      sc.add(lbox = new MultiListBox(items3),LEFT+2,AFTER+gap,PREFERRED+gap,PREFERRED);
      lbox.setOrderIsImportant(true);
      // change the fore color of some ListBox items. See also ListBox.ihtBackColors.
      IntHashtable htf = new IntHashtable(1);
      htf.put(0,Color.RED);
      htf.put(1,Color.GREEN);
      htf.put(2,Color.BLUE);
      lbox.ihtForeColors = htf;

      // image list
      sc.add(new Label("ImageList"),LEFT,AFTER+gap);
      Image[] images = new Image[ImageBookSample.images.length];
      for (int i = 0; i < images.length; i++) {
        images[i] = new Image(ImageBookSample.images[i]).smoothScaledFixedAspectRatio(fmH*2,true);
      }
      sc.add(new ComboBox(new ImageList(images)),LEFT+2,AFTER+gap,PREFERRED+gap,PREFERRED);

      final Button btn1 = new Button(" Popup menu ",new Image("totalcross/res/android/comboArrow.png"), LEFT, fmH/2);
      sc.add(btn1,LEFT,AFTER+gap);
      btn1.addPressListener(new PressListener()
      {
        @Override
        public void controlPressed(ControlEvent e)
        {
          try
          {
            String[] items =
              {
                  "Always",
                  "Never",
                  "Only in Silent mode",
                  "Only when not in Silent mode",
                  "None the answers above",
                  "All the answers above"
              };
            PopupMenu pm = new PopupMenu("Vibrate",items);
            pm.setBackColor(Color.BRIGHT);
            pm.setCursorColor(Color.CYAN);
            pm.setSelectedIndex(lastSel);
            pm.popup();
            lastSel = pm.getSelectedIndex();
            setInfo(lastSel == -1 ? "Cancelled" : "Selected "+lastSel);
          }
          catch (Exception ee)
          {
            MessageBox.showException(ee,true);
          }
        }
      });
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }
}