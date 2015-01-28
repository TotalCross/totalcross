/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.util.*;
import totalcross.util.zip.*;

/** Converts a Windows true type font to a pdb file that can be used by TotalCross programs
  * (and also by other programs)
  * Must be compiled with JDK 1.2.2 or above.
  */

public class FontGenerator
{
   public static byte detailed=0; // 1 show messages, 2 show msgs + chars

   class Range 
   {
      int s, e; 
      String name; 
      
      Range(int ss, int ee, String nn) 
      {
         s = ss; 
         e = ee; 
         name=nn;
      }
   }

   String fontName;
   PalmFont pf;
   int antialiased;
   Vector newRanges;
   java.awt.Component comp;
   static IntVector sizes = new IntVector(30);
   boolean skipBigChars;
   int[] fontsizes = {7,8,9,10,11,12,13,14,15,16,17,18,19,20,40,60,80};


   public FontGenerator(String fontName, String []extraArgs) throws Exception
   {
      int i;
      String sizesArg=null;
      comp = new java.awt.Frame();
      comp.addNotify();
      newRanges = new Vector();
      newRanges.addElement(new Range(32, 255, "u0")); // default range
      String jdkversion = System.getProperty("java.version");
      if (jdkversion.startsWith("1.1.") || jdkversion.startsWith("1.2."))
         throw new Exception("This program requires JDK version greater or equal than 1.3!");
      String outName = fontName; // guich@401_11
      boolean noBold = false;
      boolean isMono = false;
      for (i=1; i < extraArgs.length; i++) // 0 if font name
         if (extraArgs[i] != null)
         {
            String arg = extraArgs[i];
            String argLow = arg.toLowerCase();
            if (argLow.indexOf("monospace") >= 0)
               isMono = true;
            else
            if (argLow.equals("/skipbigchars"))
               skipBigChars = true;
            else
            if (argLow.equals("/nobold"))
               noBold = true;
            else
            if (argLow.startsWith("/sizes"))
               sizesArg = argLow.substring(argLow.indexOf(':')+1);
            else
            if (argLow.equals("/aa"))
               antialiased = AA_8BPP; // always force 8bpp
            else
            if (argLow.equals("/aa8"))
               antialiased = AA_8BPP;
            else
            if (argLow.startsWith("/rename:"))
            {
               outName = arg.substring(8);
               if (outName.indexOf('.') >= 0) // guich@tc123_11
                  throw new Exception("Invalid rename parameter: "+outName);
            }
            else
            if (argLow.startsWith("/detailed:"))
            {
               detailed = (byte)(arg.charAt(10)-'0');
               if (detailed != 1 && detailed != 2)
               {
                  println("Invalid detailed value. Resetting to 0");
                  detailed = 0;
               }
            }
            else
            if (argLow.startsWith("/u"))
            {
               String[] ranges = new String[extraArgs.length-i-1];
               for (int k =0; ++i < extraArgs.length;)
                  if ((ranges[k++] = extraArgs[i]).indexOf('-') < 0)
                     throw new Exception("Invalid range '"+extraArgs[i]+"' is not in the format 'start-end'");
               processRanges(ranges);
            }
            else
               throw new Exception("Invalid argument: "+extraArgs[i]);
         }

      totalcross.sys.Settings.showDesktopMessages = false;
      // parse parameters
      if (fontName.indexOf('_') != -1) // guich@421_26: lets the user pass fonts with spaces
         fontName = fontName.replace('_',' ');
      this.fontName = fontName;
      String realName;
      if (!(realName=new java.awt.Font(fontName, java.awt.Font.PLAIN, 14).getFontName()).toLowerCase().startsWith(fontName.toLowerCase()))
      {
         println("Font "+fontName+" not found. Was replaced by "+realName+". Run the program without arguments to see the list of possible fonts.");
         System.exit(1);
      }
      // create fonts
      println("FontGenerator - Copyright (c) SuperWaba 2002-2012. Processing...");
      if (sizesArg != null)
      {
         String[] ss = totalcross.sys.Convert.tokenizeString(sizesArg, ',');
         for (i =0; i < ss.length; i++)
            sizes.addElement(totalcross.sys.Convert.toInt(ss[i]));
         sizes.qsort();
      }
      else
         for (i = 0; i < fontsizes.length; i++)
            sizes.addElement(fontsizes[i]);
      Vector v = new Vector(30);

      for (i = 0; i < sizes.size(); i++)
      {
         int s = sizes.items[i];
         convertFont(v, new java.awt.Font(fontName, java.awt.Font.PLAIN, s), outName+"$p"+s, newRanges, isMono);
         if (!noBold)
            convertFont(v, new java.awt.Font(fontName, java.awt.Font.BOLD, s), outName+"$b"+s, newRanges, isMono);
      }

      // write the file
      try {new File(outName).delete();} catch (Exception e) {} // delete if it exists
      new TCZ(v, outName, (short)0);
      // test fonts
      runTestFont(outName);
   }

   private void convertFont(Vector v, java.awt.Font f, String fileName, Vector newRanges, boolean isMono)
   {
      java.awt.FontMetrics fm = comp.getFontMetrics(f);
      int width = fm.charWidth('@')*4;
      int height = fm.getHeight()*4;

      java.awt.Image img = comp.createImage(width,height);
      java.awt.Graphics2D g = (java.awt.Graphics2D)img.getGraphics();
      try {g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, antialiased != AA_NO ? java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON:java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);} catch (Throwable t) {println("Antialiased font not supported!"); System.exit(2);}
      g.setFont(f);

      // Note: Sun's JDK does not return a optimal width value for the
      // characters, notabily chars W,V,T,A. Because of this, we need to
      // find the width ourselves.
      // Note2: due to this, monospaced fonts may not be converted properly
      int widths[] = new int[65536];
      byte gaps[] = new byte[65536];
      short sum = 0;
      int totalBits=0;
      int maxW = 0;
      int maxH = 0;
      int i;
      int wW=0;

      // first, we have to compute the sizes for all ranges
      if (detailed == 0) System.out.println("Computing bounds for "+f);
      int x = width>>2;
      int n = newRanges.size();
      char back = 8;
      String backs = ""+back+back+back+back+back+back;
      for (int ri = 0; ri < n; ri++)
      {
         Range rr = (Range)newRanges.items[ri];
         if (detailed == 0)
            System.out.print(backs+((ri+1)*100/n)+"%");
         int ini = rr.s;
         int end = rr.e;
         for (i = ini; i <= end; i++)
         {
            int ch = i;
            g.setColor(java.awt.Color.white);
            g.fillRect(0,0,width,height);
            g.setColor(java.awt.Color.black);
            if (ch > 255 && !f.canDisplay((char)ch)) // guich@tc115_69
            {
               System.out.println("Warning! The true type font cannot display the character number "+i+". Character will be replaced by space.");
               ch = ' ';
            }
            g.drawString(totalcross.sys.Convert.toString((char)ch), x, height>>2);

            totalcross.ui.gfx.Rect r = computeBounds(getPixels(img, width,height), width);

            if (r == null) // blank char?
            {
               if (detailed==1 && i == ch) println("char "+ch+" is blank");
               widths[i] = fm.charWidth(ch);
            }
            else
            {
               int w = r.width+1; // +1 for interchar spacing - guich@560_15: use java's if monospaced font
               
               // guich@tc126_44: skip chars above normal
               if (wW == 0 && ch == 'W')
                  wW = w;
               else
               if (wW > 0 && w > wW)
               {
                  println("Skipped char "+ch+" "+(char)ch+": "+w);
                  continue;
               }
               
               int h = r.height;
               int gap = x - r.x;
               maxW = Math.max(maxW,w);
               maxH = Math.max(maxH,h);
               gaps[i] = (byte)gap;//<=0 ? 1 : 0; // most chars Java puts 1 pixel away; some chars Java puts one pixel near; for those chars, we make them one pixel right
               widths[i] = w;
               if (detailed==1) println("Width of "+(char)ch+" = "+widths[i]+" - gap: "+gap);
            }
         }
      }
      if (isMono) // guich@tc113_31
      {
         for (i = 0; i < widths.length; i++)
            widths[i] = maxW;
         println("Setting all widths to "+maxW);
      }
      
      maxH = fm.getHeight();
      int nullCount = 0;
      if (detailed == 0) System.out.println(backs+"Saving glyphs");
      // now, for each range, compute the totalbits and the chars
      for (int ri = 0; ri < n; ri++)
      {
         Range rr = (Range)newRanges.items[ri];
         int ini = rr.s;
         int end = rr.e;
         totalBits = 0;
         for (i = ini; i <= end; i++)
            totalBits += widths[i];

         // Create the PalmFont and set its parameters
         println((ri+1)+" of "+n+". Original height: "+fm.getHeight()+", ascent: "+fm.getMaxAscent()+", descent: "+fm.getMaxDescent()+", leading: "+fm.getLeading());
         pf = new PalmFont(fileName+rr.name);
         pf.antialiased = antialiased;
         pf.firstChar   = ini;
         pf.lastChar    = end;
         pf.spaceWidth  = isMono ? maxW : fm.charWidth(' '); // guich@tc115_87
         pf.maxWidth    = maxW;
         pf.maxHeight   = fm.getHeight();
         pf.descent     = fm.getMaxDescent();
         pf.ascent      = (maxH-pf.descent);
         pf.rowWords    = ((totalBits+15) / 16);
         pf.rowWords    = (((int)(pf.rowWords+1)/2)*2); // guich@400_67
         pf.owTLoc      = (pf.rowWords*pf.maxHeight+(pf.lastChar-pf.firstChar)+8);
         pf.debugParams();
         if (detailed>=1) println("totalBits: "+totalBits+", rowWords: "+pf.rowWords);

         pf.initTables();

         // fill in PalmFont tables
         sum = 0;
         for (i =ini; i <= end; i++)
         {
            pf.bitIndexTable[i-ini] = sum;
            sum += widths[i];
         }
         pf.bitIndexTable[i-ini] = sum;  i++;

         // draw the chars in the image and decode that image
         sum = 0;
         for (i = ini; i <= end; i++)
         {
            int ww = widths[i];

            g.setColor(java.awt.Color.white);
            g.fillRect(0,0,width,height);
            g.setColor(java.awt.Color.black);
            g.drawString(totalcross.sys.Convert.toString((char)i), gaps[i], pf.ascent);
            int[] pixels = getPixels(img, ww,maxH);
            if (pixels != null) // guich@tc110_5
               computeBits(pixels, sum, ww);
            else
               nullCount++;
            sum += ww;
         }
         // save the image
         v.addElement(pf.save());
      }
      if (nullCount > 0) System.out.println("A total of "+nullCount+" characters had no glyphs");
   }

   private totalcross.ui.gfx.Rect computeBounds(int pixels[], int w)
   {
      int white = -1;
      int x=0,y=0;
      int xmin=10000, ymin=10000, xmax=-1, ymax=-1;
      for (int i = 0; i < pixels.length; i++)
      {
         if (pixels[i] != white)
         {
            xmin = Math.min(xmin,x);
            xmax = Math.max(xmax,x);
            ymin = Math.min(ymin,y);
            ymax = Math.max(ymax,y);
         }

         if (++x == w)
         {
            x = 0;
            y++;
         }
      }
      if (xmin == 10000) // empty char?
         return null;

      return new totalcross.ui.gfx.Rect(xmin,ymin,xmax-xmin+1,ymax-ymin+1);
   }

   int bits[] = {128,64,32,16,8,4,2,1};

   private void setBit(int x, int y)
   {
      pf.bitmapTable[(x>>3) + y * pf.rowWidthInBytes] |= bits[x % 8];  // set
      if (detailed==2) System.out.print((char)('@'));
   }

   private void setNibble4(int pixel, int x, int y)
   {
      int nibble = 0xF0 - (pixel & 0xF0);  // 4 bits of transparency
      if (detailed==2) System.out.print((char)('a'+nibble)/*'@'*/);
      if ((x & 1) != 0)
         nibble >>= 4;
      pf.bitmapTable[(x>>1) + (y * pf.rowWidthInBytes)] |= nibble;  // set
   }
   
   private void setNibble8(int pixel, int x, int y)
   {
      int nibble = 0xFF - (pixel & 0xFF); // FFF5F5F5
      if (detailed==2) System.out.print((char)('a'+nibble)/*'@'*/);
      pf.bitmapTable[x + (y * pf.rowWidthInBytes)] = (byte)nibble;  // set
   }

   private void computeBits(int pixels[], int xx, int w)
   {
      int white = -1;
      int x=0,y=0;
      for (int i = 0; i < pixels.length; i++)
      {
         if (pixels[i] != white)
         {
            switch (antialiased)
            {
               case AA_NO: setBit(xx+x,y); break;
               case AA_4BPP: setNibble4(pixels[i], xx+x, y); break;
               case AA_8BPP: 
                  setNibble8(pixels[i], xx+x, y); 
                     //AndroidUtils.debug("@"+(xx+x)+","+y+": "+Integer.toHexString(pixels[i]).toUpperCase()+" -> "+Integer.toHexString(pf.bitmapTable[xx+x + (y * pf.rowWidthInBytes)] & 0xFF).toUpperCase());
                  break;
            }               
         }
         else
         if (detailed==2) System.out.print(' ');

         if (++x == w)
         {
            x = 0;
            y++;
            if (detailed==2) println("");
         }
      }
   }

   /*
   chi: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 19968-40869 65281-65374 65504-65510
   Jap: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 12353-12435 12449-12534 65281-65374 65504-65510
   Kor: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 44032-55203 65281-65374 65504-65510
   Internal ranges are stored as 0-255, 256-511, 512-767, ...
   So, 160-383 is splitted into 160-255, 256-383
    */
   private void processRanges(String[] ranges)
   {
      int n = ranges.length,s,e=0,first,last;
      newRanges = new Vector();
      // first we set all bits defined in the given ranges
      IntVector iv = new IntVector();
      iv.ensureBit(65535);
      iv.setBit(' ',true); // MUST include the space in the range
      for (int i =0; i < n; i++)
      {
         String r = ranges[i];
         String s1 = r.substring(0,r.indexOf('-'));
         String s2 = r.substring(r.indexOf('-')+1);
         s = Integer.parseInt(s1);
         e = Integer.parseInt(s2);
         while (s <= e)
            iv.setBit(s++,true);
      }
      int max = e/256+1;
      // now we create the ranges in 256-char groups.
      for (int i =0; i < max; i++)
      {
         s = i * 256;
         e = (i+1)*256-1;
         first = last = -1;
         // find the first and the last bit set in the range
         for (int j = s; j <= e; j++)
            if (iv.isBitSet(j))
            {
               if (first == -1)
                  first = j;
               last = j;
            }
         if (first != -1)
         {
            Range rr = new Range(first, last, "u"+s);
            newRanges.addElement(rr);
            println("Unicode range "+rr.name+": "+rr.s+" - "+rr.e);
         }
      }
   }

   public static int[] getPixels(java.awt.Image img, int width, int height)
   {
      java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(img,0,0,width,height,true);
      try {pg.grabPixels();} catch (InterruptedException ie) {System.out.println("interrupted!");}
      return (int [])pg.getPixels();
   }

   static public void println(String s)
   {
      System.out.println(s);
   }

   public static void runTestFont(String fontName)
   {
      if (true)
      {
         totalcross.Launcher.main(new String[]
         {
            "/scr",      "800x600x24",
            "/scale",    "1",
            "/cmdLine",  fontName,
            "tc.tools.FontGenerator$TestFont"
         }); // guich@503_6: fixed package name
         while (true)
            totalcross.sys.Vm.sleep(100);
      }
      else System.exit(0);
   }
   public static void main(String args[])
   {
      try
      {
         java.awt.Font fonts[] = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
         if (args.length < 1)
         {
            println("Format: java FontGenerator <font name> /rename:newName /detailed:1_or_2 /aa");
            println("  /NoBold /sizes:<comma-separeted list of sizes> /u <list of ranges>");
            println("");
            println("Parameters are case insensitive, meaning:");
            println(". /monospace To create a monospaced font.");
            println(". /rename:newName to rename the output font name.");
            println(". /detailed:1_or_2 to show detailed information.");
            println(". /aa to create a 4-bpp antialiased font.");
            println(". /aa8 to create a 8-bpp antialiased font.");
            println("  /sizes:<comma-separeted list of sizes> to create a font with the given sizes");
            println(". /NoBold to don't create the bold font.");
            println(". /skipBigChars: useful when creating monospaced fonts; the glyphs that have a width above the W char are skipped.");
            println(". /u to create unicode chars in the range. By default, we create chars in the");
            println("range 32-255. Using this option, you can pass ranges in the form");
            println("\"start0-end0 start1-end1 start2-end2 ...\", which creates a file containing the");
            println("characters ranging from \"startN <= c <= endN\". For example:");
            println("\"/u 32-255 256-383 402-402 20284-40869\". The ranges must be in increased order.");
            println("The /u option must be the LAST option used.");
            println("");
            println("When creating unicode fonts of a wide range, using options /nobold");
            println("will create a file 1/4 of the original size.");
            println("");
            println("Alternative format: java FontGenerator test <font name>");
            println("This will open a TotalCross app to test the font");
            println("");
            println("Copyright (c) SuperWaba 2002-2012");
            println("Must use JDK 1.3 or higher!");
            println("\nPress enter to list the available fonts, or q+enter to stop.");
            try
            {
               if (Character.toLowerCase((char)System.in.read()) != 'q')
                  for (int i =0 ; i < fonts.length; i++)
                     println(" "+fonts[i].getName());
            } catch (java.io.IOException ie) {}
         }
         else
         if (args[0].equalsIgnoreCase("test"))
            runTestFont(args[1]);
         else
            new FontGenerator(args[0],args);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   static final int AA_NO = 0;
   static final int AA_4BPP = 1;
   static final int AA_8BPP = 2;
   static class PalmFont
   {
      public int antialiased;      // true if its antialiased
      public int firstChar;        // ASCII code of first character
      public int lastChar;         // ASCII code of last character
      public int spaceWidth;       // width of the space char
      public int maxWidth;         // width of font rectangle
      public int maxHeight;        // height of font rectangle
      public int owTLoc;           // offset to offset/width table
      public int ascent;           // ascent
      public int descent;          // descent
      public int rowWords;         // row width of bit image / 2

      public int   rowWidthInBytes;
      public int   bitmapTableSize;
      public byte  []bitmapTable;
      public int []bitIndexTable;

      public String fileName;

      public TCZ.Entry save()
      {
         try
         {
            ByteArrayStream from = new ByteArrayStream(4096);
            ByteArrayStream to = new ByteArrayStream(2048);
            DataStreamLE ds = new DataStreamLE(from);
            ds.writeShort(antialiased); // note that writeShort already writes an unsigned short (its the same method)
            ds.writeShort(firstChar  );
            ds.writeShort(lastChar   );
            ds.writeShort(spaceWidth );
            ds.writeShort(maxWidth   );
            ds.writeShort(maxHeight  );
            ds.writeShort(owTLoc     );
            ds.writeShort(ascent     );
            ds.writeShort(descent    );
            ds.writeShort(rowWords   );
            ds.writeBytes(bitmapTable);
            for (int i=0; i < bitIndexTable.length; i++)
               ds.writeShort(bitIndexTable[i]);
            // compress the font
            int f = from.getPos(),s;
            from.mark();
            s = ZLib.deflate(from, to, 9);
            // write the name uncompressed before it
            println("Font "+fileName+" stored compressed ("+f+" -> "+s+")");
            return new TCZ.Entry(to.toByteArray(),  fileName.toLowerCase(),f);
         } catch (Exception e) {e.printStackTrace();}
         return null;
      }
      public PalmFont(String fileName)
      {
         this.fileName = fileName;
      }

      public void debugParams()
      {
         println("antialiased: "+antialiased);
         println("firstChar  : "+firstChar  );
         println("lastChar   : "+lastChar   );
         println("spaceWidth : "+spaceWidth );
         println("maxWidth   : "+maxWidth   );
         println("maxHeight  : "+maxHeight  );
         println("owTLoc     : "+owTLoc     );
         println("ascent     : "+ascent     );
         println("descent    : "+descent    );
         println("rowWords   : "+rowWords   );
      }

      public void initTables()
      {
         rowWidthInBytes = 2 * rowWords * (antialiased == AA_NO ? 1 : antialiased == AA_4BPP ? 4 : 8); // 4 bits of transparency or 1 bit (B/W)
         bitmapTableSize = (int)rowWidthInBytes * (int)maxHeight;

         bitmapTable     = new byte[bitmapTableSize];
         bitIndexTable   = new int[lastChar - firstChar + 1 + 1];
      }

      public int charWidth(char ch)
      {
         int index = ch - firstChar;
         return index < 0 || index > lastChar ? spaceWidth : bitIndexTable[index+1] - bitIndexTable[index];
      }
   }
   public static class TestFont extends MainWindow
   {
      private Button btn;

      public TestFont()
      {
         super("", TAB_ONLY_BORDER);
         setUIStyle(Settings.Vista);
      }
      public void initUI()
      {
         Label l;
         String fontName = getCommandLine();
         setTitle("Test Font - "+fontName);
         if (fontName == null || fontName.length() == 0)
            exit(1);

         String tit = "__ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678890`~!@#$%^&*()_+=-{}\\][:;\"'<,>./?�������";
         if (sizes.size() == 0) // occurs when the user call the TestFont directly
         {
            for (int i = Font.MIN_FONT_SIZE; i <= Font.MAX_FONT_SIZE; i++)
               sizes.addElement(i);
         }
         for (int ii = 0; ii < sizes.size(); ii++)
         {
            int i = sizes.items[ii];
            // how only the fonts that match this name.
            Font ff = Font.getFont(fontName, false, i);
            if (ff.name.equals(fontName))
            {
               l = new Label(i+" "+tit);
               l.setFont(ff);
               add(l,LEFT,AFTER);
            }
            ff = Font.getFont(fontName, true, i);
            if (ff.name.equals(fontName))
            {
               l = new Label(i+" "+tit);
               l.setFont(ff);
               add(l,LEFT,AFTER);
            }
         }
         add(btn = new Button("Exit"), RIGHT,0);
      }
      public void onEvent(Event e)
      {
         if (e.type == ControlEvent.PRESSED && e.target == btn)
            exit(0);
      }
   }
}