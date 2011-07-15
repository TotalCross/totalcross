/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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
import totalcross.ui.image.*;

/** This class loads images depending on the user interface selected.
 * Currently there's only Android images.
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
   // Background and selection images
   public static TristateImage checkSel;
   public static TristateImage checkBkg;
   public static TristateImage radioSel;
   public static TristateImage radioBkg;
   // other
   public static Image warning;
   
   private static void loadImages(String folder) throws ImageException, IOException
   {
      warning  = new Image(folder+"warning.png");
      button   = new Image(folder+"button.png");
      edit     = new Image(folder+"edit.png");
      combobox = new Image(folder+"combobox.png");
      listbox  = new Image(folder+"listbox.png");
      multiedit= new Image(folder+"multiedit.png");
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
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t.getClass().getName()+" "+t.toString());
      }
   }
}
