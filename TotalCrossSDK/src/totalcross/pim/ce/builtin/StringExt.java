/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Gilbert Fridgen                                           *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.pim.ce.builtin;

/** StringBuffer-like class for internal use. */
public class StringExt
{
   private StringBuffer data;
   /* wrapped Contructors */
   public StringExt()
   {
      data = new StringBuffer(16);
   }
   public StringExt(int length)
   {
      data = new StringBuffer(length);
   }
   public StringExt(String str)
   {
      data = new StringBuffer(str);
   }
   public StringExt(StringBuffer str)
   {
      data = new StringBuffer(str.toString());
   }
   public StringExt(StringExt str)
   {
      data = new StringBuffer(str.toString());
   }
   /* wrapped methods in alphabetical order */
   public StringExt append(boolean b)
   {
      data.append(b);
      return this;
   }
   public StringExt append(char c)
   {
      data.append(c);
      return this;
   }
   public StringExt append(char[] c)
   {
      data.append(c);
      return this;
   }
   public StringExt append(char[] c, int offset, int length)
   {
      data.append(c, offset, length);
      return this;
   }
   public StringExt append(double d)
   {
      data.append(d);
      return this;
   }
   public StringExt append(int i)
   {
      data.append(i);
      return this;
   }
   public StringExt append(long l)
   {
      data.append(l);
      return this;
   }
   public StringExt append(Object obj)
   {
      data.append(obj);
      return this;
   }
   public StringExt append(String str)
   {
      data.append(str);
      return this;
   }
   public int capacity()
   {
      return data.capacity();
   }
   public char charAt(int i)
   {
      return data.charAt(i);
   }
   public StringExt delete(int start, int end)
   {
      data.delete(start, end);
      return this;
   }
   public StringExt deleteCharAt(int i)
   {
      data.delete(i, i);
      return this;
   }
   public void ensureCapacity(int min)
   {
      data.ensureCapacity(min);
   }
   public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)
   {
      data.getChars(srcBegin, srcEnd, dst, dstBegin);
   }
   public int length()
   {
      return data.length();
   }
   public void setLength(int newLength)
   {
      data.setLength(newLength);
   }
   public String toString()
   {
      return data.toString();
   }
   public StringExt clear()
   {
      data.setLength(0);
      return this;
   }
   public boolean isEmpty()
   {
      return (data.length() == 0);
   }
   public StringExt cut(int numberOfChars)
   {
      if (length() - numberOfChars > 0)
         setLength(length() - numberOfChars);
      else
         setLength(0);
      return this;
   }
   public boolean contains(char c)
   {
      int n = length();
      for (int i = 0; i < n; i++)
         if (data.charAt(i) == c)
            return true;
      return false;
   }
   public boolean startsWith(char c)
   {
      if (length() == 0)
         return false;
      else
         return charAt(0) == c;
   }
   public boolean startsWith(String str)
   {
      int n = str.length();
      if (n > length())
         return false;
      for (int i = 0; i < n; i++)
         if (str.charAt(i) != data.charAt(i))
            return false;
      return true;
   }
   public StringExt startWith(char c)
   {
      if (!startsWith(c))
         append(c);
      return this;
   }
   public StringExt startWith(String str)
   {
      if (!startsWith(str))
         append(str);
      return this;
   }
   public boolean endsWith(char c)
   {
      int len = length();
      return (len == 0)?false: (charAt(len - 1) == c);
   }
   public StringExt endWith(char c)
   {
      if (!endsWith(c))
         append(c);
      return this;
   }
   public int count(char c)
   {
      int counter = 0;
      int n = length();
      for (int i = 0; i < n; i++)
         if (data.charAt(i) == c)
            counter++;
      return counter;
   }
   public int count(String str)
   {
      int counter = 0;
      int strLength = str.length();
      char[] comp = str.toCharArray();
      int n;
      //int n = 0; // performance mesurement
      StringExt buffer;
      dataLoop:
      {
         n = length();
         for (int i = 0; i < n; i++)
         {
            if (i + strLength > n)
               break dataLoop; // this is the end; this isn't long enough any more
            buffer = new StringExt();
            bufferLoop:
            {
               for (int j = 0; j < strLength; j++)
               {
                  //n++;
                  char tmp = charAt(i + j);
                  if (!(tmp == comp[j]))
                     break bufferLoop; // buffer doen't match char for char terminate
                  buffer.append(tmp);
               }
               // we will only get here if bufferLoop hasn't been broken - that means buffer == str !
               counter++;
               i += strLength - 1; // we found something, so jump ahead length of found Exp
            }
         }
      }
      //System.out.println("needed " + n + " iterations");
      return counter;
   }
}
