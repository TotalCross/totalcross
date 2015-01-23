package totalcross.sql;

import totalcross.sys.*;
import totalcross.util.*;

public class Timestamp extends Date
{
   long t;

   public Timestamp(Time t)
   {
   }
   
   public Timestamp(long time)
   {
      t = time;
   }
   
   public long getTime()
   {
      return t;
   }
   
   public String toString()
   {
      return super.toString()+", ts: "+t;
   }
/*   public static Timestamp valueOf(String s)
   {
   }*/
   public int getNanos()
   {
      return 0;
   }
   public void setNanos(int n)
   {
   }
   public boolean equals(Timestamp ts)
   {
      return ts.t == t;
   }
   public boolean equals(Object ts)
   {
      return ts instanceof Timestamp && ((Timestamp)ts).t == t;
   }
   public boolean before(Timestamp ts)
   {
      return t < ts.t;
   }
   public boolean after(Timestamp ts)
   {
      return t > ts.t;
   }

}
