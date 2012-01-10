package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;

public class MultiEditSamples extends BaseContainer
{
   public MultiEditSamples()
   {
      helpMessage = "These are MultiEdit samples in the Android user interface style. Press back to go to the main menu.";
   }
   
   public void initUI()
   {
      try
      {
         super.initUI();
         ScrollContainer sc = new ScrollContainer(false, true);
         sc.setInsets(gap,gap,gap,gap);
         add(sc,LEFT,TOP,FILL,FILL);
         MultiEdit c;
         
         sc.add(c = new MultiEdit(5,1), LEFT,TOP,FILL,PREFERRED);
         c.setBackColor(Color.brighter(Color.BLUE));

         sc.add(c = new MultiEdit(2,0), LEFT,AFTER+gap,FILL,PREFERRED);
         c.setBackColor(Color.brighter(Color.GREEN));

         sc.add(new Label("Non-editable MultiEdit:"),LEFT,AFTER+gap);
         String s = "James Robert Baker (1946-1997) was an American author of sharply satirical, " +
               "predominantly gay-themed transgressional fiction. A native Californian, his work " +
               "is set almost entirely in Southern California. After graduating from UCLA, he began " +
               "his career as a screenwriter, but became disillusioned and started writing novels " +
               "instead. Though he garnered fame for his books Fuel-Injected Dreams and Boy Wonder," +
               "after the controversy surrounding publication of his novel, Tim And Pete, he faced " +
               "increasing difficulty having his work published. According to his life partner, this " +
               "was a contributing factor in his suicide. Baker's work has achieved cult status in the " +
               "years since his death, and two additional novels have been posthumously published. " +
               "First-edition copies of his earlier works have become collector's items. One of his " +
               "novels was filmed (though it was not a financial success) and two others have been " +
               "optioned for the movies, though they have not been produced.";
         MultiEdit mEdit;
         mEdit = new MultiEdit("",6,1);
         mEdit.drawDots = (false);
         mEdit.justify = true;
         mEdit.setEditable(false);
         MultiEdit.hasCursorWhenNotEditable = false;
         sc.add(mEdit,LEFT,AFTER+gap);
         mEdit.setText(s); //eventually
         mEdit.requestFocus();

         setInfo("Click Info button for help.");
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}