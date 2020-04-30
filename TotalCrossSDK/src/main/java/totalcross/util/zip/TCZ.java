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

package totalcross.util.zip;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStreamLE;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.util.Vector;

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

public class TCZ {
  /** Set this to be the main class name. It is stored in record #0. */
  public static String mainClassName; // remove dependency on DeploySettings

  /** An entry of the TCZ file. */
  public static class Entry {
    /** The compressed byte block. */
    public byte[] bytes;
    /** The name of the entry. */
    public String name;
    /** The size of the block when it is uncompressed. */
    public int uncompressedSize;
    /** Anything you want to hold here. */
    public Object extra; // JavaClass for the converter

    private String name2write; // guich@tc115_23

    public Entry(byte[] bytes, String name, int uncompressedSize) throws Exception {
      this(bytes, name, uncompressedSize, null);
    }

    public Entry(byte[] bytes, String name, int uncompressedSize, Object extra) throws Exception {
      this.uncompressedSize = uncompressedSize;
      this.bytes = bytes;
      this.name = name.replace('\\', '/');
      this.extra = extra;
      if (name.endsWith(".class")) {
        name = name.substring(0, name.length() - 6);
      }
      name2write = name;
      if (name2write.indexOf('.') < 0) {
        name2write = name2write.replace('/', '.'); // assuming not ending with .class
      }
    }

    /** Returns a String representing this Entry. Used in the Vector.qsort method */
    @Override
    public String toString() {
      if (mainClassName != null && name.equals(mainClassName)) {
        return '\1' + name2write; // make sure this name will go first when sorting
      }
      return name2write;
    }
  }

  public static final short TCZ_VERSION = 200; // must sync with tcz.h

  /** Defines that the tcz file has a MainClass at the first record. If false, it is a library-only module */
  public static final short ATTR_HAS_MAINCLASS = 1;
  /** Defines that the tcz file has a MainWindow at the first record. */
  public static final short ATTR_HAS_MAINWINDOW = 2;
  /** Defines that the tcz file is a library-only module. */
  public static final short ATTR_LIBRARY = 4;
  /** Defines that the application uses the new font set. */
  //public static final short ATTR_NEW_FONT_SET = 8;
  /** Defines that the application has resizable window. */
  public static final short ATTR_RESIZABLE_WINDOW = 16;
  /** Defines that the application uses the default font. */
  public static final short ATTR_WINDOWFONT_DEFAULT = 32;
  /** Defines that the application uses the given window size. */
  public static final short ATTR_WINDOWSIZE_320X480 = 64;
  /** Defines that the application uses the given window size. */
  public static final short ATTR_WINDOWSIZE_480X640 = 128;
  /** Defines that the application uses the given window size. */
  public static final short ATTR_WINDOWSIZE_600X800 = 256;

  /** The names of the files. */
  public String[] names;
  /** The attributes.
   * @see #ATTR_HAS_MAINCLASS
   * @see #ATTR_HAS_MAINWINDOW
   * @see #ATTR_LIBRARY
   * @see #ATTR_RESIZABLE_WINDOW
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

  private int idx;
  private Stream in;

  /** Create a TCZ file with the given vector of Entry(s).
   * @throws IOException
   * @throws ZipException */
  public TCZ(Vector vout, String outName, short attr) throws IOException, ZipException {
    if (!outName.toLowerCase().endsWith(".tcz")) {
      outName = outName.concat(".tcz");
    }
    // creates an empty file for output
    String path = Convert.getFilePath(outName);
    if (path != null) {
      try {
        new totalcross.io.File(path).createDir();
      } catch (totalcross.io.IOException e) {
      }
    }
    File fout = new File(outName, File.CREATE_EMPTY);

    vout.qsort(); // sort the files

    // now we process the files.
    int n = vout.size();

    // first pass, we setup the names and offset arrays
    offsets = new int[n + 1]; // first offset is 0
    names = new String[n];
    uncompressedSizes = new int[n];
    int ofs = 0;
    for (int i = 0; i < n; i++) {
      Entry of = (Entry) vout.items[i];
      names[i] = of.name2write;
      ofs += of.bytes.length;
      offsets[i + 1] = ofs;
      uncompressedSizes[i] = of.uncompressedSize;
    }

    // prepare the header
    ByteArrayStream header = new ByteArrayStream(4096);
    DataStreamLE dsh = new DataStreamLE(header);
    dsh.writeInt(n);
    for (int i = 0; i <= n; i++) {
      dsh.writeInt(offsets[i]); // the offsets are stored first to ensure alignment.
    }
    for (int i = 0; i < n; i++) {
      dsh.writeInt(uncompressedSizes[i]); // idem
    }
    for (int i = 0; i < n; i++) // strings are stored as UTF8
    {
      String s = names[i];
      int l = s.length();
      if (l > 255) {
        throw new ZipException("Error: Name too long: " + s + ". (" + l + " chars). Maximum allowed: 255 chars");
      }
      dsh.writeSmallString(s);
    }

    // write the first part: the version, the attribute and the compressed header
    DataStreamLE dsf = new DataStreamLE(fout);
    size += dsf.writeShort(TCZ_VERSION);
    size += dsf.writeShort(attr);
    ByteArrayStream bc = new ByteArrayStream(2048);
    header.mark();
    ZLib.deflate(header, bc, 9); // the smaller the compression level, the slower is the vm's startup
    size += dsf.writeInt(bc.getPos() + 8); // base offset = compressed header size + 4
    size += dsf.writeBytes(bc.getBuffer(), 0, bc.getPos());
    // now write the compressed chunks
    for (int i = 0; i < n; i++) {
      size += dsf.writeBytes(((Entry) vout.items[i]).bytes);
    }
    fout.close();
  }

  /** Reads a TCZ file and fill the public members available in this class: version, attr,
   * baseOffset, offsets, uncompressedSizes, names, chunks, numberOfChunks.
   * To preserve memory, you must request each chunk using readNextChunk <b>right after</b>
   * you call this constructor.
   * @throws IOException
   */
  public TCZ(Stream fin) throws IOException {
    this.in = fin;
    int baseOffset;
    DataStreamLE ds = new DataStreamLE(fin);
    this.version = ds.readShort();
    this.attr = ds.readShort();
    baseOffset = ds.readInt();
    ByteArrayStream bas = new ByteArrayStream(baseOffset - 8);
    DataStreamLE dsbas = new DataStreamLE(bas);
    ZLib.inflate(ds, bas, baseOffset - 8);
    bas.mark();
    int n = numberOfChunks = dsbas.readInt();
    offsets = new int[n + 1];
    uncompressedSizes = new int[n];
    for (int i = 0; i <= n; i++) {
      offsets[i] = baseOffset + dsbas.readInt();
    }
    for (int i = 0; i < n; i++) {
      uncompressedSizes[i] = dsbas.readInt();
    }
    names = new String[n];
    for (int i = 0; i < n; i++) {
      names[i] = dsbas.readSmallString();
    }
  }

  /** Returns the size of the next available chunk */
  public int getNextChunkSize() {
    return uncompressedSizes[idx];
  }

  /** Fills the given stream with the next available chunk.
   * The size of the chunk can be retrieved with getNextChunkSize.
   * If out is a ByteArrayStream, don't forget to call reset before starting to read from it!
   * @throws IOException 
   */
  public void readNextChunk(Stream out) throws IOException {
    int s = offsets[idx + 1] - offsets[idx];
    idx++;
    ZLib.inflate(in, out, s);
  }

  /** Finds the position of the given name in this tcz. */
  public int findNamePosition(String name) {
    String[] names = this.names;
    if (names[0].equals(name)) {
      return 0;
    } else {
      int inf = 1, sup = names.length - 1, half, res;
      while (inf <= sup) {
        half = (inf + sup) >> 1;
        res = name.compareTo(names[half]);
        if (res == 0) {
          return half;
        }
        if (res < 0) {
          sup = half - 1;
        } else {
          inf = half + 1;
        }
      }
      return -1;
    }
  }
}
