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

package totalcross.ui.dialog;

import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.ScrollBar;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.tree.Node;
import totalcross.ui.tree.PathEntry;
import totalcross.ui.tree.Tree;
import totalcross.ui.tree.TreeModel;
import totalcross.util.Vector;

/** A class that shows all folders from a startup one to allow the user select a file or a folder.
 * Here's a sample of how to use it:
 *
 * <pre>
   try
   {
      FileChooserBox w = new FileChooserBox("Select the folder",new String[]{"  This one  "," Cancel "},
         new FileChooserBox.Filter()
         {
            public boolean accept(File f) throws IOException
            {
               return f.isDir(); // will only list folders. you may filter by other file types too
            }
         });
      w.mountTree(Settings.appPath,1);
      w.popup();
      return w.getPressedButtonIndex() == 0 ? w.getAnswer() : null;
   }
   catch (IOException e)
   {
      return null;
   }
 * </pre>
 * The tree is mounted <i>on demand</i> to speedup the process.
 * <p>
 * Here's a list of customizations you can do:
 * <ul>
 * <li> You can set a path to be selected initially by setting the <code>initialPath</code> property.
 * <li> Set the defaultButton property to allow the selection of an item doing a double-click on it.
 * </ul>
 */

public class FileChooserBox extends Window {
  protected PushButtonGroup pbg;
  protected Tree tree;
  protected Node lastSelected;
  protected Filter ff;
  /** The default button captions: " Select " and " Cancel ". You can localize them if you want. */
  public static String[] defaultButtonCaptions = { " Select ", " Cancel " };
  /** The "Volume: " label that's placed before the volume combo. You can localize it if you want. */
  public static String msgVolume = "Volume: ";
  /** The " Refresh " button that's placed after the volume combo. You can localize it if you want. */
  public static String msgRefresh = " Refresh ";
  /** The title of a message box that appears if the user tries to access a volume and an error is issued by the operating system. Defaults to "Error". You can localize it if you want. */
  public static String msgInvalidVolumeTitle = "Error";
  /** The " Preview " button title. You can localize it if you want. */
  public static String previewTitle = " Preview ";
  /** The body of a message box that appears if the user tries to access a volume and an error is issued by the operating system. Defaults to "Unable to read the contents of the selected volume. Make sure the volume is mounted and you have enough privileges to query its contents.". You can localize it if you want. */
  public static String msgInvalidVolumeMessage = "Unable to read the contents of the selected volume. Make sure the volume is mounted and you have enough privileges to query its contents.";
  protected String[] buttonCaptions;
  protected TreeModel tmodel;
  protected int selectedIndex;
  protected ComboBox cbRoot;
  protected Button btRefresh;
  protected ImageControl preview;
  private int previouslySelectedRootIndex = -1;
  private Button btnPreview;
  private Container tap;
  private static final boolean isAndroid = Settings.platform.equals(Settings.ANDROID);

  /** On Win32, we show by default the root drive and expand to the default path.
   * Set this to false to show the initial path only.
   * @since TotalCross 3.2
   */
  public boolean showInitialPathOnly = !Settings.onJavaSE && !Settings.platform.equals(Settings.WIN32);

  /* return the number of files found in the current directory
   * @since TotalCross 1.53 
   */
  public int fileCount;

  /** Set to true to allow multiple selections using a Check drawn before the nodes.
   * @since TotalCross 1.15 
   */
  public boolean multipleSelection; // guich@tc115_4

  /** Selects a file that is in this tree.
   * @since TotalCross 1.25
   */
  public String initialPath; // guich@tc125_21

  /** The button index that will be choosen if the user makes a double-click in an item.
   * Usually you set this to the index of the "Ok" button. 
   */
  public int defaultButton = -1; // guich@tc125_23

  /** The preview height in percentage of the total height. Defaults to 30. */
  public static final int PREVIEW_HEIGHT = 30;

  /** Set to true to use a preview window to show photo thumbnails */
  public boolean showPreview;

  /** Set to true to sort the list showing the newest files first, instead of alphabetical order */
  public boolean newestFirst;

  class LoadOnDemandTree extends Tree {
    public LoadOnDemandTree() {
      super(tmodel);
    }

    /** Checks if a node was already loaded and load it otherwise */
    @Override
    public boolean expand(Node node) {
      if (node.size() == 1 && node.getFirstChild().userObject instanceof File) {
        try {
          // replace the string by the files
          File f = (File) node.getFirstChild().userObject;
          node.removeAllChildren();
          mountTree(node, f);
        } catch (Exception e) {
        }
      }
      return super.expand(node);
    }
  }

  /** Interface used if you want to filter the files that will be added to the tree. */
  public static interface Filter {
    /** Must return true if the file is to be added to the tree. */
    public boolean accept(File f) throws IOException;
  }

  /** Constructs a file chooser with the given parameters.
   * @param caption The caption to be displayed in the title
   * @param buttonCaptions The button captions that will be used in the PushButtonGroup
   * @param ff The Filter. Pass null to accept all files.
   */
  public FileChooserBox(String caption, String[] buttonCaptions, Filter ff) {
    super(caption, RECT_BORDER);
    fadeOtherWindows = Settings.fadeOtherWindows;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    this.ff = ff;
    this.buttonCaptions = buttonCaptions;
    tmodel = new TreeModel();
    if (Settings.isWindowsCE() || isAndroid || Settings.platform.equals(Settings.WIN32)
        || Settings.platform.equals(Settings.JAVA)) {
      cbRoot = new ComboBox(listRoots());
    }
  }

  private static String[] listRoots() {
    if (!isAndroid) {
      return File.listRoots();
    }
    Vector v = new Vector(10);
    v.addElement("device/");
    try {
      for (int i = 0; i < 9; i++) {
        if (File.isCardInserted(i)) {
          v.addElement("/sdcard" + i);
        }
      }
    } catch (Exception e) {
    }
    return (String[]) v.toObjectArray();
  }

  /** Constructs a file chooser with "Select a file" as the window title, and "Select" and "Cancel" buttons.
   * @param ff The Filter. Pass null to accept all files.
   */
  public FileChooserBox(Filter ff) {
    this("Select a file", defaultButtonCaptions, ff);
  }

  @Override
  protected void onPopup() // guich@tc100b5_28
  {
    if (children != null) {
      if (cbRoot != null) // guich@tc126_10
      {
        cbRoot.removeAll();
        cbRoot.add(listRoots());
        selectedIndex = previouslySelectedRootIndex = -1;
      }
      return;
    }
    setRect(LEFT + 5, TOP + 5, FILL - 5, FILL - 5);
    setBackForeColors(UIColors.fileChooserBack, UIColors.fileChooserFore);

    if (cbRoot != null) // guich@tc126_10
    {
      Label l;
      add(l = new Label(msgVolume), LEFT + 2, TOP + 2);
      add(btRefresh = new Button(msgRefresh), RIGHT - 2, TOP + 2);
      add(cbRoot, AFTER + 2, SAME, FIT - 2, btRefresh.getHeight(), l);
    }

    pbg = new PushButtonGroup(buttonCaptions, 8, 1);
    pbg.setFont(font);
    add(pbg, RIGHT - 2, BOTTOM - 2, PREFERRED + 4, PREFERRED + 4);
    tree = new LoadOnDemandTree();
    tree.multipleSelection = multipleSelection;
    tree.setFont(font);
    add(tap = new Container(), LEFT + 2, btRefresh == null ? TOP + 2 : AFTER + 2, FILL - 2, FIT - 5, btRefresh);
    if (showPreview) {
      add(btnPreview = new Button(previewTitle), LEFT + 2, BOTTOM - 2);
      tap.add(preview = new ImageControl(), LEFT, BOTTOM, FILL, PARENTSIZE + PREVIEW_HEIGHT);
      preview.setBackColor(Color.getCursorColor(backColor));
      preview.setEventsEnabled(true);
      preview.centerImage = preview.scaleToFit = true;
    }
    tap.add(tree, LEFT, TOP, FILL, showPreview ? PARENTSIZE + 69 : FILL, btRefresh);
    //tree.dontShowFileAndFolderIcons();
    int c = getBackColor();
    if (cbRoot != null) {
      cbRoot.setBackColor(Color.brighter(c));
    }
    pbg.setBackColor(c);
    tree.setBackColor(c);
    tree.setCursorColor(c);
    if (initialPath != null) {
      reload(initialPath);
    }
    tree.requestFocus();
  }

  private void reload(String initialPath) {
    try {
      if (showInitialPathOnly) {
        mountTree(initialPath);
      } else {
        int dp = initialPath.indexOf(':');
        boolean cut = initialPath.length() > 3 && dp != -1; // dont cut if is "c:\"
        String ini = cut ? initialPath.substring(0, dp + 2) : initialPath;
        mountTree(ini);
        if (cut) {
          tree.expandTo(initialPath.substring(dp + 2));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** @deprecated */
  @Deprecated
  public void mountTree(String filePath, int volume) throws IOException {
    mountTree(filePath);
  }

  /** Call this method to mount the tree, starting from the given path and volume.
   * @param filePath The root from where the tree will be mounted.
   */
  public void mountTree(String filePath) throws IOException {
    Node root;
    filePath = filePath.replace('\\', '/');
    File f = new File(filePath, File.DONT_OPEN);
    if (!f.isDir()) {
      throw new IOException(filePath + " is not a valid path");
    }
    root = new Node(new PathEntry(filePath, true));
    mountTree(root, f);
    tmodel.setRoot(root);
    if (cbRoot != null) // guich@tc126_10: select current drive on combobox
    {
      previouslySelectedRootIndex = cbRoot.getSelectedIndex();
      Node p = tmodel.getRoot();
      if (p != null) {
        String s = p.toString();
        int pp = s.length() > 1 ? s.indexOf('/', 1) : -1;
        if (pp != -1) {
          s = s.substring(0, pp);
        }
        cbRoot.setSelectedItemStartingWith(s, true, false);
      }
    }
  }

  private static void qsort(String[] items, int first, int last) // guich@tc126_9: quick sort method
  {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    String mid = getSortName(items[(first + last) >> 1]);
    while (true) {
      while (high >= low && mid.compareTo(getSortName(items[low])) > 0) {
        low++;
      }
      while (high >= low && mid.compareTo(getSortName(items[high])) < 0) {
        high--;
      }
      if (low <= high) {
        String temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsort(items, first, high);
    }
    if (low < last) {
      qsort(items, low, last);
    }
  }

  private static void qsort(String thisDir, String[] items, int first, int last) // guich@tc310: newest first - do not handle folders
  {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    String mid = getSortName(thisDir, items[(first + last) >> 1]);
    while (true) {
      while (high >= low && mid.compareTo(getSortName(thisDir, items[low])) < 0) {
        low++;
      }
      while (high >= low && mid.compareTo(getSortName(thisDir, items[high])) > 0) {
        high--;
      }
      if (low <= high) {
        String temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsort(thisDir, items, first, high);
    }
    if (low < last) {
      qsort(thisDir, items, low, last);
    }
  }

  private static String getSortName(String thisDir, String s) {
    String prefix = "";
    String theFile = Convert.appendPath(thisDir, s);
    File f = null;
    try {
      f = new File(theFile, File.READ_ONLY);
      prefix = String.valueOf(f.getTime(File.TIME_MODIFIED).getTimeLong());
    } catch (FileNotFoundException fnfe) {
      // ignore. usually trying to access hidden or system files, like swapfile.sys and pagefile.sys
    } catch (Exception e) {
      Vm.debug("File: " + theFile);
      if (!String.valueOf(e.getMessage()).contains("Error Code: 32")) {
        e.printStackTrace();
      }
    }
    if (f != null) {
      try {
        f.close();
      } catch (Throwable t) {
      }
    }

    s = s.toLowerCase(); // case insensitive
    return prefix.concat(s); // put folders first
  }

  private static String getSortName(String s) // guich@tc126_9: give priority to folders and case insensitive sort
  {
    s = s.toLowerCase(); // case insensitive
    if (s.endsWith("/")) {
      s = "\1".concat(s);
    }
    return s;
  }

  protected void mountTree(Node root, File f) throws IOException {
    String files[], theFile = root.getNodeName();
    String thisDir = f.getPath();
    Node nod;
    files = f.listFiles();
    fileCount = 0;
    if (files != null) {
      if (newestFirst) {
        // have to sort folders ascending and files descending
        qsort(files, 0, files.length - 1); // put folders first
        int i = 0;
        for (; i < files.length; i++) {
          if (!files[i].endsWith("/")) {
            break;
          }
        }
        qsort(thisDir, files, i, files.length - 1);
      } else {
        qsort(files, 0, files.length - 1); // guich@tc126_9
      }
      // add files
      for (int i = 0; i < files.length; i++) {
        theFile = Convert.appendPath(thisDir, files[i]);
        f = new File(theFile, File.DONT_OPEN); // use the same volume
        if (ff == null || ff.accept(f)) {
          boolean isDir = f.isDir();
          root.add(nod = new Node(new PathEntry(files[i], isDir)));
          nod.allowsChildren = isDir;
          if (!isDir) {
            fileCount++;
          } else {
            try {
              if (!f.isEmpty()) {
                nod.add(new Node(f)); // store the file for easy call on mountTree when expanding
              }
            } catch (IOException ioe) {
              String msg = ioe.getMessage();
              if (msg != null && msg.toLowerCase().indexOf("denied") >= 0) {
                continue;
              }
              throw ioe;
            }
          }
        }
      }
    }
  }

  private int lastPenUp;
  private StringBuffer sbp = new StringBuffer(128);

  @Override
  public void onEvent(Event e) {
    try {
      switch (e.type) {
      case PenEvent.PEN_UP:
        if (defaultButton >= 0 && !(e.target instanceof ScrollBar) && !(e.target instanceof Button)) {
          int curTime = Vm.getTimeStamp();
          if ((curTime - lastPenUp) < 1000) {
            selectedIndex = defaultButton;
            this.unpop();
          } else {
            lastPenUp = curTime;
          }
        }
        break;
      case ControlEvent.PRESSED:
        if (showPreview && e.target == btnPreview) {
          preview.setImage(null);
          boolean b = !preview.isVisible();
          preview.setVisible(b);
          tree.setRect(KEEP, KEEP, KEEP, PARENTSIZE + (b ? 100 - PREVIEW_HEIGHT : 100));
        } else if (e.target == pbg) {
          selectedIndex = pbg.getSelectedIndex();
          this.unpop();
        } else if (e.target == tree) {
          lastSelected = tree.getSelectedItem();
          if (lastSelected != null) {
            if (showPreview && preview.isVisible()) {
              try {
                if (!isImage(lastSelected.getNodeName())) {
                  preview.setImage(null);
                } else {
                  sbp.setLength(0);
                  appendPath(sbp, lastSelected);
                  byte[] bytes = new File(sbp.toString(), File.READ_ONLY).readAndClose();
                  preview.setImage(new Image(bytes));
                }
              } catch (Exception ee) {
                preview.setImage(null);
                if (Settings.onJavaSE) {
                  ee.printStackTrace();
                }
              }
            }
          }
        } else if (cbRoot != null) {
          try {
            if (e.target == cbRoot) {
              int selectedIndex = cbRoot.getSelectedIndex();
              if (previouslySelectedRootIndex != selectedIndex) {
                mountTree((String) cbRoot.getSelectedItem());
                tree.setModel(tmodel);
                tree.reload();
                previouslySelectedRootIndex = selectedIndex;
              }
            } else if (e.target == btRefresh) {
              String selectedItem = (String) cbRoot.getSelectedItem();
              if (selectedItem != null || initialPath != null) {
                reload(initialPath != null ? initialPath : selectedItem);
              }
            }
          } catch (IOException e1) {
            new MessageBox(msgInvalidVolumeTitle, msgInvalidVolumeMessage).popupNonBlocking();
            cbRoot.setSelectedIndex(previouslySelectedRootIndex);
          }
        }
        break;
      }
    } catch (Exception ee) {
      if (Settings.onJavaSE) {
        ee.printStackTrace();
      }
    }
  }

  private boolean isImage(String f) {
    f = f.toLowerCase();
    return f.endsWith(".jpg") || f.endsWith(".jpeg") || f.endsWith(".png");
  }

  /** Returns the button index used to close this window. */
  public int getPressedButtonIndex() {
    return selectedIndex;
  }

  /** Returns the path choosen by the user. If using the default captions and the cancel 
   * button was pressed, returns null.
   * The filename is returned with normal (/) slashes; the path also ends with a slash.
   * If multipleSelections is on, a list of paths, separated by comma (,) is returned.
   */
  public String getAnswer() {
    if (buttonCaptions == defaultButtonCaptions && selectedIndex == 1) {
      return null;
    }
    StringBuffer sbPath = new StringBuffer(256);
    if (multipleSelection || lastSelected != null) {
      if (multipleSelection) {
        for (int i = 0, n = tree.size(); i < n; i++) {
          Node no = (Node) tree.getItemAt(i);
          if (no.isChecked) {
            appendPath(sbPath, no);
          }
        }
      }
      if (!multipleSelection || sbPath.length() == 0) {
        appendPath(sbPath, lastSelected);
      }
    }
    return sbPath.toString().replace('\\', '/');
  }

  private void appendPath(StringBuffer sbPath, Node lastSelected) {
    if (sbPath.length() > 0) {
      sbPath.append(',');
    }
    Node nods[] = lastSelected.getPath();
    for (int i = 0; i < nods.length; i++) {
      sbPath.append(nods[i].toString()).append('/');
    }
    // check if the last node is a file and remove the last / if so
    Object o = lastSelected.userObject;
    if (o instanceof PathEntry && ((PathEntry) o).type == PathEntry.FILE) {
      sbPath.setLength(sbPath.length() - 1);
    }
  }

  /** Returns the tree. */
  public Tree getTree() {
    return tree;
  }
}
