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

import tc.samples.api.*;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class ComboListSample extends BaseContainer
{
   private int lastSel;

   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("ComboBox and ListBox");
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         
         String[] items = {"One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten","Um","Dois","Tres","Quatro","Cinco","Seis","Sete","Oito","Nove","Dez"};
         ComboBox cb = new ComboBox(items);
         cb.popupTitle = "Select the item";
         cb.enableSearch = false;
         cb.setBackColor(Color.BRIGHT);
         cb.checkColor = Color.GREEN;
         sc.add(cb,LEFT,AFTER,FILL,PREFERRED+gap);
         
         String[] items2 = {"cyan","black","blue","bright","green","dark","magenta","orange","pink","red","white","yellow"};
         cb = new ComboBox(items2);
         cb.popupTitle = "Select the item";
         cb.setBackColor(Color.BRIGHT);
         cb.checkColor = Color.CYAN;
         sc.add(cb,LEFT,AFTER+gap,FILL,PREFERRED+gap);

         ListBox l = new ListBox(items);
         l.setBackColor(SELCOLOR);
         sc.add(l,LEFT,AFTER+gap,FILL,fmH*7+4);
    
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
         
         final Button btn1 = new Button(" Popup menu ",new Image("totalcross/res/android/comboArrow.png"), LEFT, fmH/2);
         sc.add(btn1,LEFT,AFTER+gap);
         btn1.addPressListener(new PressListener()
         {
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