package totalcross.lang;


public class Boolean4D
{
   public static final Class TYPE = Boolean.class;
   public static final Boolean4D TRUE = new Boolean4D(true);
   public static final Boolean4D FALSE = new Boolean4D(false);
   boolean v;
   
   public Boolean4D(boolean v)
   {
      this.v = v;
   }
   public boolean booleanValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Boolean4D && ((Boolean4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return v ? 1 : 0;
   }
   public String toString()
   {
      return super.toString()+" v="+v;
   }
   public static Boolean4D valueOf(String s) 
   {
      return new Boolean4D(s != null && s.equalsIgnoreCase("true"));
   }
}
