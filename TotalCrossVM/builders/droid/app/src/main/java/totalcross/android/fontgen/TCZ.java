// Copyright (C) 2000-2011 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only


package totalcross.android.fontgen;

import totalcross.*;

import android.os.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
    A tcz (TotalCross Zip) file has the following format:
    <ul>
    <li> version     (2 bytes)
    <li> attributes  (2 bytes)
    <li> base offset (header size + 4)
    <li> header
    <li> compressed data chunks
    </ul>

    The header is:

    <ul>
    <li> length      (4 bytes)
    <li> offsets array (length+1) - offset[i+1]-offset[i] gives the compressed size
    <li> uncompressed sizes array (length)
    <li> names array (length)
    </ul>

    The header is compressed to save space.
    The first record is the class that implements totalcross.MainClass or extends totalcross.ui.MainWindow.
*/

public class TCZ
{
   /** Set this to be the main class name. It is stored in record #0. */
   public static String mainClassName; // remove dependency on DeploySettings

   /** An entry of the TCZ file. */
   public static class Entry
   {
      /** The compressed byte block. */
      public byte[] bytes;
      /** The name of the entry. */
      public String name;
      /** The size of the block when it is uncompressed. */
      public int uncompressedSize;
      /** Anything you want to hold here. */
      public Object extra; // JavaClass for the converter
      
      private String name2write; // guich@tc115_23

      public Entry(byte []bytes, String name, int uncompressedSize) throws Exception
      {
         this(bytes, name, uncompressedSize, null);
      }

      public Entry(byte []bytes, String name, int uncompressedSize, Object extra) throws Exception
      {
         this.uncompressedSize = uncompressedSize;
         this.bytes = bytes;
         this.name = name.replace('\\','/');
         this.extra = extra;
         if (name.endsWith(".class"))
            name = name.substring(0,name.length()-6);
         name2write = name;
         if (name2write.indexOf('.') < 0) // no dots on string? note that an image will have a dot, so its correctly kept with / on the name
            name2write = name2write.replace('/','.'); // assuming not ending with .class
      }

      /** Returns a String representing this Entry. Used in the Vector.qsort method */
      public String toString()
      {
         if (mainClassName != null && name.equals(mainClassName))
            return '\1'+name2write; // make sure this name will go first when sorting
         return name2write;
      }
   }

   public static final short TCZ_VERSION = 106; // must sync with tcz.h

   /** Defines that the tcz file has a MainClass at the first record. If false, it is a library-only module */
   public static final short ATTR_HAS_MAINCLASS = 1;  
   /** Defines that the tcz file has a MainWindow at the first record. */
   public static final short ATTR_HAS_MAINWINDOW = 2;
   /** Defines that the tcz file is a library-only module. */  
   public static final short ATTR_LIBRARY = 4;

   /** The names of the files. */
   public String[] names;
   /** The attributes.
    * @see #ATTR_HAS_MAINCLASS
    * @see #ATTR_HAS_MAINWINDOW
    * @see #ATTR_LIBRARY
    */
   public int attr;
   /** Version of the tcz file.
    * @see #TCZ_VERSION
    */
   public int version;
   /** Offsets to the compressed data chunks */
   public int[] offsets;
   /** Sizes of the data chunks when they are uncompressed. */
   public int[] uncompressedSizes;
   /** The number of chunks. */
   public int numberOfChunks;
   /** Stores the total size when a TCZ file is created. */
   public int size;
   /** Bag that can be used to store anything that the user wants. */
   public Object bag;

   /** Create a TCZ file with the given vector of Entry(s).
    * @throws IOException
    * @throws ZipException */
   public TCZ(Vector<Entry> vout, String outName, short attr) throws IOException
   {
      if (!outName.toLowerCase().endsWith(".tcz"))
         outName =  outName.concat(".tcz");
      // creates an empty file for output
      String ss = Environment.getExternalStorageDirectory()+"/"+"_"+outName;
      FileOutputStream fout = new FileOutputStream(ss);
      
      Object[] out = vout.toArray();
      sort(out,0,out.length-1);

      // now we process the files.
      int n = out.length;

      // first pass, we setup the names and offset arrays
      offsets = new int[n+1]; // first offset is 0
      names = new String[n];
      uncompressedSizes = new int[n];
      int ofs = 0;
      for (int i =0; i < n; i++)
      {
         Entry of = (Entry)out[i];
         names[i] = of.name2write;
         ofs += of.bytes.length;
         offsets[i+1] = ofs;
         uncompressedSizes[i] = of.uncompressedSize;
      }

      // prepare the header
      ByteArrayOutputStream header = new ByteArrayOutputStream(4096);
      
      writeInt(header,n);
      for (int i = 0; i <= n; i++)
         writeInt(header,offsets[i]); // the offsets are stored first to ensure alignment.
      for (int i = 0; i < n; i++)
         writeInt(header,uncompressedSizes[i]); // idem
      for (int i = 0; i < n; i++) // strings are stored as UTF8
      {
         String s = names[i];
         int l = s.length();
         if (l > 255) throw new ZipException("Error: Name too long: "+s+". ("+l+" chars). Maximum allowed: 255 chars");
         writeSmallString(header,s);
      }
      byte[] hh = header.toByteArray();
      byte[] compressedHeaderBytes = compress(hh);
      AndroidUtils.debug("header: "+hh.length+" -> "+compressedHeaderBytes.length);

      // write the first part: the version, the attribute and the compressed header
      size += writeShort(fout, TCZ_VERSION);
      size += writeShort(fout, attr);
      size += writeInt(fout, compressedHeaderBytes.length+8); // base offset = compressed header size + 4
      fout.write(compressedHeaderBytes, 0, compressedHeaderBytes.length);
      size += compressedHeaderBytes.length;
      // now write the compressed chunks
      for (int i = 0; i < n; i++)
      {
         byte[] bytes = ((Entry)out[i]).bytes;
         fout.write(bytes);
         size += bytes.length;
      }
      fout.close();
      AndroidUtils.debug("OUTPUT: "+ss);
   }
   
   public static byte[] compress(byte[] in) throws IOException
   {
      ByteArrayOutputStream bc = new ByteArrayOutputStream(in.length/2);
      Deflater dd = new Deflater(9, false);
      DeflaterOutputStream def = new DeflaterOutputStream(bc, dd);
      def.write(in);
      def.finish();
      dd.end();
      return bc.toByteArray();
   }

   private static void sort(Object []items, int first, int last)
   {
      if (first >= last)
         return;
      int low = first;
      int high = last;

      String mid = items[(first+last) >> 1].toString();
      while (true)
      {
         while (high >= low && mid.compareTo(items[low].toString())  > 0) // guich@566_25: added "high > low" here and below - guich@568_5: changed to >=
            low++;
         while (high >= low && mid.compareTo(items[high].toString()) < 0)
            high--;
         if (low <= high)
         {
            Object temp = items[low];
            items[low++] = items[high];
            items[high--] = temp;
         }
         else break;
      }

      if (first < high)
         sort(items,first,high);
      if (low < last)
         sort(items,low,last);
   }

   public static int writeShort(OutputStream stream, int i) throws IOException
   {
      byte[] b = buffer;
      b[0] = (byte)i;
      i >>= 8;
      b[1] = (byte)i;
      stream.write(b, 0, 2);
      return 2;
   }

   static private byte[] buffer = new byte[256]; // starts with 256 bytes since readSmallString uses it
   
   public static int writeSmallString(OutputStream stream, String s) throws IOException
   {
      int len = s == null ? 0 : s.length();
      if (s.length() > 255) throw new IOException("String size "+s.length()+" is too big to use with writeSmallString!");
      int pos = 0;
      buffer[pos++] = (byte)len;
      int ret = len+1;
      if (len > 0)
         for (int i = 0; len-- > 0; i++)
            buffer[pos++] = (byte)s.charAt(i);
      stream.write(buffer, 0, pos);
      return ret;
   }

   public static int writeInt(OutputStream stream, int i) throws IOException
   {
      byte[] b = buffer;
      b[0] = (byte)i;
      i >>= 8; // guich@300_40
      b[1] = (byte)i;
      i >>= 8;
      b[2] = (byte)i;
      i >>= 8;
      b[3] = (byte)i;
      stream.write(b, 0, 4);
      return 4;
   }
}
