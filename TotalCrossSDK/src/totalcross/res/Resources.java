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
   public static Image tab,tab2;
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
   // SWITCH
   public static Image switchBrdAnd,switchBack,switchBtnAnd,switchBtnIos,switchBrdIos;
   
   public static String chimeMP3 = "device/chime.mp3";
   
   private static void loadImages(String folder) throws ImageException, IOException
   {
      if (progressHandle == null) try {progressHandle = new Image(folder+"progressHandle.png");  } catch (ImageException e) {}
      if (warning        == null) try {warning        = new Image(folder+"warning.png");         } catch (ImageException e) {}
      if (button         == null) try {button         = new Image(folder+"button.png");          } catch (ImageException e) {}
      if (edit           == null) try {edit           = new Image(folder+"edit.png");            } catch (ImageException e) {}
      if (combobox       == null) try {combobox       = new Image(folder+"combobox.png");        } catch (ImageException e) {}
      if (listbox        == null) try {listbox        = new Image(folder+"listbox.png");         } catch (ImageException e) {}
      if (multiedit      == null) try {multiedit      = new Image(folder+"multiedit.png");       } catch (ImageException e) {}
      if (progressbarv   == null) try {progressbarv   = new Image(folder+"progressbarV.png");    } catch (ImageException e) {}
      if (scrollposh     == null) try {scrollposh     = new Image(folder+"scrollposH.png");      } catch (ImageException e) {}
      if (scrollposv     == null) try {scrollposv     = new Image(folder+"scrollposV.png");      } catch (ImageException e) {}
      if (tab            == null) try {tab            = new Image(folder+"tab.png");             } catch (ImageException e) {}
      if (tab2           == null) try {tab2           = new Image(folder+"tab2.png");            } catch (ImageException e) {}
      if (grid           == null) try {grid           = new Image(folder+"grid.png");            } catch (ImageException e) {}
      if (exit           == null) try {exit           = new Image(folder+"exit.png");            } catch (ImageException e) {}
      if (back           == null) try {back           = new Image(folder+"back.png");            } catch (ImageException e) {}
      if (menu           == null) try {menu           = new Image(folder+"menu.png");            } catch (ImageException e) {}
      if (comboArrow     == null) try {comboArrow     = new Image(folder+"comboArrow.png");      } catch (ImageException e) {}
      if (comboArrow2    == null) try {comboArrow2    = new Image(folder+"comboArrow2.png");     } catch (ImageException e) {}
      if (switchBack     == null) try {switchBack     = new Image(folder+"switch_bck.png");      } catch (ImageException e) {}
      if (switchBtnAnd   == null) try {switchBtnAnd   = new Image(folder+"switch_btn_and.png");  } catch (ImageException e) {}
      if (switchBtnIos   == null) try {switchBtnIos   = new Image(folder+"switch_btn_ios.png");  } catch (ImageException e) {}
      if (switchBrdAnd   == null) try {switchBrdAnd   = new Image(folder+"switch_brd_and.png");  } catch (ImageException e) {}
      if (switchBrdIos   == null) try {switchBrdIos   = new Image(folder+"switch_brd_ios.png");  } catch (ImageException e) {}
      if (checkBkg       == null) try {checkBkg       = new TristateImage(folder+"checkBkg.png");} catch (ImageException e) {}
      if (checkSel       == null) try {checkSel       = new TristateImage(folder+"checkSel.png");} catch (ImageException e) {}
      if (radioBkg       == null) try {radioBkg       = new TristateImage(folder+"radioBkg.png");} catch (ImageException e) {}
      if (radioSel       == null) try {radioSel       = new TristateImage(folder+"radioSel.png");} catch (ImageException e) {}
   }
   
   public static void uiStyleChanged()
   {
      try
      {
         switch (Settings.uiStyle)
         {
            case Settings.Holo:
               loadImages("totalcross/res/holo/");
               break;
         }
         loadImages("totalcross/res/android/"); // always load android UI
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
      if (Settings.uiStyle >= Settings.Android)
      {
         NinePatch.getInstance().flush();
         checkSel.flush();
         checkBkg.flush();
         radioSel.flush();
         radioBkg.flush();
      }
   }
}
