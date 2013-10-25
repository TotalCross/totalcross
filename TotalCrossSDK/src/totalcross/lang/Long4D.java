package totalcross.lang;

public class Long4D
{
   public static final Class TYPE = Long.class;
   long v;
   
   public Long4D(long v)
   {
      this.v = v;
   }
   public long longValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Long4D && ((Long4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return (int)(v ^ (v >>> 32));
   }
   public String toString()
   {
      return super.toString()+" v="+v;
   }
}
