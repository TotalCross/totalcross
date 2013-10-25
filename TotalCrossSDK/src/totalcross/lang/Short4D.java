package totalcross.lang;

public class Short4D
{
   public static final Class TYPE = Short.class;
   short v;
   
   public Short4D(short v)
   {
      this.v = v;
   }
   public short shortValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Short4D && ((Short4D)o).v == this.v; 
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
