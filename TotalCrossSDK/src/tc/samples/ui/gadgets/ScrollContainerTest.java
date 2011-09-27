package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;

public class ScrollContainerTest extends Container
{
   public void initUI()
   {
      Button b;
      ScrollContainer sc1,sc2,sc3;
      // a ScrollContainer with both ScrollBars
      add(new Label("Vertical and horizontal:"),LEFT+10,TOP);
      add(sc1 = new ScrollContainer());
      sc1.setBorderStyle(BORDER_SIMPLE);
      sc1.setRect(LEFT+10,AFTER,FILL-10,SCREENSIZE+20);
      int xx = new Label("Name99").getPreferredWidth()+2; // edit's alignment
      for (int i =0; i < 50; i++)
      {
         sc1.add(new Label("Name"+i),LEFT,AFTER);
         sc1.add(new Edit("@@@@@@@@@@@@@@"),xx,SAME);
         if (i % 3 == 0) sc1.add(new Button("Go"), AFTER+2,SAME,PREFERRED,SAME);
      }

      // a ScrollContainer with vertical ScrollBar disabled
      add(sc2 = new ScrollContainer(true,false));
      sc2.setBorderStyle(BORDER_RAISED);
      int lines = Settings.screenHeight > 320 ? 4 : 3;
      sc2.setRect(LEFT+10,BOTTOM-5,FILL-10,(fmH+Edit.prefH)*lines+4);
      for (int i =0; i < lines; i++)
      {
         sc2.add(new Label("Name"+i),LEFT,AFTER);
         sc2.add(new Edit("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"),xx,SAME); // fit
         sc2.add(new Button("Go"), AFTER,SAME,PREFERRED,SAME);
      }
      Label l;
      add(l = new Label("Vertical-only:"),LEFT+10,AFTER+5,sc1);
      add(new Label("Horizontal-only:"),LEFT+10,BEFORE,sc2);

      // a ScrollContainer with horizontal ScrollBar disabled
      add(sc3 = new ScrollContainer(false,true));
      sc3.setBorderStyle(BORDER_LOWERED);
      sc3.setRect(LEFT+10,AFTER,FILL-10,FIT-5,l);
      for (int i =0; i < 50; i++)
      {
         sc3.add(new Label("Name"+i),LEFT,AFTER);
         sc3.add(b = new Button("Go"), RIGHT,SAME,PREFERRED,SAME);
         sc3.add(new Edit(""),xx,SAME,FIT-2,PREFERRED,b); // fit
      }
   }

}
