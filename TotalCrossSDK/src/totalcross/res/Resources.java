/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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

package totalcross.res;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.image.*;

/** This class loads images depending on the user interface selected.
 * Currently there's only Android images.
 * 
 * Android uses lots of images to render the user interface. If you get 
 * an OutOfMemoryError, try calling the flush method. Note that doing this often may
 * slowdown the whole program.
 * 
 * @since TotalCross 1.3
 */
public class Resources
{
   // NinePatches
   public static Image button;
   public static Image edit;
   public static Image combobox;
   public static Image listbox;
   public static Image multiedit;
   public static Image progressbarv;
   public static Image scrollposh,scrollposv;
   public static Image tab;
   public static Image grid;
   // Background and selection images
   public static TristateImage checkSel;
   public static TristateImage checkBkg;
   public static TristateImage radioSel;
   public static TristateImage radioBkg;
   // other
   public static Image warning;
   public static Image progressHandle;
   public static Image exit;
   public static Image back;
   public static Image menu;
   public static Image comboArrow;
   public static Image comboArrow2;
   
   public static String chimeMP3 = "device/chime.mp3";
   
   private static void loadImages(String folder) throws ImageException, IOException
   {
      progressHandle = new Image(folder+"progressHandle.png");
      warning  = new Image(folder+"warning.png");
      button   = new Image(folder+"button.png");
      edit     = new Image(folder+"edit.png");
      combobox = new Image(folder+"combobox.png");
      listbox  = new Image(folder+"listbox.png");
      multiedit= new Image(folder+"multiedit.png");
      progressbarv= new Image(folder+"progressbarV.png");
      scrollposh = new Image(folder+"scrollposH.png");
      scrollposv = new Image(folder+"scrollposV.png");
      tab      = new Image(folder+"tab.png");
      grid     = new Image(folder+"grid.png");
      exit     = new Image(folder+"exit.png");
      back     = new Image(folder+"back.png");
      menu     = new Image(folder+"menu.png");
      comboArrow  = new Image(folder+"comboArrow.png");
      comboArrow2 = new Image(folder+"comboArrow2.png");
      
      checkBkg = new TristateImage(folder+"checkBkg.png");
      checkSel = new TristateImage(folder+"checkSel.png");
      radioBkg = new TristateImage(folder+"radioBkg.png");
      radioSel = new TristateImage(folder+"radioSel.png");
   }
   
   public static void uiStyleChanged()
   {
      try
      {
         switch (Settings.uiStyle)
         {
            case Settings.Android:
               loadImages("totalcross/res/android/");
               break;
         }
         try {if (!new File("device/chime.mp3").exists()) new File("device/chime.mp3",File.CREATE_EMPTY).writeAndClose(Vm.getFile("totalcross/res/mp3/chime.mp3"));} catch (Exception e) {if (!Settings.onJavaSE) e.printStackTrace();}
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t.getClass().getName()+" "+t.toString());
      }
   }
   
   /** Flush all resources held in the hashtables of the classes used by the Android user interface style.
    * Does nothing when the style is another one. 
    */
   public static void flush()
   {
      if (Settings.uiStyle == Settings.Android)
      {
         NinePatch.getInstance().flush();
         checkSel.flush();
         checkBkg.flush();
         radioSel.flush();
         radioBkg.flush();
      }
   }
}
