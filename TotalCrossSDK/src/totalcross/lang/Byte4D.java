package totalcross.lang;

public class Byte4D
{
   public static final Class TYPE = Byte.class;
   byte v;
   
   public Byte4D(byte v)
   {
      this.v = v;
   }
   public byte byteValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Byte4D && ((Byte4D)o).v == this.v; 
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
