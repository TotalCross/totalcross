package totalcross.lang;

public class Integer4D
{
   public static final Class TYPE = Integer.class;
   int v;
   
   public Integer4D(int v)
   {
      this.v = v;
   }
   public int intValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Integer4D && ((Integer4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return v;
   }
   public String toString()
   {
      return super.toString()+" v="+v;
   }
}
