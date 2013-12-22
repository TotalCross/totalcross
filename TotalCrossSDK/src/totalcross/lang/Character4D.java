package totalcross.lang;

public class Character4D
{
   public static final Class TYPE = Character.class;
   char v;
   
   public Character4D(char v)
   {
      this.v = v;
   }
   public char charValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Character4D && ((Character4D)o).v == this.v; 
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
