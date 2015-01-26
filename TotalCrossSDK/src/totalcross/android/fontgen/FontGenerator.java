package totalcross.android.fontgen;

import totalcross.*;

import java.io.*;
import java.nio.*;
import java.util.*;

import android.graphics.*;
import android.os.*;

public class FontGenerator
{
   final int MAX_FONT_SIZE=120;
   int[] fontsizes = {7,8,9,10,11,12,13,14,15,16,17,18,19,20,40,60,80};
   static boolean generated;

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
   public static byte detailed; // 1 show messages, 2 show msgs + chars

   String fontName;
   PalmFont pf;
   int antialiased;
   Vector<Range> newRanges;
   boolean skipBigChars;

   public FontGenerator(String fontName, String []extraArgs)
   {
      try
      {
         if (generated) return;
         generated = true;
         int i;
         newRanges = new Vector<Range>();
         newRanges.addElement(new Range(32, 255, "u0")); // default range
         String outName = fontName; // guich@401_11
         boolean noBold = false;
         boolean isMono = false;
         if (extraArgs != null)
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
               if (argLow.equals("/aa"))
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
   
         // parse parameters
         if (fontName.indexOf('_') != -1) // guich@421.27: lets the user pass fonts with spaces
            fontName = fontName.replace('_',' ');
         this.fontName = fontName;
         
         Typeface ttfp = Typeface.DEFAULT;//createFromAsset(Launcher4A.instance.getContext().getAssets(), "fonts/"+fontName+".ttf");
         Typeface ttfb = Typeface.DEFAULT_BOLD;//createFromAsset(Launcher4A.instance.getContext().getAssets(), "fonts/"+fontName+"bd.ttf");
         
         // create fonts
         println("FontGenerator - Copyright (c) SuperWaba 2002-2014. Processing...");
         Vector<TCZ.Entry> v = new Vector<TCZ.Entry>(30);
         if (antialiased != AA_8BPP)
         {
            fontsizes = new int[48-7+1];
            for (i = 7; i <= 48; i++)
               fontsizes[i-7] = i;
         }
         for (int s = 0; s < fontsizes.length; s++)
         {
            i = fontsizes[s];
            convertFont(v, ttfp, i, outName+"$p"+i, newRanges, isMono);
            if (!noBold)
               convertFont(v, ttfb, i, outName+"$b"+i, newRanges, isMono);
         }
   
         // write the file
         try {new File(Environment.getExternalStorageDirectory()+"/"+outName).delete();} catch (Exception e) {} // delete if it exists
         new TCZ(v, outName, (short)0);
      }
      catch (Exception e) {e.printStackTrace();}
   }

   private Paint setFontSize(int size, Typeface ttf)
   {
      Paint paint = new Paint();
      paint.setTypeface(ttf);
      paint.setColor(Color.BLACK);
      paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
      Rect r = new Rect();

      for (float i = size/2; i < MAX_FONT_SIZE*2; i+=0.01)
      {
         paint.setTextSize(i);
         paint.getTextBounds("Ãg_",0,3,r);
         //int maxH = (int)(Math.abs(paint.descent()) + Math.abs(paint.ascent()));
         int realH = Math.abs(r.top - r.bottom);
         //AndroidUtils.debug(i+": "+realH);
         if (realH >= size)
         {
            AndroidUtils.debug("logic size: "+size+" real size: "+i);
            return paint;
         }
      }
      return null;
   }
   
   private void convertFont(Vector<TCZ.Entry> v, Typeface ttf, int size, String fileName, Vector<Range> newRanges, boolean isMono)
   {
      println("CONVERTING FONT LOGIC SIZE "+size);
      
      Bitmap bmp;
      Canvas g;
      Paint paint = setFontSize(size, ttf);
      Rect r = new Rect();
      paint.getTextBounds("Ã",0,1,r);
      int yy = Math.abs(r.top - r.bottom);

      // Note: Sun's JDK does not return a optimal width value for the
      // characters, notabily chars W,V,T,A. Because of this, we need to
      // find the width ourselves.
      // Note2: due to this, monospaced fonts may not be converted properly
      int widths[] = new int[65536];
      byte gaps[] = new byte[65536];
      short sum = 0;
      int totalBits=0;
      int maxW = 0;
      int i;

      // first, we have to compute the sizes for all ranges
      //if (detailed == 0) System.out.println("Computing bounds for "+f);
      int n = newRanges.size();
      for (int ri = 0; ri < n; ri++)
      {
         Range rr = newRanges.get(ri);
         int ini = rr.s;
         int end = rr.e;
         for (i = ini; i <= end; i++)
         {
            String si = String.valueOf((char)i);
            int w = (int)paint.measureText(si)+1;
            maxW = Math.max(maxW,w);
            widths[i] = w;
            if (detailed==1) println("Width of "+((char)i)+" "+i+" = "+widths[i]);
         }
      }
      if (isMono) // guich@tc113_31
      {
         for (i = 0; i < widths.length; i++)
            widths[i] = maxW;
         println("Setting all widths to "+maxW);
      }
      
      int nullCount = 0;
      
      // now, for each range, compute the totalbits and the chars
      for (int ri = 0; ri < n; ri++)
      {
         Range rr = newRanges.get(ri);
         int ini = rr.s;
         int end = rr.e;
         totalBits = 0;
         for (i = ini; i <= end; i++)
            totalBits += widths[i];

         // Create the PalmFont and set its parameters
         //println((ri+1)+" of "+n+". Original height: "+maxH+", ascent: "+fm.getMaxAscent()+", descent: "+fm.getMaxDescent()+", leading: "+fm.getLeading());
         pf = new PalmFont(fileName+rr.name);
         pf.antialiased = antialiased;
         pf.firstChar   = ini;
         pf.lastChar    = end;
         pf.spaceWidth  = isMono ? maxW : (int)paint.measureText(" "); // guich@tc115_87
         pf.maxWidth    = maxW;
         pf.maxHeight   = size;
         pf.descent     = (int)paint.descent();
         pf.ascent      = (size-pf.descent);
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
            if (ww > 0)
            {
               bmp = Bitmap.createBitmap(ww, size, Bitmap.Config.ARGB_8888);
               g = new Canvas(bmp);
   
               String si = String.valueOf((char)i);
               
               bmp.eraseColor(Color.WHITE);
               g.drawText(si, gaps[i], yy, paint);
               IntBuffer ibuf = IntBuffer.allocate(ww*size);
               bmp.copyPixelsToBuffer(ibuf);
               computeBits(ibuf.array(), sum, ww, i == '#' && size == 70);
            }
            sum += ww;
         }
         // save the image
         v.addElement(pf.save());
      }
      if (nullCount > 0) println("A total of "+nullCount+" characters had no glyphs");
   }

   int bits[] = {128,64,32,16,8,4,2,1};
   
   private void setBit(int x, int y)
   {
      pf.bitmapTable[(x>>3) + y * pf.rowWidthInBytes] |= bits[x % 8];  // set
   }

   private void setNibble4(int pixel, int x, int y)
   {
      int nibble = 0xF0 - (pixel & 0xF0);  // 4 bits of transparency
      if ((x & 1) != 0) nibble >>= 4;
      pf.bitmapTable[(x>>1) + (y * pf.rowWidthInBytes)] |= nibble;  // set
   }

   private void setNibble8(int pixel, int x, int y)
   {
      int nibble = 0xFF - (pixel & 0xFF); // FFF5F5F5
      pf.bitmapTable[x + (y * pf.rowWidthInBytes)] = (byte)nibble;  // set
   }

   private void computeBits(int pixels[], int xx, int w, boolean print)
   {
      final int white = -1;
      int x=0,y=0;
      //StringBuffer sb = print ? new StringBuffer(50) : null;
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
         //if (print) sb.append(pixels[i] != white ? "#" : " ");
         if (++x == w)
         {
            //if (print){ AndroidUtils.debug(sb.toString()); sb.setLength(0);}
            x = 0;
            y++;
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
      newRanges = new Vector<Range>();
      // first we set all bits defined in the given ranges
      BitSet iv = new BitSet(65535);
      iv.set(' ',true); // MUST include the space in the range
      for (int i =0; i < n; i++)
      {
         String r = ranges[i];
         String s1 = r.substring(0,r.indexOf('-'));
         String s2 = r.substring(r.indexOf('-')+1);
         s = Integer.parseInt(s1);
         e = Integer.parseInt(s2);
         while (s <= e)
            iv.set(s++,true);
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
            if (iv.get(j))
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

   static public void println(String s)
   {
      AndroidUtils.debug(s);
   }

   public static void main(String args[])
   {
      try
      {
         if (args.length < 1)
         {
            String msg = 
            "Format: java FontGenerator <font name> /rename:newName /detailed:1_or_2 /aa\n" +
            "  /DefaultSizes /NoBold /sizes:<comma-separeted list of sizes> /u <list of ranges>\n" +
            "\n" +
            "Parameters are case insensitive, meaning:\n" +
            ". /monospace To create a monospaced font.\n" +
            ". /rename:newName to rename the output font name.\n" +
            ". /detailed:1_or_2 to show detailed information.\n" +
            ". /aa to create an antialiased font.\n" +
            ". /NoBold to don't create the bold font.\n" +
            ". /skipBigChars: useful when creating monospaced fonts; the glyphs that have a width above the W char are skipped.\n" +
            ". /u to create unicode chars in the range. By default, we create chars in the\n" +
            "range 32-255. Using this option, you can pass ranges in the form\n" +
            "\"start0-end0 start1-end1 start2-end2 ...\", which creates a file containing the\n" +
            "characters ranging from \"startN <= c <= endN\". For example:\n" +
            "\"/u 32-255 256-383 402-402 20284-40869\". The ranges must be in increased order.\n" +
            "The /u option must be the LAST option used.\n" +
            "\n" +
            "When creating unicode fonts of a wide range, using options /nobold /defaultsizes\n" +
            "will create a file 1/4 of the original size.\n" +
            "\n" +
            "Alternative format: java FontGenerator test <font name>\n" +
            "This will open a TotalCross app to test the font\n" +
            "\n" +
            "Copyright (c) SuperWaba 2002-2011\n";
            Launcher4A.alert(msg);
         }
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
      public int antialiased;  // true if its antialiased
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
            ByteArrayOutputStream from = new ByteArrayOutputStream(4096);
            TCZ.writeShort(from,antialiased);
            TCZ.writeShort(from,firstChar  );
            TCZ.writeShort(from,lastChar   );
            TCZ.writeShort(from,spaceWidth );
            TCZ.writeShort(from,maxWidth   );
            TCZ.writeShort(from,maxHeight  );
            TCZ.writeShort(from,owTLoc     );
            TCZ.writeShort(from,ascent     );
            TCZ.writeShort(from,descent    );
            TCZ.writeShort(from,rowWords   );
            from.write(bitmapTable);
            for (int i=0; i < bitIndexTable.length; i++)
               TCZ.writeShort(from,bitIndexTable[i]);
            // compress the font
            byte[] fromBytes = from.toByteArray();
            byte[] toBytes = TCZ.compress(fromBytes);
            println("Font "+fileName+" stored compressed ("+fromBytes.length+" -> "+toBytes.length+")");
            return new TCZ.Entry(toBytes, fileName.toLowerCase(),fromBytes.length);
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
}
