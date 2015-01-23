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



package totalcross.ui.dialog;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.ui.tree.*;
import totalcross.util.*;

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

public class FileChooserBox extends Window
{
	protected PushButtonGroup pbg;
	protected Tree tree;
	protected Node lastSelected;
	protected Filter ff;
	/** The default button captions: " Select " and " Cancel ". You can localize them if you want. */
	public static String[] defaultButtonCaptions = {" Select "," Cancel "};
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
	protected Vector selectedNodes; // guich@tc115_4: used in multiple selections
	protected ComboBox cbRoot;
	protected Button btRefresh;
	protected ImageControl preview;
   private int previouslySelectedRootIndex = -1;
   private Button btnPreview;
   private Container tap;
   private static final boolean isAndroid = Settings.platform.equals(Settings.ANDROID);

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
   
   class LoadOnDemandTree extends Tree
   {
      public LoadOnDemandTree()
      {
         super(tmodel);
      }
      
      /** Checks if a node was already loaded and load it otherwise */
      public boolean expand(Node node)
      {
         if (node.size() == 1 && node.getFirstChild().userObject instanceof File)
         {
            try
            {
               // replace the string by the files
               File f = (File)node.getFirstChild().userObject;
               node.removeAllChildren();
               mountTree(node, f);
            }
            catch (Exception e)
            {
            }
         }
         return super.expand(node);
      }
   }
   
   /** Interface used if you want to filter the files that will be added to the tree. */
   public static interface Filter
   {
      /** Must return true if the file is to be added to the tree. */
      public boolean accept(File f) throws IOException;
   }

   /** Constructs a file chooser with the given parameters.
    * @param caption The caption to be displayed in the title
    * @param buttonCaptions The button captions that will be used in the PushButtonGroup
    * @param ff The Filter. Pass null to accept all files.
    */
	public FileChooserBox(String caption, String[] buttonCaptions, Filter ff)
	{
      super(caption, RECT_BORDER);
      transitionEffect = Settings.enableWindowTransitionEffects ? TRANSITION_OPEN : TRANSITION_NONE;
      fadeOtherWindows = Settings.fadeOtherWindows;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      this.ff = ff;
      this.buttonCaptions = buttonCaptions;
      tmodel = new TreeModel();
      if (Settings.isWindowsDevice() || isAndroid || Settings.platform.equals(Settings.WIN32) || Settings.platform.equals(Settings.JAVA)) // guich@tc126_10
         cbRoot = new ComboBox(listRoots());
   }
	
	private static String[] listRoots()
	{
	   if (!isAndroid)
	      return File.listRoots();
	   Vector v = new Vector(10);
	   v.addElement("device/");
	   try
	   {
         String[] ll = new File("/mnt").listFiles();
         for (int i = 0; i < ll.length; i++)
         {
            String dir = "/mnt/"+ll[i];
            try
            {
               if (new File(dir).listFiles() != null)
                  v.addElement(dir);
            } catch (Exception e) {}
         }
	   }
	   catch (Exception e)
	   {
	   }
	   return (String[]) v.toObjectArray();
	}

   /** Constructs a file chooser with "Select a file" as the window title, and "Select" and "Cancel" buttons.
    * @param ff The Filter. Pass null to accept all files.
    */
   public FileChooserBox(Filter ff)
   {
      this("Select a file", defaultButtonCaptions, ff);
   }

   protected void onPopup() // guich@tc100b5_28
   {
      if (children != null) 
      {
         if (cbRoot != null) // guich@tc126_10
         {
            cbRoot.removeAll();
            cbRoot.add(listRoots());
            selectedIndex = previouslySelectedRootIndex = -1;
         }
         return;
      }
      setRect(LEFT+5,TOP+5,FILL-5,FILL-5);
      setBackForeColors(UIColors.fileChooserBack, UIColors.fileChooserFore);

      if (cbRoot != null) // guich@tc126_10
      {
         Label l;
         add(l = new Label(msgVolume), LEFT + 2, TOP + 2);
         add(btRefresh = new Button(msgRefresh), RIGHT - 2, TOP + 2);
         add(cbRoot, AFTER + 2, SAME, FIT-2, btRefresh.getHeight(), l);
      }

      pbg = new PushButtonGroup(buttonCaptions, 8, 1);
      pbg.setFont(font);
      add(pbg, RIGHT-2, BOTTOM - 2, PREFERRED+4, PREFERRED+4);
      tree = new LoadOnDemandTree();
      tree.multipleSelection = multipleSelection;
      if (multipleSelection)
         selectedNodes = new Vector();
      tree.setFont(font);
      add(tap = new Container(), LEFT+2,btRefresh == null ? TOP+2 : AFTER+2, FILL-2, FIT - 5, btRefresh);
      if (showPreview)
      {
         add(btnPreview = new Button(previewTitle),LEFT+2,BOTTOM-2);
         tap.add(preview = new ImageControl(), LEFT,BOTTOM,FILL,PARENTSIZE+PREVIEW_HEIGHT);
         preview.setBackColor(Color.getCursorColor(backColor));
         preview.setEventsEnabled(true);
         preview.centerImage = preview.scaleToFit = true;
      }
      tap.add(tree, LEFT,TOP, FILL, showPreview ? PARENTSIZE+69 : FILL, btRefresh);
      //tree.dontShowFileAndFolderIcons();
      int c = getBackColor();
      if (cbRoot != null) cbRoot.setBackColor(Color.brighter(c));
      pbg.setBackColor(c);
      tree.setBackColor(c);
      tree.setCursorColor(c);
      if (initialPath != null)
         try
         {
            mountTree(initialPath);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      tree.requestFocus();
	}
   
   /** @deprecated */
   public void mountTree(String filePath, int volume) throws IOException
   {
      mountTree(filePath);
   }
   /** Call this method to mount the tree, starting from the given path and volume.
    * @param filePath The root from where the tree will be mounted.
    */
   public void mountTree(String filePath) throws IOException
   {
      Node root;
      filePath = filePath.replace('\\','/');
      File f = new File(filePath, File.DONT_OPEN);
      if (!f.isDir())
         throw new IOException(filePath+" is not a valid path");
      root = new Node(new PathEntry(filePath, true));
      mountTree(root, f);
      tmodel.setRoot(root);
      if (cbRoot != null) // guich@tc126_10: select current drive on combobox
      {
         int firstSlash = filePath.indexOf('/');
         if (firstSlash != -1)
         {
            if (isAndroid && filePath.startsWith("/mnt"))
            {
               int otherSlash = filePath.indexOf(firstSlash+1);
               if (otherSlash != -1)
                  firstSlash = otherSlash;
            }
            cbRoot.setSelectedItemStartingWith(filePath.substring(0,firstSlash),true);
            previouslySelectedRootIndex = cbRoot.getSelectedIndex();
         }
      }
   }

   private static void qsort(String []items, int first, int last) // guich@tc126_9: quick sort method
   {
      if (first >= last)
         return;
      int low = first;
      int high = last;

      String mid = getSortName(items[(first+last) >> 1]);
      while (true)
      {
         while (high >= low && mid.compareTo(getSortName(items[low]))  > 0)
            low++;
         while (high >= low && mid.compareTo(getSortName(items[high])) < 0)
            high--;
         if (low <= high)
         {
            String temp = items[low];
            items[low++] = items[high];
            items[high--] = temp;
         }
         else break;
      }

      if (first < high)
         qsort(items,first,high);
      if (low < last)
         qsort(items,low,last);
   }
   
   private static String getSortName(String s) // guich@tc126_9: give priority to folders and case insensitive sort
   {
      s = s.toLowerCase(); // case insensitive
      if (s.endsWith("/")) // put folders first
         s = "\1".concat(s);
      return s;
   }

   protected void mountTree(Node root, File f) throws IOException
   {
      String files[], theFile = root.getNodeName();
      String thisDir = f.getPath();
      Node nod;
      files = f.listFiles();
      fileCount = 0;
      if (files != null)
      {
         qsort(files, 0, files.length-1); // guich@tc126_9
         // add files
         for (int i = 0; i < files.length; i++)
         {
            theFile = Convert.appendPath(thisDir,files[i]);
            f = new File(theFile, File.DONT_OPEN); // use the same volume
            if (ff == null || ff.accept(f))
            {
               boolean isDir = f.isDir();
               root.add(nod = new Node(new PathEntry(files[i], isDir)));
               nod.allowsChildren = isDir;
               if (!isDir)
                  fileCount++;
               else
               {
                  try
                  {
                     if (!f.isEmpty())
                        nod.add(new Node(f)); // store the file for easy call on mountTree when expanding
                  }
                  catch (IOException ioe)
                  {
                     String msg = ioe.getMessage();
                     if (msg != null && msg.toLowerCase().indexOf("denied") >= 0) // guich@tc123_51: permission denied? skip this folder
                        continue;
                     throw ioe;
                  }
               }
            }
         }
      }
   }

   private int lastPenUp;
   private StringBuffer sbp = new StringBuffer(128);
	public void onEvent(Event e)
	{
	   try
	   {
   	   switch (e.type)
   	   {
   	      case PenEvent.PEN_UP:
         	   if (defaultButton >= 0 && !(e.target instanceof ScrollBar) && !(e.target instanceof Button))
         	   {
         	      int curTime = Vm.getTimeStamp();
         	      if ((curTime-lastPenUp) < 1000)
         	      {
         	         selectedIndex = defaultButton;
         	         this.unpop();
         	      }
         	      else lastPenUp = curTime;
         	   }
         	   break;
   	      case ControlEvent.PRESSED:
   	         if (showPreview && e.target == btnPreview)
   	         {
   	            preview.setImage(null);
   	            boolean b = !preview.isVisible();
 	               preview.setVisible(b);
   	            tree.setRect(KEEP,KEEP,KEEP,PARENTSIZE+(b ? 100-PREVIEW_HEIGHT : 100));
   	         }
   	         else
         		if (e.target == pbg)
               {
                  selectedIndex = pbg.getSelectedIndex();
         			this.unpop();
               }
               else
               if (e.target == tree)
               {
                  lastSelected = tree.getSelectedItem();
                  if (lastSelected != null)
                  {
                     if (showPreview && preview.isVisible())
                        try
                        {
                           if (!isImage(lastSelected.getNodeName()))
                              preview.setImage(null);
                           else
                           {
                              sbp.setLength(0);
                              appendPath(sbp,lastSelected);
                              byte[] bytes = new File(sbp.toString(), File.READ_ONLY).readAndClose();
                              preview.setImage(new Image(bytes));
                           }
                        }
                        catch (Exception ee)
                        {
                           preview.setImage(null);
                           if (Settings.onJavaSE) ee.printStackTrace();
                        }                        
                     if (multipleSelection)
                        if (lastSelected.isChecked)
                           selectedNodes.addElement(lastSelected);
                        else
                           selectedNodes.removeElement(lastSelected);
                  }
               }
               else
               if (cbRoot != null) // guich@tc126_10
                  try
                  {
                     if (e.target == cbRoot)
                     {
                        int selectedIndex = cbRoot.getSelectedIndex();
                        if (previouslySelectedRootIndex != selectedIndex)
                        {
                           mountTree((String) cbRoot.getSelectedItem());
                           tree.setModel(tmodel);
                           tree.reload();
                           previouslySelectedRootIndex = selectedIndex;
                        }
                     }
                     else 
                     if (e.target == btRefresh)
                     {
                        String selectedItem = (String) cbRoot.getSelectedItem();
                        if (selectedItem != null || initialPath != null)
                        {
                           mountTree(initialPath != null ? initialPath : selectedItem);
                           //tree.setModel(tmodel);
                           //tree.reload();
                        }
                     }
                  }
                  catch (IOException e1)
                  {
                     new MessageBox(msgInvalidVolumeTitle, msgInvalidVolumeMessage).popupNonBlocking();
                     cbRoot.setSelectedIndex(previouslySelectedRootIndex);
                  }
         		break;
         }
	   }
      catch (Exception ee)
      {
         if (Settings.onJavaSE) ee.printStackTrace();
      }
	}

   private boolean isImage(String f)
   {
      f = f.toLowerCase();
      return f.endsWith(".jpg") || f.endsWith(".jpeg") || f.endsWith(".png");
   }

   /** Returns the button index used to close this window. */
   public int getPressedButtonIndex()
   {
      return selectedIndex;
   }

   /** Returns the path choosen by the user. If using the default captions and the cancel 
    * button was pressed, returns null.
    * The filename is returned with normal (/) slashes; the path also ends with a slash.
    * If multipleSelections is on, a list of paths, separated by comma (,) is returned.
    */
   public String getAnswer()
   {
      if (buttonCaptions == defaultButtonCaptions && selectedIndex == 1) // canceled?
         return null;
      StringBuffer sbPath = new StringBuffer(256);
      if ((multipleSelection && selectedNodes.size() > 0) || (!multipleSelection && lastSelected != null))
      {
         if (multipleSelection)
            for (int i =0,n=selectedNodes.size(); i < n; i++)
               appendPath(sbPath, (Node)selectedNodes.items[i]);
         else
            appendPath(sbPath, lastSelected);
      }
      return sbPath.toString().replace('\\','/');
   }
   
   private void appendPath(StringBuffer sbPath, Node lastSelected)
   {
      if (sbPath.length() > 0)
         sbPath.append(',');
      Node nods[] = lastSelected.getPath();
      for (int i =0; i < nods.length; i++)
         sbPath.append(nods[i].toString()).append('/');
      // check if the last node is a file and remove the last / if so
      Object o = lastSelected.userObject;
      if (o instanceof PathEntry && ((PathEntry)o).type == PathEntry.FILE)
         sbPath.setLength(sbPath.length()-1);            
   }

   /** Returns the tree. */
   public Tree getTree()
   {
      return tree;
   }
}
