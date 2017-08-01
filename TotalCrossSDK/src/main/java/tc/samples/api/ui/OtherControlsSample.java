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
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Check;
import totalcross.ui.Control;
import totalcross.ui.Label;
import totalcross.ui.MultiListBox;
import totalcross.ui.Ruler;
import totalcross.ui.ScrollBar;
import totalcross.ui.ScrollContainer;
import totalcross.ui.Slider;
import totalcross.ui.SpinList;
import totalcross.ui.dialog.ColorChooserBox;
import totalcross.ui.dialog.FileChooserBox;
import totalcross.ui.dialog.InputBox;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.dialog.TimeBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.unit.UIRobotEvent;

public class OtherControlsSample extends BaseContainer
{
  Label lStatus;
  ScrollContainer sc;

  @Override
  public void initUI()
  {
    try
    {
      super.initUI();
      setTitle("Other controls");
      sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);

      sc.add(lStatus = new Label("",CENTER), LEFT,AFTER);
      lStatus.setHighlighted(true);

      sc.add(new Label("TimeBox"),LEFT,AFTER+gap);
      addClock();

      final Button btnInput;
      sc.add(new Label("InputBox"),LEFT,AFTER+gap);
      sc.add(btnInput = new Button("Click to input your name"),LEFT,AFTER);
      btnInput.addPressListener(new PressListener()
      {
        @Override
        public void controlPressed(ControlEvent e)
        {
          InputBox ib = new InputBox("InputBox","Please enter your name:","");
          ib.popup();
          String s = ib.getValue();
          if (s != null) {
            lStatus.setText(s);
          }
        }
      });

      final Button btnChooseColor;
      sc.add(new Label("ColorChooserBox"),LEFT,AFTER+gap);
      sc.add(btnChooseColor = new Button("Choose new background color"),LEFT,AFTER);
      btnChooseColor.addPressListener(new PressListener()
      {
        @Override
        public void controlPressed(ControlEvent e)
        {
          ColorChooserBox ccb = new ColorChooserBox(getBackColor());
          ccb.popup();
          if (ccb.choosenColor != -1)
          {
            sc.setBackColor(ccb.choosenColor);
            Control[] c = sc.getBagChildren();
            for (int i =0; i < c.length; i++) {
              c[i].setBackColor(ccb.choosenColor);
            }
            repaint();
          }
        }
      });

      sc.add(new Label("SpinList"),LEFT,AFTER+gap);
      SpinList sl;
      sc.add(sl = new SpinList(new String[]{"Today","Day [1,31]"}, !Settings.fingerTouch),LEFT,AFTER,Settings.fingerTouch?FILL:PREFERRED,PREFERRED);
      sl.hAlign = CENTER;

      sc.add(new Label("Horizontal ScrollBar"),LEFT,AFTER+gap);
      sc.add(sb2 = new ScrollBar(ScrollBar.HORIZONTAL), LEFT,AFTER, SCREENSIZE+50, PREFERRED);
      sb2.setVisibleItems(10);
      sb2.setValues(1,1,1,6);

      sc.add(new Label("Ruler"),LEFT,AFTER+gap);
      sc.add(new Ruler(),LEFT,AFTER+gap);

      sc.add(new Label("FileChooser"),LEFT,AFTER+gap);
      addFileChooser();
    }
    catch (Exception ee)
    {
      MessageBox.showException(ee,true);
    }
  }

  Button btnClock;
  Slider sb1;
  ScrollBar sb2;

  private void addClock() throws ImageException
  {
    Image clock = new Image(fmH,fmH);
    int xx = clock.getWidth()-1;
    int yy = clock.getHeight()-1;
    Graphics g = clock.getGraphics();
    g.foreColor = Color.BLUE;
    g.drawCircle(xx/2,yy/2,xx/2);
    g.drawLine(xx/2,yy/2,xx,yy/2);
    g.drawLine(xx/2,yy/2,xx/2,yy/3);
    btnClock = new Button(clock);
    btnClock.setBorder(Button.BORDER_NONE);
    sc.add(btnClock, LEFT, AFTER);
  }

  public void addFileChooser()
  {
    final Button btn,btn2;
    final Check ch;
    sc.add(ch = new Check("Multiple selection"),LEFT,AFTER);
    sc.add(btn = new Button("Choose file"),LEFT,AFTER+gap);
    sc.add(btn2 = new Button("Delete file"),AFTER+gap,SAME);
    btn2.setEnabled(false);
    btn.addPressListener(
        new PressListener()
        {
          @Override
          public void controlPressed(ControlEvent e)
          {
            try
            {
              FileChooserBox fcb = new FileChooserBox(null);
              fcb.showPreview = true;
              fcb.multipleSelection = ch.isChecked(); // guich@tc115_4
              fcb.mountTree("device/");
              fcb.popup();
              String s = fcb.getAnswer();
              if (s == null) {
                setInfo("Cancelled");
              } else
                if (fm.stringWidth(s) > getWidth())
                {
                  lStatus.setMarqueeText(s, 100, 1, -8);
                  setInfo("Scroll up to see returned value");
                } else {
                  setInfo(s);
                }
              btn2.setEnabled(s != null);
            }
            catch (Exception ee)
            {
              MessageBox.showException(ee,true);
            }
          }
        });
    btn2.addPressListener(new PressListener()
    {
      @Override
      public void controlPressed(ControlEvent e)
      {
        try
        {
          String s = lStatus.getText();
          new File(s,File.DONT_OPEN).delete();
        }
        catch (Exception ee)
        {
          MessageBox.showException(ee,false);
        }
      }
    });
  }

  @Override
  public void onEvent(Event event)
  {
    if (event instanceof UIRobotEvent){
      lStatus.setMarqueeText(event.type == UIRobotEvent.ROBOT_SUCCEED ? "Robot succeed" : "Robot failed: "+((UIRobotEvent)event).failureReason, 100,1,-5);
    }else
      if (event.type == ControlEvent.PRESSED)
      {
        if (event.target == btnClock)
        {
          TimeBox tb = new TimeBox();
          tb.popup();
          lStatus.setText(tb.getTime().toString());
        }
        else
          if (event.target == sb1 || event.target == sb2)
          {
            int value = ((ScrollBar)event.target).getValue();
            sb1.setValue(value);
            sb2.setValue(value);
          }
          else
            if (event.target instanceof MultiListBox) {
              lStatus.setText("Last selected: "+((MultiListBox)event.target).getLastSelectedItem());
            }
      }
  }
}
