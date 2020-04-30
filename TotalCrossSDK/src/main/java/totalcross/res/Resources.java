// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.res;

import java.io.ByteArrayInputStream;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.NinePatch;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.IOUtils;

/** This class loads images depending on the user interface selected.
 * Currently there's only Android images.
 * 
 * Android uses lots of images to render the user interface. If you get 
 * an OutOfMemoryError, try calling the flush method. Note that doing this often may
 * slowdown the whole program.
 * 
 * @since TotalCross 1.3
 */
public class Resources {
  // NinePatches
  public static Image button;
  public static Image edit;
  public static Image outlinededit;
  public static Image combobox;
  public static Image floatingFrg;
  public static Image multibutton;
  public static Image listbox;
  public static Image multiedit;
  public static Image progressbarv;
  public static Image scrollposh, scrollposv;
  public static Image tab, tab2;
  public static Image grid;
  // Background and selection images
  public static TristateImage checkSel;
  public static TristateImage checkBkg;
  public static TristateImage floatingBkg;
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
  public static Image switchBrdAnd, switchBack, switchBtnAnd, switchBtnIos, switchBrdIos, switchBtn, switchBrd; // material last 2

  public static String chimeMP3 = "device/chime.mp3";

  private static boolean uiMat;

  private static void loadImages(String folder) throws ImageException, IOException {
    boolean b = Settings.showDesktopMessages;
    Settings.showDesktopMessages = false;
    if (progressHandle == null) {
      try {
        progressHandle = new Image(folder + "progressHandle.png");
      } catch (ImageException e) {
      }
    }
    if (warning == null) {
      try {
        warning = new Image(folder + "warning.png");
      } catch (ImageException e) {
      }
    }
    if (button == null) {
      try {
        button = new Image(folder + "button.png");
      } catch (ImageException e) {
      }
    }
    if (edit == null) {
      try {
        edit = new Image(folder + "edit.png");
      } catch (ImageException e) {
      }
    }
    if (combobox == null) {
      try {
        combobox = new Image(folder + "combobox.png");
      } catch (ImageException e) {
      }
    }
    if (floatingFrg == null) {
        try {
        	floatingFrg = new Image(folder + "floatingFrg.png");
        } catch (ImageException e) {
        }
      }
    if(multibutton == null) {
  	  try {
  		  multibutton = new Image(folder + "multibutton.png");
  	  } catch (ImageException e) {
      }
      }
    if (listbox == null) {
      try {
        listbox = new Image(folder + "listbox.png");
      } catch (ImageException e) {
      }
    }
    if (multiedit == null) {
      try {
        multiedit = new Image(folder + "multiedit.png");
      } catch (ImageException e) {
      }
    }
    if (outlinededit == null) {
    	try {
    		outlinededit = new Image(folder + "outlined_edit.png");
    	} catch (ImageException e) {
    	}
    }
    if (progressbarv == null) {
      try {
        progressbarv = new Image(folder + "progressbarV.png");
      } catch (ImageException e) {
      }
    }
    if (scrollposh == null) {
      try {
        scrollposh = new Image(folder + "scrollposH.png");
      } catch (ImageException e) {
      }
    }
    if (scrollposv == null) {
      try {
        scrollposv = new Image(folder + "scrollposV.png");
      } catch (ImageException e) {
      }
    }
    if (tab == null) {
      try {
        tab = new Image(folder + "tab.png");
      } catch (ImageException e) {
      }
    }
    if (tab2 == null) {
      try {
        tab2 = new Image(folder + "tab2.png");
      } catch (ImageException e) {
      }
    }
    if (grid == null) {
      try {
        grid = new Image(folder + "grid.png");
      } catch (ImageException e) {
      }
    }
    if (exit == null) {
      try {
        exit = new Image(folder + "exit.png");
      } catch (ImageException e) {
      }
    }
    if (back == null) {
      try {
        back = new Image(folder + "back.png");
      } catch (ImageException e) {
      }
    }
    if (menu == null) {
      try {
        menu = new Image(folder + "menu.png");
      } catch (ImageException e) {
      }
    }
    if (comboArrow == null) {
      try {
        comboArrow = new Image(folder + "comboArrow.png");
      } catch (ImageException e) {
      }
    }
    if (comboArrow2 == null) {
      try {
        comboArrow2 = new Image(folder + "comboArrow2.png");
      } catch (ImageException e) {
      }
    }
    if (!uiMat && switchBack == null) {
      try {
        switchBack = new Image(folder + "switch_bck.png");
      } catch (ImageException e) {
      }
    }
    if (!uiMat && switchBtnAnd == null) {
      try {
        switchBtnAnd = new Image(folder + "switch_btn_and.png");
      } catch (ImageException e) {
      }
    }
    if (!uiMat && switchBtnIos == null) {
      try {
        switchBtnIos = new Image(folder + "switch_btn_ios.png");
      } catch (ImageException e) {
      }
    }
    if (!uiMat && switchBrdAnd == null) {
      try {
        switchBrdAnd = new Image(folder + "switch_brd_and.png");
      } catch (ImageException e) {
      }
    }
    if (!uiMat && switchBrdIos == null) {
      try {
        switchBrdIos = new Image(folder + "switch_brd_ios.png");
      } catch (ImageException e) {
      }
    }
    if (uiMat && switchBtn == null) {
      try {
        switchBtn = new Image(folder + "switch_btn.png");
      } catch (ImageException e) {
      }
    }
    if (uiMat && switchBrd == null) {
      try {
        switchBrd = new Image(folder + "switch_brd.png");
      } catch (ImageException e) {
      }
    }
    if (checkBkg == null) {
      try {
        checkBkg = new TristateImage(folder + "checkBkg.png");
      } catch (ImageException e) {
      }
    }
    if (checkSel == null) {
      try {
        checkSel = new TristateImage(folder + "checkSel.png");
      } catch (ImageException e) {
      }
    }
    if(floatingBkg == null) {
    	try {
    		floatingBkg = new TristateImage(folder + "floatingBkg.png");
    	} catch (ImageException e) {
    		
    	}
    }
    if (radioBkg == null) {
      try {
        radioBkg = new TristateImage(folder + "radioBkg.png");
      } catch (ImageException e) {
      }
    }
    if (radioSel == null) {
      try {
        radioSel = new TristateImage(folder + "radioSel.png");
      } catch (ImageException e) {
      }
    }
    Settings.showDesktopMessages = b;
  }

  public static void uiStyleChanged() {
    try {
      switch (Settings.uiStyle) {
      case Settings.Holo:
        loadImages("totalcross/res/holo/");
        break;
      case Settings.Material:
        uiMat = true;
        loadImages("totalcross/res/mat/");
        break;
      }
      loadImages("totalcross/res/android/"); // always load android UI
      try (File test = new File("device/chime.mp3")) {
        if (!test.exists()) {
          try (File writeTo = new File("device/chime.mp3", File.CREATE_EMPTY)) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
                Vm.getFile("totalcross/res/mp3/chime.mp3"))) {
              IOUtils.copy(inputStream, writeTo.asOutputStream(), 4096);
            }
          }
        }
      } catch (Exception e) {
        if (!Settings.onJavaSE) {
          e.printStackTrace();
        }
      }
    } catch (Throwable t) {
      throw new RuntimeException(t.getClass().getName() + " " + t.toString());
    }
  }

  /** Flush all resources held in the hashtables of the classes used by the Android user interface style.
   * Does nothing when the style is another one. 
   */
  public static void flush() {
    if (Settings.uiStyle >= Settings.Android) {
      NinePatch.getInstance().flush();
      checkSel.flush();
      checkBkg.flush();
      radioSel.flush();
      radioBkg.flush();
    }
  }
}
