package tc.samples.ui.gadgets;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class AlignedLabelsTest extends Container
{
   public void initUI()
   {
      String[] labels =
      {
         "Name",
         "Born date",
         "Telephone",
         "Address",
         "City",
         "Country",
         "",
      };
      AlignedLabelsContainer c = new AlignedLabelsContainer(labels,4);
      c.setBorderStyle(BORDER_LOWERED);
      c.labelAlign = RIGHT;
      c.foreColors = new int[]{Color.RED,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,};
      c.setInsets(2,2,2,2);
      add(c,LEFT+2,TOP+2,FILL-2,PREFERRED+4);
      int i;
      for (i =0; i < labels.length-2; i++)
         c.add(new Edit(),LEFT+2,c.getLineY(i));
      c.add(new ComboBox(new String[]{"Brazil","USA"}),LEFT+2,c.getLineY(i));
      c.add(new Button("Insert data"),RIGHT,SAME);
      c.add(new Button("Clear data"),RIGHT,AFTER+4,SAME,PREFERRED);
   }

}
