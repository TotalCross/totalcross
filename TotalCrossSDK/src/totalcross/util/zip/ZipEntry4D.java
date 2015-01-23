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



package totalcross.util.zip;

public class ZipEntry4D
{
   String name; // entry name
   int dostime = -1; // modification time (in DOS time)
   int crc = -1; // crc-32 of entry data
   int size = -1; // uncompressed size of entry data
   int csize = -1; // compressed size of entry data
   int method = -1; // compression method
   byte[] extra; // optional extra field data for entry
   String comment; // optional comment string for entry
   short known = 0;

   //   int time = -1; // modification time (in DOS time)
   //   short method = -1; // compression method
   //
   //   int dostime;

   int flags; /* used by ZipOutputStream */
   int offset; /* used by ZipFile and ZipOutputStream */

   public final static int STORED = 0;
   public final static int DEFLATED = 8;

   /**
    * Default constructor is used by native code.
    */
   ZipEntry4D()
   {
   }

   public ZipEntry4D(String name)
   {
      int length = name.length();
      if (length > 65535)
         throw new IllegalArgumentException("name length is " + length);
      this.name = name;
   }

   public ZipEntry4D(ZipEntry e)
   {
      name = e.name;
      known = e.known;
      size = e.size;
      csize = e.csize;
      crc = e.crc;
      dostime = e.dostime;
      method = e.method;
      extra = e.extra;
      comment = e.comment;
   }

   public String getName()
   {
      return name;
   }

   native public void setTime(long time);

   native public long getTime();

   public void setSize(long size)
   {
      if ((size & 0xffffffff00000000L) != 0)
         throw new IllegalArgumentException();
      this.size = (int) size;
   }

   public long getSize()
   {
      return size;
   }

   public void setCompressedSize(long csize)
   {
      if ((csize & 0xffffffff00000000L) != 0)
         throw new IllegalArgumentException();
      this.csize = (int) csize;
   }

   public long getCompressedSize()
   {
      return csize != -1 ? csize & 0xffffffffL : -1L;
   }

   public void setCrc(long crc)
   {
      if ((crc & 0xffffffff00000000L) != 0)
         throw new IllegalArgumentException();
      this.crc = (int) crc;
   }

   public long getCrc()
   {
      return crc != -1 ? crc & 0xffffffffL : -1L;
   }

   public void setMethod(int method)
   {
      if (method != STORED && method != DEFLATED)
         throw new IllegalArgumentException();
      this.method = (short) method;
   }

   public int getMethod()
   {
      return method;
   }

   public void setExtra(byte[] extra)
   {
      if (extra == null)
         this.extra = null;
      else if (extra.length > 0xffff)
         throw new IllegalArgumentException();
      else
      {
         this.extra = extra;
         try
         {
            int pos = 0;
            while (pos < extra.length)
            {
               int sig = (extra[pos++] & 0xff)
                     | (extra[pos++] & 0xff) << 8;
               int len = (extra[pos++] & 0xff)
                     | (extra[pos++] & 0xff) << 8;
               if (sig == 0x5455)
               {
                  /* extended time stamp */
                  int flags = extra[pos];
                  if ((flags & 1) != 0)
                  {
                     long time = ((extra[pos + 1] & 0xff)
                           | (extra[pos + 2] & 0xff) << 8
                           | (extra[pos + 3] & 0xff) << 16
                           | (extra[pos + 4] & 0xff) << 24);
                     setTime(time);
                  }
               }
               pos += len;
            }
         }
         catch (ArrayIndexOutOfBoundsException ex)
         {
            /* be lenient */
            return;
         }
      }
   }

   public byte[] getExtra()
   {
      return extra;
   }

   public void setComment(String comment)
   {
      if (comment != null && comment.length() > 0xffff)
         throw new IllegalArgumentException();
      this.comment = comment;
   }

   public String getComment()
   {
      return comment;
   }

   public boolean isDirectory()
   {
      int nlen = name.length();
      return nlen > 0 && name.charAt(nlen - 1) == '/';
   }

   public String toString()
   {
      return name;
   }

   public int hashCode()
   {
      return name.hashCode();
   }
}
