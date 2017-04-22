package tc.samples.api.ui.transluc;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

import tc.samples.api.*;

public class MenuScreen extends TranslucentBaseContainer
{
   private static MenuScreen inst;
   public static MenuScreen getInstance() {return inst;}

   BaseContainer base;
   public MenuScreen(BaseContainer base)
   {
      this.base = base;
      inst = this;
   }

   public void initUI()
   {
      super.initUI();
      try
      {
         setBackgroundImage(0);
         
         Button t = createTransButton("Edit","ui/images/bt_edit.png"); 
         add(t, PARENTSIZE+25,PARENTSIZE+25,PARENTSIZE+40,PARENTSIZE+20);
         t.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {EditScreen.getInstance().show();}});
         
         t = createTransButton("Search", "ui/images/bt_search.png"); 
         add(t, PARENTSIZE+75,PARENTSIZE+25,PARENTSIZE+40,PARENTSIZE+20);
         t.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {SearchScreen.getInstance().show();}});
         
         t = createTransButton("List","ui/images/bt_list.png"); 
         add(t, PARENTSIZE+25,PARENTSIZE+50,PARENTSIZE+40,PARENTSIZE+20);
         t.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {ListScreen.getInstance().show();}});
         
         t = createTransButton("View","ui/images/bt_view.png"); 
         add(t, PARENTSIZE+75,PARENTSIZE+50,PARENTSIZE+40,PARENTSIZE+20);        
         t.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {ViewScreen.getInstance().show();}});

         Button t1 = createTransBarButton("ui/images/bt_info.png");
         t1.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {MessageBox mb = new MessageBox(null,"Sample with translucent controls.",null); mb.popupNonBlocking(); Vm.sleep(3000); mb.unpop();}});
         Button t2 = createTransBarButton("ui/images/bt_home.png");
         t2.addPressListener(new PressListener() {public void controlPressed(ControlEvent e) {
            base.back();}});

         Button[] bar = {t1, t2};
         for (int i = 0, n = bar.length; i < n; i++)
            add(bar[i], i == 0 ? LEFT : AFTER,BOTTOM, PARENTSIZE-n,fmH*3);
         
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}
