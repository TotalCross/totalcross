package tc.samples.ui.gadgets;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class MultiEditTest extends Container
{
   public void initUI()
   {
      add(new Label("Non-editable MultiEdit:"),LEFT,TOP+2);
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
      mEdit.hasCursorWhenNotEditable = false;
      add(mEdit,LEFT,AFTER+2);
      mEdit.setText(s); //eventually
      mEdit.requestFocus();

      Label l;
      add(new Label("Label:"),LEFT,AFTER+4);
      add(l = new Label(Convert.insertLineBreak(Settings.screenWidth-20, fm, s)));
      l.align = FILL;
      l.setRect(LEFT,AFTER+2,FILL-20,FILL-2);
      l.backgroundType = Label.VERTICAL_GRADIENT_BACKGROUND;
      l.firstGradientColor = Color.YELLOW;
      l.secondGradientColor = Color.RED;
   }
}
