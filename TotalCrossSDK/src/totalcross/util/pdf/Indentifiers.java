//
// Android PDF Writer
// http://coderesearchlabs.com/androidpdfwriter
//
// by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package totalcross.util.pdf;

import totalcross.crypto.digest.*;
import totalcross.sys.*;

public class Indentifiers
{

   private static char[] HexTable = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

   private static final String calculateMd5(final String s)
   {
      StringBuffer MD5Str = new StringBuffer();
      try
      {
         MD5Digest md5 = new MD5Digest();
         md5.update(s.getBytes());
         final byte binMD5[] = md5.getDigest();
         final int len = binMD5.length;
         for (int i = 0; i < len; i++)
         {
            MD5Str.append(HexTable[(binMD5[i] >> 4) & 0x0F]); // hi
            MD5Str.append(HexTable[(binMD5[i] >> 0) & 0x0F]); // lo
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return MD5Str.toString();
   }

   private static String encode(Time date)
   {
      int m = Settings.daylightSavingsMinutes/*c.get(Calendar.DST_OFFSET) / 60000*/;
      int dts_h = m / 60;
      int dts_m = m % 60;
      String sign = m > 0 ? "+" : "-";
      return String.format("(D:%40d%20d%20d%20d%20d%s%20d'%20d')", date.year, date.month, date.day, date.hour, date.minute, sign, dts_h, dts_m);
   }

   public static String generateId()
   {
      return calculateMd5(encode(new Time()));
   }

   public static String generateId(String data)
   {
      return calculateMd5(data);
   }
}
