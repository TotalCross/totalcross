package tc.samples.ui.androidui;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class BaseContainer extends Container
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);
   protected Bar headerBar,footerBar;
   protected String helpMessage;
   private static Vector containerStack = new Vector(5);
   private static Image exitImg,backImg,infoImg;
   private String defaultTitle = "Android User Interface";
   protected int gap;

   public BaseContainer()
   {
      transitionEffect = TRANSITION_OPEN;
   }
   
   public void initUI()
   {
      try
      {
         gap = fmH/2;
         boolean isMainMenu = containerStack.size() == 1;
         
         if (backImg == null)
         {
            backImg = new Image("images/back.png");
            exitImg = new Image("images/exit.png");
            infoImg = new Image("images/info.png");
         }
         int c1 = 0x0A246A;
         Font f = font.adjustedBy(2,true);
         headerBar = new Bar(defaultTitle);
         headerBar.setFont(f);
         headerBar.setBackForeColors(c1,Color.WHITE);
         headerBar.addButton(infoImg);
         headerBar.addButton(isMainMenu ? exitImg : backImg);
         add(headerBar, LEFT,0,FILL,PREFERRED);
         
         footerBar = new Bar("");
         footerBar.setFont(f);
         footerBar.titleAlign = CENTER;
         footerBar.backgroundStyle = BACKGROUND_SOLID;
         footerBar.setBackForeColors(c1,Color.WHITE);
         setInsets(0,0,headerBar.getHeight(),footerBar.getPreferredHeight());
         add(footerBar, LEFT,BOTTOM+insets.bottom,FILL,PREFERRED);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void setTitle(String s)
   {
      headerBar.setTitle(s);
   }
   
   public String getTitle()
   {
      return headerBar.getTitle();
   }
   
   public void setInfo(String s)
   {
      footerBar.setTitle(s);
   }
   
   public void show()
   {
      containerStack.push(this); // push ourself
      Window.getTopMost().swap(this);
      
      String name = getClass().getName();
      setTitle(name.endsWith("Samples") ? name.substring(name.lastIndexOf('.')+1,name.length()-7)+" samples" : defaultTitle);
   }
   
   public void back()
   {
      try
      {
         containerStack.pop(); // pop ourself
         Window.getTopMost().swap((Container)containerStack.peek());
      }
      catch (ElementNotFoundException enfe)
      {
         MainWindow.exit(0); // we're the last screen, so just exit the application
      }
   }
   
   public void onEvent(Event e)
   {
      try
      {
         if (e.type == ControlEvent.PRESSED && e.target == headerBar)
         {
            switch (((Bar)e.target).getSelectedIndex())
            {
               case 1:
               {
                  if (helpMessage == null) return;
                  MessageBox mb = new MessageBox("Help",helpMessage,new String[]{"Close"});
                  mb.footerColor = mb.headerColor = UIColors.messageboxBack;
                  mb.setIcon(infoImg);
                  mb.popup();
                  break;
               }
               case 2:
               {
                  back();
                  break;
               }  
            }
         }
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
}