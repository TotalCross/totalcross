package tc.samples.ui.controls;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class ProgressBarSample extends BaseContainer
{
   ProgressBar pbe,pbzv,pbv,pbzh,pbh;
   
   public void initUI()
   {
      try
      {
         super.initUI();
         setTitle("ProgressBar");
         Container sc = new Container();
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         
         pbh = new ProgressBar();
         pbh.max = 50;
         pbh.highlight = true;
         pbh.suffix = " of "+pbh.max;
         pbh.textColor = 0xAAAA;
         sc.add(pbh,LEFT,TOP,FILL,PREFERRED);
         
         // endless ProgressBar
         pbe = new ProgressBar();
         pbe.max = width/4; // max-min = width of the bar
         pbe.setEndless();
         pbe.setBackColor(Color.YELLOW);
         pbe.setForeColor(Color.ORANGE);
         pbe.prefix = "Loading, please wait...";
         sc.add(pbe,LEFT,AFTER+gap,FILL,PREFERRED);
         pbzh = new ProgressBar();
         pbzh.max = 50;
         pbzh.drawText = false;
         pbzh.setBackForeColors(Color.DARK,Color.RED);
         sc.add(pbzh,LEFT,AFTER+gap,FILL,fmH/2);
         
         final int max = Settings.onJavaSE ? 2000 : 200;
         // vertical ones
         pbv = new ProgressBar();
         pbv.vertical = true;
         pbv.max = max;
         pbv.suffix = "";
         pbv.textColor = Color.BLUE;
         pbv.setBackColor(Color.CYAN);
         pbv.setForeColor(Color.GREEN);
         sc.add(pbv,RIGHT,AFTER+gap,PREFERRED,FILL);
         
         pbzv = new ProgressBar();
         pbzv.vertical = true;
         pbzv.max = 50;
         pbzv.drawText = false;
         pbzv.setBackForeColors(Color.RED,Color.DARK);
         
         sc.add(pbzv,BEFORE-gap,SAME,fmH/2,SAME);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void onSwapFinished()
   {
      final int ini = Vm.getTimeStamp();
      repaintNow();
      // runs the bench test
      int max = pbv.max;
      for (int i = max; --i >= 0;)
      {
         int v = pbh.getValue();
         v = (v+1) % (pbh.max+1);
         Window.enableUpdateScreen = false; // since each setValue below updates the screen, we disable it to let it paint all at once at the end
         pbh.setValue(v);
         pbv.setValue(i);
         pbe.setValue(5); // increment value
         pbzh.setValue(v);
         Window.enableUpdateScreen = true;
         pbzv.setValue(v);
      }
      final String s = "Elapsed: "+(Vm.getTimeStamp()-ini)+"ms";
      MainWindow.getMainWindow().runOnMainThread(new Runnable() {public void run() {setInfo(s);}});
      Vm.debug(s);
   }
}