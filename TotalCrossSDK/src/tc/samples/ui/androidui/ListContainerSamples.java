package tc.samples.ui.androidui;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ListContainerSamples extends BaseContainer
{
   public ListContainerSamples()
   {
      helpMessage = "These are ListContainer samples in the Android user interface style. The information listed at startup shows the creation time (C), addition time (A), total time (T) and gc time/count during everything. Calling this sample will alternate between 30 and 3000 ListContainer items. Press back to go to the main menu.";
   }
   
   static int lastCount = -1;
   public void initUI()
   {
      try
      {
         super.initUI();

         int TOTAL_ITEMS = lastCount == 30 ? 3000 : 30; // increase this to 3000, for example
         lastCount = TOTAL_ITEMS;
         setInfo("Loading "+TOTAL_ITEMS+" items...");

         if (TOTAL_ITEMS > 1000)
            Flick.defaultLongestFlick = TOTAL_ITEMS * 3;
         
         // "on" image
         final Image off = new Image("totalcross/res/android/radioBkg.png");
         // "off" image is a composite of two images: on + selection
         final Image on = off.getFrameInstance(0);
         final Image ball = new Image("totalcross/res/android/radioSel.png");
         ball.applyColor2(Color.RED); // paint it
         on.getGraphics().drawImage(ball,0,0,Graphics.DRAW_PAINT,Color.WHITE,true);
         
         ListContainer lc = new ListContainer();
         add(lc, LEFT,TOP,FILL,FILL);
         
         ListContainer.Layout layout = lc.getLayout(5,2);
         layout.insets.set(50,10,50,10);
         layout.defaultLeftImage = off;
         layout.leftImageEnlargeIfSmaller = true;
         layout.defaultRightImage = off;
         layout.defaultRightImage2 = on;
         layout.boldItems[1] = layout.boldItems[3] = true;
         layout.controlGap = 10; // 10% of font's height
         layout.defaultItemColors[1] = Color.RED;
         layout.lineGap = 25; // 1/4 font's height
         layout.relativeFontSizes[2] = layout.relativeFontSizes[3] = -1;
         layout.positions[3] = RIGHT;
         layout.positions[4] = CENTER;
         layout.setup();
         
         ListContainer.Item c = new ListContainer.Item(layout);
         c.items = new String[]{"00011 ","BAR LANCHONETE CONRADO","Rio de Janeiro/Centro"," 99999,99","Brasil"};
         lc.addContainer(c);
         
         c = new ListContainer.Item(layout);
         c.items = new String[]{"00015 ","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana"," 80000,00","Também Brasil"};
         c.leftControl = new Check(" "); ((Check)c.leftControl).checkColor = Color.RED;
         lc.addContainer(c);
         
         c = new ListContainer.Item(layout);
         c.items = new String[]{"00015 ","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana"," 80000,00","Também Brasil"};
         c.leftControl = new Check(" "); ((Check)c.leftControl).checkColor = Color.RED;
         lc.addContainer(c);

         Vm.gc();
         int gcTime = Settings.gcTime;
         int gcCount = Settings.gcCount;
         int ini = Vm.getTimeStamp();
         Container all[] = new Container[TOTAL_ITEMS];
         for (int i = 0; i < all.length; i++)
         {
            all[i] = c = new ListContainer.Item(layout);
            c.items = new String[]{Convert.zeroPad(i+1,5)," BARITONOS LANCHONETE","Rio de Janeiro/Leme","75000,00","Perú"};;
            c.leftControl = null;  
         }
         int ini2 = Vm.getTimeStamp();
         lc.addContainers(all);
         int ini3 = Vm.getTimeStamp();
         gcTime = Settings.gcTime - gcTime;
         gcCount = Settings.gcCount - gcCount;
         setInfo("C="+(ini2-ini)+", A="+(ini3-ini2)+", T="+(ini3-ini)+", gc: "+gcTime+"/"+gcCount+"x"); 
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ListContainerEvent.ITEM_SELECTED_EVENT:
            setInfo("Item selected "+e.target);
            break;
         case ListContainerEvent.LEFT_IMAGE_CLICKED_EVENT:
            setInfo("Left image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
            setInfo("Right image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
      }
   }
}