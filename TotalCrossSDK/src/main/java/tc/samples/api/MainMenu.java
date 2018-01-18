/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api;

import tc.samples.api.crypto.CipherSample;
import tc.samples.api.crypto.DigestSample;
import tc.samples.api.io.FileSample;
import tc.samples.api.io.PDBFileSample;
import tc.samples.api.io.device.BTTransfer;
import tc.samples.api.io.device.GPSVelocitySample;
import tc.samples.api.io.device.GpsSample;
import tc.samples.api.io.device.PortConnectorSample;
import tc.samples.api.io.device.PrinterSampleCitizen;
import tc.samples.api.io.device.PrinterSampleMPT;
import tc.samples.api.io.device.PrinterSampleZebra;
import tc.samples.api.io.device.ScannerInternal;
import tc.samples.api.io.device.ScannerZXing;
import tc.samples.api.json.JSONSample;
import tc.samples.api.lang.reflection.ReflectionSample;
import tc.samples.api.lang.thread.ThreadSample;
import tc.samples.api.map.GoogleMapsSample;
import tc.samples.api.media.MediaSample;
import tc.samples.api.net.FTPSample;
import tc.samples.api.net.ServerSocketSample;
import tc.samples.api.net.SocketSample;
import tc.samples.api.net.mail.MailSample;
import tc.samples.api.phone.PhoneDialerSample;
import tc.samples.api.phone.PhoneSmsSample;
import tc.samples.api.sql.SQLiteBenchSample;
import tc.samples.api.sys.ExternalViewersSample;
import tc.samples.api.sys.SettingsSample;
import tc.samples.api.ui.AccordionSample;
import tc.samples.api.ui.AlignedLabelsSample;
import tc.samples.api.ui.AnimationSample;
import tc.samples.api.ui.AwesomeFontSample;
import tc.samples.api.ui.ButtonMenuSample;
import tc.samples.api.ui.ButtonSample;
import tc.samples.api.ui.CameraSample;
import tc.samples.api.ui.ChartSample;
import tc.samples.api.ui.CheckRadioSample;
import tc.samples.api.ui.ComboListSample;
import tc.samples.api.ui.ControlAnimationSample;
import tc.samples.api.ui.DynScrollContainerSample;
import tc.samples.api.ui.EditSample;
import tc.samples.api.ui.FontSample;
import tc.samples.api.ui.GridSample;
import tc.samples.api.ui.HtmlContainerSample;
import tc.samples.api.ui.ImageBookSample;
import tc.samples.api.ui.ImageControlSample;
import tc.samples.api.ui.ImageModifiersSample;
import tc.samples.api.ui.ListContainerSample;
import tc.samples.api.ui.MessageBoxSample;
import tc.samples.api.ui.MultiButtonSample;
import tc.samples.api.ui.MultiEditSample;
import tc.samples.api.ui.MultitouchSample;
import tc.samples.api.ui.OtherControlsSample;
import tc.samples.api.ui.ProgressBarSample;
import tc.samples.api.ui.ProgressBoxSample;
import tc.samples.api.ui.ScrollContainerSample;
import tc.samples.api.ui.SignatureSample;
import tc.samples.api.ui.SliderSample;
import tc.samples.api.ui.SpinnerSample;
import tc.samples.api.ui.SwitchSample;
import tc.samples.api.ui.TabbedContainerSample;
import tc.samples.api.ui.TopMenuSample;
import tc.samples.api.ui.VelocimeterSample;
import tc.samples.api.ui.transluc.TranslucentUISample;
import tc.samples.api.util.PDFWriterSample;
import tc.samples.api.util.ZLibSample;
import tc.samples.api.util.ZipSample;
import tc.samples.api.xml.SoapSample;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.ButtonMenu;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;

@SuppressWarnings("rawtypes")
public class MainMenu extends BaseContainer {
  private ButtonMenu menu;

  static String DEFAULT_INFO = "Click Info for help";

  String[] uiItems = { "AccordionContainer", "AlignedLabelsContainer", "Awesome Font", "Button", "ButtonMenu", "Camera",
      "Chart", "Check/Radio", "ComboBox/ListBox", "ControlAnimation", "Dynamic ScrollContainer", "Edit", "Font sizes",
      "Grid", "HtmlContainer", "ImageControl", "Image Animation", "Image book", "Image modifiers", "ListContainer",
      "MessageBox", "MultiButton", "MultiEdit", "Multi touch", "ProgressBar", "ProgressBox", "ScrollContainer",
      "Slider", "Spinner inside loop", "Signature", "Switch On/Off", "TabbedContainer", "TopMenu", "Velocimeter",
      "Other controls", "Translucent UI", };

  String[] cryptoItems = { "crypto - Cipher", "crypto - Digest", "crypto - Signature", };

  String[] ioItems = { "File", "PDBFile", "Bluetooth Print (Citizen)", "Bluetooth Print (MPT)",
      "Bluetooth Print (Zebra)", "Bluetooth Transfer", "GPS logger", "GPS velocity", "Scanner Internal",
      "Scanner Camera", "PortConnector", };

  String[] langItems = { "Reflection", "Thread", };

  String[] jsonItems = { "JSon parser", };

  String[] mapItems = { "GoogleMaps", };

  String[] mediaItems = { "Sound", };

  String[] netItems = { "Mail", "FTP", "Server socket", "Socket Http",
      //"Socket Https",
  };

  String[] phoneItems = { "Dialer", "SMS", };

  String[] sqlItems = { "SQLite Bench", };

  String[] sysItems = { "Settings", "External Viewers", };

  String[] utilItems = { "PDF writer", "Zip", "Zlib", };

  String[] xmlItems = { "Soap", };

  String[] categs = { "ui", "crypto", "io", "json", "lang", "map", "media", "net", "phone", "sql", "sys", "util",
      "xml", };

  Class[] uiClasses = { AccordionSample.class, AlignedLabelsSample.class, AwesomeFontSample.class, ButtonSample.class,
      ButtonMenuSample.class, CameraSample.class, ChartSample.class, CheckRadioSample.class, ComboListSample.class,
      ControlAnimationSample.class, DynScrollContainerSample.class, EditSample.class, FontSample.class,
      GridSample.class, HtmlContainerSample.class, ImageControlSample.class, AnimationSample.class,
      ImageBookSample.class, ImageModifiersSample.class, ListContainerSample.class, MessageBoxSample.class,
      MultiButtonSample.class, MultiEditSample.class, MultitouchSample.class, ProgressBarSample.class,
      ProgressBoxSample.class, ScrollContainerSample.class, SliderSample.class, SpinnerSample.class,
      SignatureSample.class, SwitchSample.class, TabbedContainerSample.class, TopMenuSample.class,
      VelocimeterSample.class, OtherControlsSample.class, TranslucentUISample.class, };

  Class[] cryptoClasses = { CipherSample.class, DigestSample.class, tc.samples.api.crypto.SignatureSample.class, };

  Class[] ioClasses = { FileSample.class, PDBFileSample.class, PrinterSampleCitizen.class, PrinterSampleMPT.class,
      PrinterSampleZebra.class, BTTransfer.class, GpsSample.class, GPSVelocitySample.class, ScannerInternal.class,
      ScannerZXing.class, PortConnectorSample.class, };

  Class[] jsonClasses = { JSONSample.class, };
  Class[] langClasses = { ReflectionSample.class, ThreadSample.class, };
  Class[] mapClasses = { GoogleMapsSample.class, };
  Class[] mediaClasses = { MediaSample.class, };
  Class[] netClasses = { MailSample.class, FTPSample.class, ServerSocketSample.class, SocketSample.class,
      // SecureSocketSample.class, - buggy
  };
  Class[] phoneClasses = { PhoneDialerSample.class, PhoneSmsSample.class, };
  Class[] sqlClasses = { SQLiteBenchSample.class, };
  Class[] sysClasses = { SettingsSample.class, ExternalViewersSample.class, };
  Class[] utilClasses = { PDFWriterSample.class, ZipSample.class, ZLibSample.class, };
  Class[] xmlClasses = { SoapSample.class, };

  @Override
  protected String getHelpMessage() {
    return "This is a TotalCross " + Settings.versionStr + "." + Settings.buildNumber
        + " sample that shows most of the Application Programming Interfaces available in the SDK. You may drag the menu up and down. Device information: screen: "
        + Settings.screenWidth + "x" + Settings.screenHeight + ", device id: " + Settings.deviceId + ", font size: "
        + Font.NORMAL_SIZE + ", gc ran " + Settings.gcCount + " in " + Settings.gcTime + "ms";
  }

  ButtonMenu topmenu;

  String[][] items = { uiItems, cryptoItems, ioItems, jsonItems, langItems, mapItems, mediaItems, netItems, phoneItems,
      sqlItems, sysItems, utilItems, xmlItems };

  Class[][] classes = { uiClasses, cryptoClasses, ioClasses, jsonClasses, langClasses, mapClasses, mediaClasses,
      netClasses, phoneClasses, sqlClasses, sysClasses, utilClasses, xmlClasses };

  @Override
  public void initUI() {
    super.initUI(); // important!
    transitionEffect = TRANSITION_CLOSE;

    // single-row
    topmenu = new ButtonMenu(categs, ButtonMenu.SINGLE_ROW);
    topmenu.textPosition = BOTTOM;
    topmenu.buttonHorizGap = topmenu.buttonVertGap = 25;
    topmenu.setBackForeColors(Color.brighter(BKGCOLOR), Color.WHITE);
    topmenu.pressedColor = Color.CYAN;
    add(topmenu, LEFT, TOP, FILL, PREFERRED);

    setInfo(DEFAULT_INFO);

    String cmd = MainWindow.getCommandLine();

    if (cmd != null && cmd.startsWith("/t")) {
      try {
        showSample(uiClasses[Convert.toInt(cmd.substring(2))]);
        return;
      } catch (Exception e) {
      }
    } else if (cmd != null) {
      setInfo("cmdline: " + cmd);
    }
  }

  Class itemClasses;

  private void showMenu(String[] names) {
    setNextTransitionEffect(TRANSITION_FADE);
    if (menu != null) {
      remove(menu);
    }
    menu = new ButtonMenu(names, ButtonMenu.SINGLE_COLUMN);

    menu.pressedColor = BKGCOLOR;
    if (isTablet) {
      menu.borderGap = 100;
      menu.buttonHorizGap = menu.buttonVertGap = 200;
    } else {
      menu.buttonHorizGap = 50;
    }
    add(menu, LEFT, AFTER, FILL, FILL, topmenu);
    applyTransitionEffect();
  }

  int topmenuSel = -1;

  @Override
  public void onEvent(Event e) {
    if (e.type == ControlEvent.PRESSED) {
      try {
        if (e.target == topmenu) {
          int sel = topmenu.getSelectedIndex();
          if (sel != topmenuSel) {
            topmenuSel = sel;
            if (topmenuSel == -1) {
              remove(menu);
              setTitle(BaseContainer.defaultTitle);
            } else {
              setTitle(BaseContainer.defaultTitle + " - " + categs[topmenuSel]);
              showMenu(items[topmenuSel]);
            }
          }
        } else if (e.target == menu) {
          Class[] itemClasses = classes[topmenuSel];
          int idx = menu.getSelectedIndex();
          if (0 <= idx && idx < itemClasses.length) {
            showSample(itemClasses[idx]);
          }
        }
      } catch (Exception ee) {
        MessageBox.showException(ee, true);
      }
    }
  }

  private void showSample(Class c) throws Exception {
    BaseContainer bc = (BaseContainer) c.newInstance();
    bc.info = "Press Back for main menu";
    bc.show();
    bc.setInfo(bc.info);
  }

  @Override
  public void onAddAgain() {
    getParentWindow().setMenuBar(null);
  }
}
