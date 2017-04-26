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

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class DynScrollContainerSample extends BaseContainer
{
   /**
    * Test DynamicScrollContainer and it's ability to display thousands of rows.
    * 
    * Performance is determined by cost of object creation of implemented AbstractView and calculating AbstractView.getHeight() and number of views to create.
    * Once determined the scroll container easily handles the scrolling of the components.
    * 
    * Contributed by Tomek Korzeniewski
    */

   private Button goButton;
   private Edit evenHeightEdit;
   private Edit oddHeightEdit;
   private Edit rowCountEdit;
   private DynamicScrollContainer vsc;
   private Check dynamicChk;
   private int rowCount0,oddHeight0,evenHeight0;

   private Edit add(String text)
   {
      add(new Label(text), LEFT, AFTER, PARENTSIZE+80, PREFERRED);
      Edit ed = new Edit();
      ed.setKeyboard(Edit.KBD_NUMERIC);
      add(ed, RIGHT, SAME, PARENTSIZE+20, PREFERRED);
      return ed;
   }
   
   public void initUI()
   {
      super.initUI();
      setTitle("Dynamic ScrollContainer");
      
      add(new Spacer(0,0),LEFT,TOP+gap); // reset after position
      rowCountEdit = add("Number of rows to create: ");
      oddHeightEdit = add("Odd view height:");
      evenHeightEdit = add("Even view height:");
      
      dynamicChk = new Check("Dynamic height");
      add(dynamicChk, LEFT, AFTER);

      goButton = new Button("Generate");
      goButton.setBackColor(Color.GREEN);
      add(goButton, RIGHT, SAME, PARENTSIZE+20, PREFERRED);

      vsc = new DynamicScrollContainer();
      vsc.setBackColor(Color.WHITE);
      vsc.setBorderStyle(BORDER_SIMPLE);
      add(vsc, LEFT, AFTER+gap, FILL, FILL-1);

      rowCountEdit.setText(String.valueOf(rowCount0=2000));
      oddHeightEdit.setText(String.valueOf(oddHeight0=fmH));
      evenHeightEdit.setText(String.valueOf(evenHeight0=fmH*3/2));
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == goButton)
      {
         int rowCount = rowCount0;
         int oddHeight = oddHeight0;
         int evenHeight = evenHeight0;
         try
         {
            rowCount = Convert.toInt(rowCountEdit.getText());
         }
         catch (Exception e)
         {
            rowCountEdit.setText(rowCount + "");
         }
         try
         {
            oddHeight = Convert.toInt(oddHeightEdit.getText());
         }
         catch (Exception e)
         {
            oddHeightEdit.setText(oddHeight + "");
         }
         try
         {
            evenHeight = Convert.toInt(evenHeightEdit.getText());
         }
         catch (Exception e)
         {
            evenHeightEdit.setText(evenHeight + "");
         }

         ProgressBox pb = new ProgressBox("Generating", "Creating datasource, please wait...", null);
         pb.popupNonBlocking();
         DynamicScrollContainer.DataSource datasource = new DynamicScrollContainer.DataSource(rowCount);

         int start = Vm.getTimeStamp();
         for (int i = 0; i < rowCount; i++)
         {
            DynSCTestView view = new DynSCTestView(i, font);
            view.height = i % 2 == 0 ? evenHeight : oddHeight;
            datasource.addView(view);
         }

         pb.unpop();
         vsc.setDataSource(datasource);
         vsc.scrollToView(datasource.getView(0));
         setInfo("Time to create datasource: " + (Vm.getTimeStamp() - start)+" ms");
      }
      if (event.type == ControlEvent.PRESSED && event.target == dynamicChk)
      {
         DynSCTestView.dynamicHeight = dynamicChk.isChecked();

      }
   }
}
