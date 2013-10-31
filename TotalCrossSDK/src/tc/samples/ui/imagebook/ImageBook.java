package tc.samples.ui.imagebook;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ImageBook extends MainWindow
{
   GridContainer gc;
   
   static
   {
      Settings.fingerTouch = true;
   }
   
   // saves the files that contains the images
   // always store the reference to the file, NEVER the image bytes! can also be the rowid or primary key if the image is inside a database
   String[] imageNames;
   private String imageFolder;

   // parameters that you can tweak
   
   // controls the gaps within the Cell
   private static final int CELL_GAP = 0;// uncomment to change the space - Settings.uiAdjustmentsBasedOnFontHeight ? 20 : 2;
   private static final int IMAGE_GAP= CELL_GAP*2;
   
   // controls the image quality
   private static final boolean FAST = true;  // mude para false para melhorar a qualidade da imagem, true para piorar (e deixar mais lento)
   private static final int TOTAL_ITEMS = 1000;
   
   private static final int GRID_LINES = 3;
   private static final int GRID_COLS = 4;
   private static final int CACHE_PAGES = 10; // big value = more memory consumed
   private static final int LINES_PER_PAGE = 6; // auto-adjust the font size; if set to 0, the size is unchanged
   
   // thread that loads and unloads the images dynamically
   ImageLoader imgload;
   class ImageLoader extends Thread
   {
      int max;
      int perpage;
      int current=-1;
      String[] arqs;
      Image[] loaded;
      int imgW,imgH;
      int lastIni,lastEnd;
      IOException ex;
      
      public ImageLoader(int max, int perpage, String[] arqs)
      {
         this.max = max;
         this.perpage = perpage;
         this.arqs = arqs;
         loaded = new Image[arqs.length];
      }

      public void run()
      {
         int lastcur = -1;
         while (true)
         {
            while (lastcur == current)
               Vm.sleep(10);
            int range = max/2;
            lastcur = current;
            int lastMinIdx = lastcur - range; if (lastMinIdx < 0) lastMinIdx = 0; 
            int lastMaxIdx = lastcur + range; if (lastMaxIdx >= arqs.length) lastMaxIdx = arqs.length-1;
            if (lastIni != lastEnd)
            {
               for (int i = lastIni; i < lastMinIdx; i++) loaded[i] = null;
               for (int i = lastMaxIdx+1; i <= lastEnd; i++) loaded[i] = null;
            }
            lastIni = lastMinIdx;
            lastEnd = lastMaxIdx;
            ex = null;
            for (int i = lastMinIdx; i <= lastMaxIdx && lastcur == current; i++)
               if (loaded[i] == null)
                  loaded[i] = loadImage(i);
         }
      }
      
      private byte[] buf = new byte[1];
      private Image loadImage(int i)
      {
         Image img = null;
         try
         {
            File f = new File(imageFolder+arqs[i],File.READ_WRITE);
            int s = f.getSize();
            if (buf.length < s)
               buf = new byte[s];
            f.readBytes(buf,0,s);
            f.close();
            img = new Image(buf);
            img = FAST ? img.getScaledInstance(imgW,imgH) : img.getSmoothScaledInstance(imgW,imgH);
         }
         catch (IOException ioe)
         {
            Vm.debug(ioe.getMessage());
            ioe.printStackTrace();
            ex = ioe;
         }
         catch (Throwable e)
         {
            Vm.alert(e.getMessage()+" "+Vm.getStackTrace(e));
            e.printStackTrace();
         }
         return img;
      }

      private void setCurrent(int idx)
      {
         if (loaded[idx] != null && ((idx % perpage) != 0 || idx == current))
            return;
         current = idx;
      }
      
      /** Returns the image in the given size */ 
      public Image getImage(int idx, int imgW, int imgH) throws IOException
      {
         if (ex != null)
            throw ex;
         try
         {
            this.imgW = imgW;
            this.imgH = imgH;
            setCurrent(idx);
            while (loaded[idx] == null)
               Vm.sleep(10);
            return loaded[idx];
         }
         catch (Exception e) {e.printStackTrace();}
         return null;
      }
   }
   
   // class used to show the Image on screen.
   class DynImage extends Control
   {
      int idx;
      Image imgq;
      boolean highQuality;
      
      public DynImage(int idx, boolean comQualidade)
      {
         this.idx = idx;
         this.highQuality = comQualidade;
      }
      
      public void onPaint(Graphics g)
      {
         final int s = width < height ? width : height;
         Image img = null;
         try
         {
            if (highQuality)
            {
               if (imgq == null)
               {
                  File f = new File(imageFolder+imageNames[idx],File.READ_WRITE);
                  imgq = new Image(f);
                  f.close();
                  imgq = imgq.getSmoothScaledInstance(s,s);
               }
               img = imgq;
            }
            else img = imgload.getImage(idx,s,s);
            if (img != null)
               g.drawImage(img,(width-s)/2,(height-s)/2);
         }
         catch (Exception ioe) {}
         if (img == null)
         {
            // no memory, draw a rect with a x
            g.foreColor = Color.BLACK;
            g.drawRect(0,0,width,height);
            g.drawLine(0,0,width,height);
            g.drawLine(width,0,0,height);
         }
      }
      
      public void reposition()
      {
         // loads a new image when repositioned
         imgq = null;
         super.reposition();
      }
   }
   
   // class that represents a Grid's cell
   
   class ImgCell extends GridContainer.Cell
   {
      int idx;
      boolean highQuality;
      Label l1,l2;
      DynImage img;
      
      public ImgCell(int idx)
      {
         this(idx,false);
      }
      public ImgCell(int idx, boolean highQuality)
      {
         this.idx = idx;
         this.highQuality = highQuality;
         setBackColor(Color.WHITE);
         setBorderStyle(BORDER_SIMPLE); // uncomment to remove border
      }
      
      public void initUI()
      {
         setInsets(CELL_GAP,CELL_GAP,CELL_GAP,CELL_GAP);
         l1 = new Label("book "+(idx+1),CENTER); l1.autoSplit = true; 
         l2 = new Label("Written by J.R.Olling",CENTER); l2.autoSplit = true;
         add(l1,LEFT,TOP,FILL,PREFERRED); l1.reposition();
         add(l2,LEFT,BOTTOM,FILL,PREFERRED); l2.reposition();
         try 
         {
            add(img = new DynImage(idx,highQuality),LEFT,AFTER+IMAGE_GAP,FILL,FIT-IMAGE_GAP,l1);
         } catch (RuntimeException re) {img = null;}
      }      
      
      public void reposition()
      {
         super.reposition();
         l1.reposition();
         l2.reposition();
         img.reposition();
      }
   }

   // class used to show a selected cell in a window
   class CellBox extends Window
   {
      int idx;
      public CellBox(int idx)
      {
         this.idx = idx;
      }
      public void initUI()
      {
         add(new ImgCell(idx,true),LEFT,TOP,FILL,FILL);
      }
      public void onEvent(Event e)
      {
         super.onEvent(e);
         if (e.type == PenEvent.PEN_UP)
            unpop();
      }
   }

   public ImageBook()
   {
      setUIStyle(Settings.Android);
   }
   
   public void initUI()
   {
      try
      {
         // folder that contains the images. can be "/sdcard", for example
         imageFolder = Settings.appPath;
         if (Settings.onJavaSE)
            imageFolder += "/src/tc/samples/ui/imagebook";
         imageFolder += "/images/";
         // list images in the folder
         String[] arqs0 = new File(imageFolder).listFiles();
         // IMPORTANT: creates a single array with all image NAMES
         imageNames = new String[TOTAL_ITEMS];
         for (int i = 0; i < imageNames.length; i++)
            imageNames[i] = arqs0[i % arqs0.length];
         // defines orientation
         int dir;
         if (Settings.appSettings == null)
         {
            dir = GridContainer.HORIZONTAL_ORIENTATION;
            Settings.appSettings = "nak";
            new MessageBox("Instructions","Click on first book to exit. Click on other book for details. Exit and open again to scroll VERTICALLY.").popup();
         }
         else
         {
            dir = GridContainer.VERTICAL_ORIENTATION;
            Settings.appSettings = null;
            new MessageBox("Instructions","Click on first book to exit. Click on other book for details. Exit and open again to scroll HORIZONTALLY.").popup();
         }
         // starts the ImageLoader
         imgload = new ImageLoader(GRID_LINES*GRID_COLS*CACHE_PAGES, GRID_LINES*GRID_COLS, imageNames);
         imgload.start();

         setBackColor(Color.WHITE);
            
         // defines the orientation: VERTICAL_ORIENTATION ou HORIZONTAL_ORIENTATION
         add(gc = new GridContainer(dir),LEFT,TOP,FILL,FILL);
         gc.setBackColor(Color.WHITE);
         Flick f = gc.getFlick();
         f.shortestFlick = 1000; // increases because the image's load time affects the flick
         f.longestFlick = TOTAL_ITEMS > 500 ? 6000 : 2500;
         gc.setPageSize(GRID_LINES,GRID_COLS);
         gc.setRowsPerPage(LINES_PER_PAGE);
         addCells();
         repaintNow();
         
         Control[] cc = gc.getChildren();
         ScrollContainer sc = (ScrollContainer)cc[1];
         cc = null;
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   private void addCells()
   {
      ImgCell []cels = new ImgCell[TOTAL_ITEMS];
      for (int i = 0; i < cels.length; i++)
         cels[i] = new ImgCell(i);
      gc.setCells(cels);
   }

   // intercepts the cell pressed event and shows it in a popup screen
   public void onEvent(Event e)
   {
      super.onEvent(e);
      if (e.type == ControlEvent.PRESSED && e.target instanceof ImgCell)
      {
         int idx = ((ImgCell)e.target).idx;
         if (idx == 0)
            exit(0);
         else
         {
            CellBox mc = new CellBox(idx);
            mc.setRect(CENTER,CENTER,SCREENSIZE+80,SCREENSIZE+80);
            mc.popup();
         }
      }
         
   }
}