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



package ras;

import totalcross.crypto.*;
import totalcross.crypto.digest.MD5Digest;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.util.Hashtable;
import totalcross.util.IntVector;
import totalcross.util.Vector;

public final class Utils
{
   // Auxiliary variables
   public static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251};
   private static final IntVector ivBuf = new IntVector();
   
   public static Hashtable getConfigInfo()
   {
      return null;
   }
   public native static Hashtable getConfigInfo4D();
   
   public static Hashtable getProductInfo()
   {
      Hashtable info = null;
      if (ActivationClient.activateOnJDK_DEBUG)
      {
         info = new Hashtable(10);
         info.put("COMPILATION_DATE", Convert.toString(CompilationDate4B.COMPILATION_DATE ^ 12341234));

         //flsobral@tc125: added more info on v2
         info.put("VERSAO_VM", Settings.versionStr);
      }
      return info;
   }
   public native static Hashtable getProductInfo4D();
   
   public static Hashtable getDeviceInfo() throws ActivationException
   {
      Hashtable info = null;
      if (ActivationClient.activateOnJDK_DEBUG)
      {
         MD5Digest md5;
         try
         {
            md5 = new MD5Digest();
         }
         catch (NoSuchAlgorithmException e)
         {
            throw new ActivationException(e.getMessage());
         }
         md5.update("1234ABCD".getBytes());
      
         info = new Hashtable(10);
         info.put("PLATFORM", Settings.WINDOWSMOBILE);
         info.put("ID", "Activation Test on JDK");
         info.put("HASH", Convert.bytesToHexString(md5.getDigest()));

         //flsobral@tc125: added more info on v2         
         info.put("VERSAO_ROM", Convert.toString(Settings.romVersion));
         info.put("COD_ATIVACAO", Settings.activationId);

         //flsobral@tc138: v3 info
         info.put("IMEI", Settings.imei);
         info.put("SERIAL", Settings.romSerialNumber);
      }
      return info;
   }
   public native static Hashtable getDeviceInfo4D() throws ActivationException;
   
   public static void writeInfo(DataStream ds, Hashtable info) throws IOException
   {
      int count = info.size();
      
      // Sort keys to always write in the same order
      Vector keys = info.getKeys();
      keys.qsort();
      
      // Write keys and values
      ds.writeInt(count);
      for (int i = 0; i < count; i++)
      {
         String k = (String)keys.items[i];
         String v = (String)info.get(k);
         
         ds.writeString(k);
         ds.writeString(v);
      }
   }
   
   public static void readInfo(DataStream dis, Hashtable info) throws IOException
   {
      int count = dis.readInt();
      for (int i = 0; i < count; i++)
      {
         String k = dis.readString();
         String v = dis.readString();
         
         info.put(k, v);
      }
   }
   
   public static void copyInfo(Hashtable from, Hashtable to)
   {
      from.copyInto(to);
   }
   
   public static int toInt(String s, int def)
   {
      try
      {
         return Convert.toInt(s);
      }
      catch (InvalidNumberException ex)
      {
         return def;
      }
   }
   
   public static boolean byteArrayEquals(byte[] b1, byte[] b2)
   {
      if (b1.length != b2.length)
         return false;
      
      for (int i = b1.length - 1; i >= 0; i--)
         if (b1[i] != b2[i])
            return false;
      
      return true;
   }
   
   public static int[] bytesToInts(byte[] bytes)
   {
      int count = bytes.length;
      int[] ints = new int[count];

      for (int i = 0; i < count; i++)
         ints[i] = (int)bytes[i] & 0xFF;

      return ints;
   }

   public static byte[] intsToBytes(int[] ints)
   {
      int count = ints.length;
      byte[] bytes = new byte[count];

      for (int i = 0; i < count; i++)
         bytes[i] = (byte)ints[i];

      return bytes;
   }

   public static int checksum(byte[] src, int mask)
   {
      return checksum(src, 0, src.length, mask);
   }

   public static int checksum(byte[] src, int offset, int count, int mask)
   {
      long hash = 0;

      while (--count >= 0)
         hash = (hash * 1313) + src[offset++];

      return (int)(hash & mask);
   }

   public static int[] getTwinPrimes(int number, int from)
   {
      int[] f1 = decompose(number);

      IntVector v = new IntVector();
      if (from < 2)
         from = 2;

      for (int i = from; i < 256; i++)
      {
         int[] f2 = decompose(i);
         boolean areTwins = true;

         for (int j = f1.length - 1; j >= 0; j--)
         {
            for (int k = f2.length - 1; k >= 0; k--)
            {
               if (f1[j] == f2[k])
               {
                  areTwins = false;
                  break;
               }
               else if (f2[k] > f1[j])
                  break;
            }
         }

         if (areTwins)
            v.addElement(i);
      }

      return v.toIntArray();
   }

   private static int[] decompose(int number)
   {
      IntVector v = ivBuf;
      v.removeAllElements();
      int[] PRIMES = Utils.PRIMES;

      for (int i = PRIMES.length - 1; i >= 0 && number > 1; i--)
      {
         while (number % PRIMES[i] == 0)
         {
            number /= PRIMES[i];
            v.addElement(PRIMES[i]);

            if (number == 1)
               break;
         }
      }

      return v.toIntArray();
   }

   public static ActivationException processException(String activity, Exception ex, boolean fatal)
   {
      ex.printStackTrace();
      String s = "";
      
      if (activity != null && activity.length() > 0)
         s = activity + " failed";
      
      return new ActivationException(s, ex);
   }

   public static ActivationException processException(String activity, String message)
   {
      if (message == null || message.length() == 0)
         message = "No detailed message";

      if (activity != null && activity.length() > 0)
         message = activity + " failed; reason: " + message;

      return new ActivationException(message);
   }
}
