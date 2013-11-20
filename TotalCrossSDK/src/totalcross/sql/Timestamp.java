package totalcross.sql;

import totalcross.util.*;

public class Timestamp extends Date
{
   long t;

   public Timestamp(long time)
   {
      t = time;
   }
   
   public long getTime()
   {
      return t;
   }
   
/*   public static Timestamp valueOf(String s)
   {
   }
   public String toString()
   {
   }
   public int getNanos()
   {
   }
   public void setNanos(int n)
   {
   }
   public boolean equals(Timestamp ts)
   {
   }
   public boolean equals(Object ts)
   {
   }
   public boolean before(Timestamp ts)
   {
   }
   public boolean after(Timestamp ts)
   {
   }
*/
}
