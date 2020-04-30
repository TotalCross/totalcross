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

/**
 * This class is used to represent a ZIP file entry.
 * 
 * @since TotalCross 1.20
 */
public class ZipEntry {
  String name; // entry name
  int dostime = -1; // modification time (in DOS time)
  long crc = -1; // crc-32 of entry data
  long size = -1; // uncompressed size of entry data
  long csize = -1; // compressed size of entry data
  int method = -1; // compression method
  byte[] extra; // optional extra field data for entry
  String comment; // optional comment string for entry

  short known = 0;

  int flags; /* used by ZipOutputStream */
  int offset; /* used by ZipFile and ZipOutputStream */

  /** Compression method. This method doesn't compress at all. */
  public final static int STORED = 0;
  /** Compression method. This method uses the Deflater. */
  public final static int DEFLATED = 8;

  Object zipEntry;

  ZipEntry(Object zipEntry) {
    this.zipEntry = (java.util.zip.ZipEntry) zipEntry;
  }

  /**
   * Creates a new zip entry with the specified name.
   * 
   * @param name
   *           the entry name
   * @throws NullPointerException
   *            if the entry name is null
   * @throws IllegalArgumentException
   *            if the entry name is longer than 0xFFFF bytes
   * 
   * @since TotalCross 1.20
   */
  public ZipEntry(String name) {
    zipEntry = new java.util.zip.ZipEntry(name);
  }

  /**
   * Creates a new zip entry with fields taken from the specified zip entry.
   * 
   * @param e
   *           a zip Entry object
   * 
   * @since TotalCross 1.23
   */
  public ZipEntry(ZipEntry e) {
    zipEntry = new java.util.zip.ZipEntry((java.util.zip.ZipEntry) e.zipEntry);
  }

  /**
   * Returns the name of the entry.
   * 
   * @return the name of the entry
   * 
   * @since TotalCross 1.20
   */
  public String getName() {
    return ((java.util.zip.ZipEntry) zipEntry).getName();
  }

  /**
   * Sets the modification time of the entry.
   * 
   * @param time
   *           the entry modification time in number of milliseconds since the epoch
   * 
   * @since TotalCross 1.23
   */
  public void setTime(long time) {
    ((java.util.zip.ZipEntry) zipEntry).setTime(time);
  }

  /**
   * Returns the modification time of the entry, or -1 if not specified.
   * 
   * @return the modification time of the entry, or -1 if not specified
   * 
   * @since TotalCross 1.23
   */
  public long getTime() {
    return ((java.util.zip.ZipEntry) zipEntry).getTime();
  }

  /**
   * Sets the uncompressed size of the entry data.
   * 
   * @param size
   *           the uncompressed size in bytes
   * @exception IllegalArgumentException
   *               if the specified size is less than 0 or greater than 0xFFFFFFFF bytes
   * 
   * @since TotalCross 1.23
   */
  public void setSize(long size) {
    ((java.util.zip.ZipEntry) zipEntry).setSize(size);
  }

  /**
   * Returns the uncompressed size of the entry data, or -1 if not known.
   * 
   * @return the uncompressed size of the entry data, or -1 if not known
   * 
   * @since TotalCross 1.23
   */
  public long getSize() {
    return ((java.util.zip.ZipEntry) zipEntry).getSize();
  }

  /**
   * Sets the size of the compressed entry data.
   * 
   * @param csize
   *           the compressed size to set to
   * 
   * @since TotalCross 1.23
   */
  public void setCompressedSize(long csize) {
    ((java.util.zip.ZipEntry) zipEntry).setCompressedSize(csize);
  }

  /**
   * Returns the size of the compressed entry data, or -1 if not known. In the case of a stored entry, the compressed
   * size will be the same as the uncompressed size of the entry.
   * 
   * @return the size of the compressed entry data, or -1 if not known
   * 
   * @since TotalCross 1.23
   */
  public long getCompressedSize() {
    return ((java.util.zip.ZipEntry) zipEntry).getCompressedSize();
  }

  /**
   * Sets the CRC-32 checksum of the uncompressed entry data.
   * 
   * @param crc
   *           the CRC-32 value
   * @exception IllegalArgumentException
   *               if the specified CRC-32 value is less than 0 or greater than 0xFFFFFFFF
   * 
   * @since TotalCross 1.23
   */
  public void setCrc(long crc) {
    ((java.util.zip.ZipEntry) zipEntry).setCrc(crc);
  }

  /**
   * Returns the CRC-32 checksum of the uncompressed entry data, or -1 if not known.
   * 
   * @return the CRC-32 checksum of the uncompressed entry data, or -1 if not known
   * 
   * @since TotalCross 1.23
   */
  public long getCrc() {
    return ((java.util.zip.ZipEntry) zipEntry).getCrc();
  }

  /**
   * Sets the compression method for the entry.
   * 
   * @param method
   *           the compression method, either STORED or DEFLATED
   * @exception IllegalArgumentException
   *               if the specified compression method is invalid
   * 
   * @since TotalCross 1.23
   */
  public void setMethod(int method) {
    ((java.util.zip.ZipEntry) zipEntry).setMethod(method);
  }

  /**
   * Returns the compression method of the entry, or -1 if not specified.
   * 
   * @return the compression method of the entry, or -1 if not specified
   * 
   * @since TotalCross 1.23
   */
  public int getMethod() {
    return ((java.util.zip.ZipEntry) zipEntry).getMethod();
  }

  /**
   * Sets the optional extra field data for the entry.
   * 
   * @param extra
   *           the extra field data bytes
   * @exception IllegalArgumentException
   *               if the length of the specified extra field data is greater than 0xFFFF bytes
   * 
   * @since TotalCross 1.23
   */
  public void setExtra(byte[] extra) {
    ((java.util.zip.ZipEntry) zipEntry).setExtra(extra);
  }

  /**
   * Returns the extra field data for the entry, or null if none.
   * 
   * @return the extra field data for the entry, or null if none
   * 
   * @since TotalCross 1.23
   */
  public byte[] getExtra() {
    return ((java.util.zip.ZipEntry) zipEntry).getExtra();
  }

  /**
   * Sets the optional comment string for the entry.
   * 
   * @param comment
   *           the comment string
   * @exception IllegalArgumentException
   *               if the length of the specified comment string is greater than 0xFFFF bytes
   * 
   * @since TotalCross 1.23
   */
  public void setComment(String comment) {
    ((java.util.zip.ZipEntry) zipEntry).setComment(comment);
  }

  /**
   * Returns the comment string for the entry, or null if none.
   * 
   * @return the comment string for the entry, or null if none
   * 
   * @since TotalCross 1.23
   */
  public String getComment() {
    return ((java.util.zip.ZipEntry) zipEntry).getComment();
  }

  /**
   * Returns true if this is a directory entry. A directory entry is defined to be one whose name ends with a '/'.
   * 
   * @return true if this is a directory entry
   * 
   * @since TotalCross 1.23
   */
  public boolean isDirectory() {
    return ((java.util.zip.ZipEntry) zipEntry).isDirectory();
  }

  /**
   * Returns a string representation of the ZIP entry. This is just the name as returned by getName().
   * 
   * @since TotalCross 1.23
   */
  @Override
  public String toString() {
    return ((java.util.zip.ZipEntry) zipEntry).toString();
  }

  /**
   * Returns the hash code value for this entry. This is just the hashCode of the name returned by getName().<br>
   * Note that the equals method isn't changed, though.
   * 
   * @since TotalCross 1.23
   */
  @Override
  public int hashCode() {
    return ((java.util.zip.ZipEntry) zipEntry).hashCode();
  }
}
