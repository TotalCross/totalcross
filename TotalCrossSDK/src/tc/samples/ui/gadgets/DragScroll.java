package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;

public class DragScroll extends Container
{
   public void initUI()
   {
      ScrollContainer sc;
      MultiEdit me;

      add(new Label("Click and drag over the controls"),LEFT,TOP);

      add(sc = new ScrollContainer());

      sc.setBorderStyle(BORDER_SIMPLE);
      sc.setRect(LEFT + 10, AFTER + 10, FILL - 20, FILL - 20);

      ListBox lb = new ListBox(new String[]
      {
          "Version is " + Settings.versionStr,
          "Platform is " + Settings.platform
      });
      sc.add(lb);
      lb.setRect(0, 0, sc.getWidth()-20 , sc.getClientRect().height);

      me = new MultiEdit(10,5);
      sc.add(me, AFTER, TOP, SAME,SAME);
      me.setEditable(false);
      me.setText("SuperWaba interprets Java Bytecodes. TotalCross uses a proprietary set of bytecodes to improve program´s security and performance: TotalCross is about two times faster than SuperWaba. The translation between the java bytecodes to our opcodes is done automatically when the application is deployed. Regarding security, using SuperWaba is very easy to recover the sources from the application's PDB file. We can extract the .class files from the PDB and then decompile them to the .java files. In TotalCross this is IMPOSSIBLE: there are no decompilers. So, don't forget to take backups of your source files, because it will be impossible to recover them. Don't trust developers, trust only your set of backups!" );

      String []items = // taken from HelloWorld
      {
         "Version is " + Settings.versionStr,
         "Platform is " + Settings.platform,
         "User is " + Settings.userName,
         "Pen is " + (Settings.keyboardFocusTraversable ? "missing" : "available"),
         "Vistual keyboard is " + Settings.virtualKeyboard,
         "Screen is " + Settings.screenWidth + "x" + Settings.screenHeight,
         "Screen bpp is " + Settings.screenBPP,
         "timeZone is " + Settings.timeZone,
         "dateFormat is " + Settings.dateFormat,
         "dateSeparator is " + Settings.dateSeparator,
         "decimalSeparator is " + Settings.decimalSeparator,
         "thousandsSeparator is " + Settings.thousandsSeparator,
         "timeSeparator is " + Settings.timeSeparator,
         "daylightSavings is " + Settings.daylightSavings,
         "is24Hour is " + Settings.is24Hour,
         "weekStart is " + Settings.weekStart,
         "Battery is at " + Vm.getRemainingBattery() + "%",
         "Free memory is at " + Vm.getFreeMemory(),
         "Rom serial number is " + Settings.romSerialNumber,
         "Rom version is " + Settings.romVersion,
         "Device id is " + Settings.deviceId,
         "App path is " + Settings.appPath,
         "Version is " + Settings.versionStr,
         "Platform is " + Settings.platform,
         "User is " + Settings.userName
      };
      lb = new ListBox(items);
      sc.add(lb);
      lb.enableHorizontalScroll();
      lb.setRect(AFTER, TOP, SAME,SAME);
   }

}
